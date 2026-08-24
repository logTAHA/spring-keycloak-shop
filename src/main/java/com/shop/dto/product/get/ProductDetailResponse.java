package com.shop.dto.product.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.product.Product;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDetailResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDetailResponse from(Product product, boolean isAdmin) {
        Integer visibleStock;
        LocalDateTime visibleCreatedAt;
        LocalDateTime visibleUpdatedAt;

        if (isAdmin) {
            visibleStock = product.getStock();
            visibleCreatedAt = product.getCreatedAt();
            visibleUpdatedAt = product.getLastUpdate();
        } else {
            visibleStock = (product.getStock() != null && product.getStock() > 0) ? 1 : 0;
            visibleCreatedAt = null;
            visibleUpdatedAt = null;
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(visibleStock)
                .createdAt(visibleCreatedAt)
                .updatedAt(visibleUpdatedAt)
                .build();
    }
}
