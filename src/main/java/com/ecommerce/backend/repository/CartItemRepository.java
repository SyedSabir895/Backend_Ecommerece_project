package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Integer userId);

    Optional<CartItem> findByUserIdAndProductId(Integer userId, Long productId);

    void deleteByUserIdAndProductId(Integer userId, Long productId);

    void deleteByUserId(Integer userId);
}