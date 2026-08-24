package com.shop.service;

import com.shop.dto.cart.create.CreateCartResponse;
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

import java.util.Optional;

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

        return CartResponse.from(cart);
    }

    @Transactional
    public UpdateCartItemResponse addOrUpdateItem(String userId, UpdateCartItemRequest request) {
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
}
