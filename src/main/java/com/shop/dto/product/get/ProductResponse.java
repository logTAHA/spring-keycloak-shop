package com.shop.dto.product.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.product.Product;
import lombok.Builder;
import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock
) {
    public static ProductResponse from(Product product) {
        Integer filteredStock = (product.getStock() != null && product.getStock() > 0) ? 1 : 0;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(filteredStock)
                .build();
    }
}
