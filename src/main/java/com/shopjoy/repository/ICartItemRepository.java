package com.shopjoy.repository;

import com.shopjoy.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface ICartItemRepository extends GenericRepository<CartItem, Integer> {
    List<CartItem> findByUserId(int userId);
    Optional<CartItem> findByUserAndProduct(int userId, int productId);
    void clearCart(int userId);
}
