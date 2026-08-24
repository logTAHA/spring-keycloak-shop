package com.shop.dto.product.get;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GetProductRequest(
        @NotNull(message = "Page number cannot be null")
        @Min(value = 0, message = "Page number cannot be negative")
        Integer page,

        @NotNull(message = "Page size cannot be null")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer size,

        Boolean inStockOnly,

        ProductSortBy sortBy
) {}