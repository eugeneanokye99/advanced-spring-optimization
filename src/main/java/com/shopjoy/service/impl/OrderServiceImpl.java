package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.OrderItemMapper;
import com.shopjoy.dto.mapper.OrderMapper;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderItemRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderItem;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.OrderItemRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Order service.
 */
@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {



    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final UserService userService;

    /**
     * Instantiates a new Order service.
     *
     * @param orderRepository     the order repository
     * @param orderItemRepository the order item repository
     * @param inventoryService    the inventory service
     * @param productService      the product service
     * @param userService         the user service
     */
    public OrderServiceImpl(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            InventoryService inventoryService,
            ProductService productService,
            UserService userService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * COMPLEX TRANSACTION EXAMPLE
     * <p>
     * This method demonstrates a multi-entity transaction:
     * 1. Validates user exists
     * 2. Validates all products exist and are active
     * 3. Checks inventory availability for all items
     * 4. Reserves inventory (decreases stock)
     * 5. Creates order
     * 6. Creates order items
     * <p>
     * If ANY step fails, entire transaction is rolled back.
     * Uses SERIALIZABLE isolation to prevent phantom reads during stock checks.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(CreateOrderRequest request) {
        userService.getUserById(request.getUserId());

        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new ValidationException("Shipping address is required");
        }

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        // Validate products and inventory FIRST
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            if (!product.isActive()) {
                throw new ValidationException("Product " + product.getProductName() + " is not active");
            }
            if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getProductName());
            }
        }

        // Reserve inventory
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
        }

        // Create the order
        Order order = OrderMapper.toOrder(request);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order createdOrder = orderRepository.save(order);

        // Create order items
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(createdOrder.getOrderId())
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getPrice())
                    .subtotal(itemReq.getQuantity() * itemReq.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }


        return convertToResponse(createdOrder);
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return convertToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * STATE MACHINE PATTERN EXAMPLE
     * <p>
     * Order status transitions follow a specific workflow:
     * PENDING -> PROCESSING -> SHIPPED -> DELIVERED
     * -> CANCELLED (from PENDING or PROCESSING only)
     */
    @Override
    @Transactional()
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.update(order);

        return convertToResponse(updatedOrder);
    }

    @Override
    @Transactional()
    public OrderResponse confirmOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "confirm");
        }

        return updateOrderStatus(orderId, OrderStatus.PROCESSING);
    }

    @Override
    @Transactional()
    public OrderResponse shipOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "ship");
        }

        return updateOrderStatus(orderId, OrderStatus.SHIPPED);
    }

    @Override
    @Transactional()
    public OrderResponse completeOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "complete");
        }

        return updateOrderStatus(orderId, OrderStatus.DELIVERED);
    }

    /**
     * COMPLEX ROLLBACK SCENARIO
     * <p>
     * When cancelling an order, we must:
     * 1. Validate order can be canceled
     * 2. Release reserved inventory back to stock
     * 3. Update order status
     * <p>
     * If inventory release fails, entire transaction rolls back.
     */
    @Override
    @Transactional()
    public OrderResponse cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(
                    orderId,
                    order.getStatus().toString(),
                    "cancel (can only cancel PENDING or PROCESSING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.update(order);

        return convertToResponse(cancelledOrder);
    }

    @Override
    public List<OrderResponse> getPendingOrders() {
        return getOrdersByStatus(OrderStatus.PENDING);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

@Transactional(isolation = Isolation.SERIALIZABLE)
public OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new InvalidOrderStateException(orderId, order.getStatus().toString(), 
            "update (can only update PENDING orders)");
    }

    // Update basic order fields
    if (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty()) {
        order.setShippingAddress(request.getShippingAddress());
    }

    if (request.getPaymentMethod() != null) {
        order.setPaymentMethod(request.getPaymentMethod());
    }

    if (request.getNotes() != null) {
        order.setNotes(request.getNotes());
    }

    // Handle order items if provided
    if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
        List<OrderItem> existingItems = orderItemRepository.findByOrderId(orderId);
        
        // Release inventory for old items
        for (OrderItem item : existingItems) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }

        // Delete old items
        for (OrderItem item : existingItems) {
            orderItemRepository.delete(item.getOrderItemId());
        }

        // Validate and reserve inventory for new items
        double newTotal = 0.0;
        for (UpdateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            if (!product.isActive()) {
                throw new ValidationException("Product " + product.getProductName() + " is not active");
            }
            if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getProductName());
            }
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
            newTotal += itemReq.getPrice() * itemReq.getQuantity();
        }

        // Create new order items
        for (UpdateOrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(orderId)
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getPrice())
                    .subtotal(itemReq.getQuantity() * itemReq.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Update total amount
        order.setTotalAmount(newTotal);
    }

    order.setUpdatedAt(LocalDateTime.now());
    Order updatedOrder = orderRepository.update(order);
    
    return convertToResponse(updatedOrder);
}


    @Override
    @Transactional()
    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "delete (can only delete PENDING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }

        orderRepository.delete(orderId);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    0,
                    currentStatus.toString(),
                    "Cannot change status of cancelled or delivered orders");
        }

        switch (newStatus) {
            case PENDING:
                // PENDING is the initial state, no validation needed
                break;
            case PROCESSING:
                if (currentStatus != OrderStatus.PENDING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only process PENDING orders");
                }
                break;
            case SHIPPED:
                if (currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only ship PROCESSING orders");
                }
                break;
            case DELIVERED:
                if (currentStatus != OrderStatus.SHIPPED) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only deliver SHIPPED orders");
                }
                break;
            case CANCELLED:
                if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only cancel PENDING or PROCESSING orders");
                }
                break;
        }
    }

    private OrderResponse convertToResponse(Order order) {
        String userName = "Unknown User";
        try {
            UserResponse user = userService.getUserById(order.getUserId());
            userName = user.getFirstName() + " " + user.getLastName();
        } catch (Exception e) {
            // Ignore user fetch errors
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            String productName = "Unknown Product";
            try {
                ProductResponse product = productService.getProductById(item.getProductId());
                productName = product.getProductName();
            } catch (Exception e) {
                // Ignore product fetch errors
            }
            return OrderItemMapper.toOrderItemResponse(item, productName);
        }).collect(Collectors.toList());

        return OrderMapper.toOrderResponse(order, userName, itemResponses);
    }
}
