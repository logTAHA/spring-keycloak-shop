package com.shop.dto.cart.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shop.entity.cart.CartStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckoutResponse(
        Long cartId,
        BigDecimal totalPrice,
        CartStatus status,
        String paymentUrl
) {}
