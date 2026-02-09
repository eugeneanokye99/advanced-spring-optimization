package com.shopjoy.service;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.exception.InsufficientStockException;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Order-related business operations.
 * Handles order creation, status management, and complex order workflows.
 */
public interface OrderService {

    /**
     * Creates a new order for a user.
     * Validates product availability, updates inventory, and calculates total.
     * This is a complex transaction involving multiple entities.
     * 
     * @param request the create order request
     * @return the created order response
     * @throws ResourceNotFoundException  if user or products not found
     * @throws InsufficientStockException if any product is out of stock
     * @throws ValidationException        if order data is invalid
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Retrieves an order by its ID.
     * 
     * @param orderId the order ID
     * @return the order response
     * @throws ResourceNotFoundException if order not found
     */
    OrderResponse getOrderById(Integer orderId);

    /**
     * Retrieves all orders for a specific user.
     * 
     * @param userId the user ID
     * @return list of user's order responses
     */
    List<OrderResponse> getOrdersByUser(Integer userId);

    /**
     * Retrieves all orders with a specific status.
     * 
     * @param status the order status
     * @return list of order responses with the status
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * Retrieves orders within a date range.
     * 
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of order responses in the date range
     */
    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Updates an order's status.
     * Validates that the status transition is allowed.
     * 
     * @param orderId   the order ID
     * @param newStatus the new status
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if status transition is invalid
     */
    OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus);

    /**
     * Confirms a pending order (moves to CONFIRMED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not pending
     */
    OrderResponse confirmOrder(Integer orderId);

    /**
     * Ships an order (moves to SHIPPED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not confirmed
     */
    OrderResponse shipOrder(Integer orderId);

    /**
     * Completes an order (moves to COMPLETED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not shipped
     */
    OrderResponse completeOrder(Integer orderId);

    /**
     * Cancels an order and restores inventory.
     * Can only cancel pending or confirmed orders.
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order cannot be cancelled
     */
    OrderResponse cancelOrder(Integer orderId);

    /**
     * Retrieves pending orders (requires action).
     * 
     * @return list of pending order responses
     */
    /**
     * Retrieves pending orders (requires action).
     * 
     * @return list of pending order responses
     */
    List<OrderResponse> getPendingOrders();

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request);

    void deleteOrder(Integer orderId);
}
