package com.shop.dto.cart.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateCartItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = -10, message = "Quantity must be at least -10")
        @Max(value = 10, message = "Quantity must be at most 10")
        Integer quantity
) {}
