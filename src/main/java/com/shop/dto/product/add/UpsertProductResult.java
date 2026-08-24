package com.shop.dto.product.add;

public record UpsertProductResult(
        AddProductResponse product,
        boolean isCreated
) {}
