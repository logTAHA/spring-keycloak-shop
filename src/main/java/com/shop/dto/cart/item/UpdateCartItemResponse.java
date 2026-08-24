package com.shop.dto.cart.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record UpdateCartItemResponse(
        Long itemId,
        Long productId,
        Integer quantity
) {}
