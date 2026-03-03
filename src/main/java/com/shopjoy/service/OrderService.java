package com.shopjoy.service;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface OrderService {

    CompletableFuture<OrderResponse> createOrder(CreateOrderRequest request);

    CompletableFuture<OrderResponse> processPayment(Integer orderId, String transactionId);

    OrderResponse getOrderById(Integer orderId);

    Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable);

    OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request);

    OrderResponse cancelOrder(Integer orderId);

    OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus);

    void deleteOrder(Integer orderId);
}
