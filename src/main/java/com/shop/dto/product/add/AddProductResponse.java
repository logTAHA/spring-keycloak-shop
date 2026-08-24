package com.shop.dto.product.add;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddProductResponse(
        Long id,
        String name
) {}
