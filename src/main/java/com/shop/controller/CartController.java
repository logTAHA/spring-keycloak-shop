package com.shop.controller;

import com.shop.dto.ApiResponse;
import com.shop.dto.cart.create.CreateCartRequest;
import com.shop.dto.cart.create.CreateCartResponse;
import com.shop.dto.cart.get.CartResponse;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCartResponse>> createCart(
            @RequestBody(required = false) CreateCartRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = (jwt != null) ? jwt.getSubject() : "anonymous";
        String name = (request != null) ? request.name() : null;
        log.info("Processing create cart request (name: '{}') by user ID '{}'", name, userId);

        CreateCartResponse response = cartService.createCart(userId, name);

        log.info("Cart (ID: {}) was successfully created for user ID '{}'", response.id(), userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cart created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> getCartById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        String userId = (jwt != null) ? jwt.getSubject() : "anonymous";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        log.info("Fetching cart ID {} for user ID '{}' (isAdmin: {})", id, userId, isAdmin);

        CartResponse cart = cartService.getCartById(id, userId, isAdmin);

        return ResponseEntity.ok(ApiResponse.ok(cart));
    }
}
