package com.shop.dto.cart.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.cart.CartItem;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        Integer quantity
) {
    public static CartItemResponse from(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productPrice(item.getProduct() != null ? item.getProduct().getPrice() : null)
                .quantity(item.getQuantity())
                .build();
    }
}
