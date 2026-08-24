package com.shop.dto.cart.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartResponse(
        Long id,
        String userId,
        CartStatus status,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        LocalDateTime createdAt
) {
    public static CartResponse of(Cart cart, List<CartItemResponse> itemResponses, BigDecimal totalPrice) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .status(cart.getStatus())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .createdAt(cart.getCreatedAt())
                .build();
    }
}
