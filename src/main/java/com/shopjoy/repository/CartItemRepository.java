package com.shopjoy.repository;

import com.shopjoy.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(int userId);
    
    Optional<CartItem> findByUserIdAndProductId(int userId, int productId);
    
    void deleteByUserId(int userId);
}
