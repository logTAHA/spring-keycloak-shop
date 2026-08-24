package com.shop.service;

import com.shop.dto.cart.create.CreateCartResponse;
import com.shop.dto.cart.get.CartResponse;
import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartStatus;
import com.shop.handler.BusinessException;
import com.shop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public CreateCartResponse createCart(String userId, String name) {
        log.info("Creating new cart for user ID '{}' with name '{}'", userId, name);

        Cart cart = Cart.builder()
                .userId(userId)
                .name(name)
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
            throw new BusinessException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return CartResponse.from(cart);
    }
}
