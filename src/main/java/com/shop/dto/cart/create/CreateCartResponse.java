package com.shop.dto.cart.create;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCartResponse(
        Long id,
        String userId,
        CartStatus status,
        LocalDateTime createdAt
) {
    public static CreateCartResponse from(Cart cart) {
        return CreateCartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .build();
    }
}
