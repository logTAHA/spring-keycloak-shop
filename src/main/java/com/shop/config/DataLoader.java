package com.shop.config;

import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartItem;
import com.shop.entity.cart.CartStatus;
import com.shop.entity.product.Product;
import com.shop.repository.CartRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Override
    public void run(String @NonNull ... args) {
        if (productRepository.count() == 0) {
            log.info("No products found. Loading initial sample data from dump...");

            Product macbook = productRepository.save(Product.builder()
                    .name("MacBook Air M4")
                    .price(new BigDecimal("99000000.00"))
                    .stock(20)
                    .build());

            Product s24 = productRepository.save(Product.builder()
                    .name("S24 Ultra")
                    .price(new BigDecimal("210000000.00"))
                    .stock(3)
                    .build());

            Product rtx = productRepository.save(Product.builder()
                    .name("Rtx 5090")
                    .price(new BigDecimal("800000000.00"))
                    .stock(0)
                    .build());

            log.info("Loaded 3 sample products successfully: MacBook Air M4, S24 Ultra, Rtx 5090.");

            String userId = "88e2118c-2a5d-4fc5-a5db-9a454f9c8e7a";

            // 1. Pending Payment Cart
            Cart pendingCart = Cart.builder()
                    .userId(userId)
                    .status(CartStatus.PENDING_PAYMENT)
                    .items(new ArrayList<>())
                    .build();

            CartItem pendingItem = CartItem.builder()
                    .cart(pendingCart)
                    .product(macbook)
                    .quantity(2)
                    .build();

            pendingCart.getItems().add(pendingItem);
            cartRepository.save(pendingCart);

            // 2. Active Cart
            Cart activeCart = Cart.builder()
                    .userId(userId)
                    .status(CartStatus.ACTIVE)
                    .items(new ArrayList<>())
                    .build();

            CartItem activeItem = CartItem.builder()
                    .cart(activeCart)
                    .product(rtx)
                    .quantity(2)
                    .build();

            activeCart.getItems().add(activeItem);
            cartRepository.save(activeCart);

            log.info("Loaded sample carts (PENDING_PAYMENT and ACTIVE) with items for user '{}'.", userId);
        }
    }
}
