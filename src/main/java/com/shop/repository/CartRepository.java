package com.shop.repository;

import com.shop.entity.cart.Cart;
import com.shop.entity.cart.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);

    boolean existsByUserIdAndStatus(String userId, CartStatus status);
}
