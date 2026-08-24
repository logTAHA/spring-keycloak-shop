package com.shop.service;

import com.shop.dto.cart.checkout.CheckoutResponse;
import com.shop.dto.cart.create.CreateCartResponse;
import com.shop.dto.cart.get.CartItemResponse;
import com.shop.dto.cart.get.CartResponse;
import com.shop.dto.cart.item.UpdateCartItemRequest;
import com.shop.dto.cart.item.UpdateCartItemResponse;
import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartItem;
import com.shop.entity.cart.CartStatus;
import com.shop.entity.product.Product;
import com.shop.handler.BusinessException;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private static final int MAX_ITEM_QUANTITY = 10;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CreateCartResponse createCart(String userId) {
        log.info("Creating new cart for user ID '{}'", userId);

        if (cartRepository.existsByUserIdAndStatus(userId, CartStatus.ACTIVE)) {
            log.warn("User '{}' already has an active cart", userId);
            throw new BusinessException(HttpStatus.CONFLICT, "You already have an active cart");
        }

        Cart cart = Cart.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart (ID: {}) successfully created for user ID '{}'", savedCart.getId(), userId);

        return CreateCartResponse.from(savedCart);
    }

    public CartResponse getCartById(Long cartId, String userId, boolean isAdmin) {
        log.debug("Fetching cart ID {} for userId='{}', isAdmin={}", cartId, userId, isAdmin);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cart not found"));

        if (!isAdmin && !cart.getUserId().equals(userId)) {
            log.warn("User '{}' unauthorized to view cart ID {}", userId, cartId);
            throw new BusinessException(HttpStatus.NOT_FOUND, "Cart not found");
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public UpdateCartItemResponse upsertItem(String userId, UpdateCartItemRequest request) {
        log.info("Processing item update for user ID '{}', product ID {}, quantity {}",
                userId, request.productId(), request.quantity());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    log.info("No active cart found for user ID '{}', creating a new one", userId);
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .status(CartStatus.ACTIVE)
                            .build();
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int currentQuantity = existingItem.map(CartItem::getQuantity).orElse(0);
        int newQuantity = currentQuantity + request.quantity();

        if (newQuantity > MAX_ITEM_QUANTITY) {
            log.warn("User '{}' tried to set total quantity to {} for product ID {} (max: {})",
                    userId, newQuantity, product.getId(), MAX_ITEM_QUANTITY);
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Total quantity cannot exceed " + MAX_ITEM_QUANTITY + " items per product (current: " + currentQuantity + ", requested: " + request.quantity() + ")");
        }

        if (newQuantity <= 0) {
            Long removedItemId = null;
            if (existingItem.isPresent()) {
                CartItem itemToRemove = existingItem.get();
                removedItemId = itemToRemove.getId();
                cart.getItems().remove(itemToRemove);
                cartItemRepository.delete(itemToRemove);
                log.info("Removed product ID {} from cart ID {}", product.getId(), cart.getId());
            }
            return UpdateCartItemResponse.builder()
                    .itemId(removedItemId)
                    .productId(product.getId())
                    .quantity(0)
                    .build();
        } else {
            if (product.getStock() != null && product.getStock() < newQuantity) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Requested total quantity (" + newQuantity + ") is more than available stock"
                );
            }

            CartItem savedItem;
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(newQuantity);
                savedItem = cartItemRepository.save(item);
                log.info("Updated product ID {} quantity to {} in cart ID {}", product.getId(), newQuantity, cart.getId());
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(newQuantity)
                        .build();
                cart.getItems().add(newItem);
                savedItem = cartItemRepository.save(newItem);
                log.info("Added product ID {} with quantity {} to cart ID {}", product.getId(), newQuantity, cart.getId());
            }

            return UpdateCartItemResponse.builder()
                    .itemId(savedItem.getId())
                    .productId(product.getId())
                    .quantity(savedItem.getQuantity())
                    .build();
        }
    }

    @Transactional
    public CheckoutResponse checkout(String userId) {
        log.info("Processing checkout for user ID '{}'", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No active cart found to checkout"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("User '{}' attempted to checkout an empty cart ID {}", userId, cart.getId());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null || product.getStock() == null || product.getStock() < item.getQuantity()) {
                String productName = (product != null) ? product.getName() : "Unknown";
                int availableStock = (product != null && product.getStock() != null) ? product.getStock() : 0;
                log.warn("Larger than available stock for product '{}' in cart ID {}: requested {}, available {}",
                        productName, cart.getId(), item.getQuantity(), availableStock);
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Product '" + productName + "' quantity is larger than stock");
            }
        }

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .toList();
        BigDecimal totalPrice = calculateCartTotal(itemResponses);

        cart.setStatus(CartStatus.PENDING_PAYMENT);
        cartRepository.save(cart);
        log.info("Cart ID {} status changed to PENDING_PAYMENT for user ID '{}'", cart.getId(), userId);

        String mockPaymentUrl = "https://mock-gateway.com/pay/" + UUID.randomUUID();

        return CheckoutResponse.builder()
                .cartId(cart.getId())
                .totalPrice(totalPrice)
                .status(cart.getStatus())
                .paymentUrl(mockPaymentUrl)
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = (cart.getItems() != null)
                ? cart.getItems().stream()
                        .map(this::mapToCartItemResponse)
                        .toList()
                : List.of();

        BigDecimal totalPrice = calculateCartTotal(itemResponses);

        return CartResponse.of(cart, itemResponses, totalPrice);
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal itemTotal = calculateItemTotal(item);
        return CartItemResponse.of(item, itemTotal);
    }

    private BigDecimal calculateItemTotal(CartItem item) {
        if (item.getProduct() == null || item.getProduct().getPrice() == null || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal calculateCartTotal(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.totalPrice() != null ? item.totalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
