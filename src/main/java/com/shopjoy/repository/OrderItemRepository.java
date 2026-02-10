package com.shopjoy.repository;

import com.shopjoy.entity.OrderItem;

import java.util.List;

public interface IOrderItemRepository extends GenericRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(int orderId);
}
