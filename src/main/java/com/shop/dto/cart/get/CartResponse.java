package com.shop.dto.cart.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartResponse(
        Long id,
        String name,
        String userId,
        CartStatus status,
        List<CartItemResponse> items,
        LocalDateTime createdAt
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> itemResponses = (cart.getItems() != null)
                ? cart.getItems().stream().map(CartItemResponse::from).toList()
                : List.of();

        return CartResponse.builder()
                .id(cart.getId())
                .name(cart.getName())
                .userId(cart.getUserId())
                .status(cart.getStatus())
                .items(itemResponses)
                .createdAt(cart.getCreatedAt())
                .build();
    }
}
