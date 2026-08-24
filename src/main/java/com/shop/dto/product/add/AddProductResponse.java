package com.shop.dto.product.add;

import lombok.Builder;

@Builder
public record AddProductResponse(
        Long id,
        String name
) {}
