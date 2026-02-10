package com.shopjoy.repository;

import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderRepository extends GenericRepository<Order, Integer> {
    List<Order> findByUserId(int userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    boolean hasUserPurchasedProduct(int userId, int productId);
}
