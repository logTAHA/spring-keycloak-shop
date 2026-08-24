package com.shop.dto.product.get;

import com.shop.entity.Product;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock
) {
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
