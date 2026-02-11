package com.shopjoy.service.impl;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.mapper.OrderMapperStruct;
import com.shopjoy.dto.mapper.OrderItemMapperStruct;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderItemRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderItem;
import com.shopjoy.entity.Product;
import com.shopjoy.entity.User;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.OrderItemRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.specification.OrderSpecification;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
    private final OrderMapperStruct orderMapper;
    private final OrderItemMapperStruct orderItemMapper;

    /**
     * Instantiates a new Order service.
     *
     * @param orderRepository     the order repository
     * @param orderItemRepository the order item repository
     * @param inventoryService    the inventory service
     * @param productService      the product service
     * @param userService         the user service
     * @param orderMapper         the order mapper
     * @param orderItemMapper     the order item mapper
     */
    public OrderServiceImpl(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            InventoryService inventoryService,
            ProductService productService,
            UserService userService,
            OrderMapperStruct orderMapper,
            OrderItemMapperStruct orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.userService = userService;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
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
     * If ANY step fails (e.g. InsufficientStockException), the entire transaction 
     * including inventory reservations and order record creation will be rolled back.
     * Uses SERIALIZABLE isolation to prevent phantom reads during the stock check-and-reserve phase.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        userService.getUserById(request.getUserId());

        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new ValidationException("Shipping address is required");
        }

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        // Fetch all products once and cache them
        java.util.Map<Integer, ProductResponse> productCache = new java.util.HashMap<>();
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            productCache.put(itemReq.getProductId(), product);
        }

        // Validate products and inventory
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productCache.get(itemReq.getProductId());
            if (!product.isActive()) {
                throw new ValidationException("Product " + product.getProductName() + " is not active");
            }
            if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getProductName());
            }
            // Calculate total amount from actual product prices
            java.math.BigDecimal itemTotal = java.math.BigDecimal.valueOf(product.getPrice())
                    .multiply(java.math.BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // Reserve inventory
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
        }

        // Create the order
        Order order = orderMapper.toOrder(request);
        order.setTotalAmount(totalAmount);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        
        // Set default payment method if not provided
        if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod("CASH");
        }
        
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Save order first to generate order ID and handle common fields
        Order createdOrder = orderRepository.save(order);

        // Create order items using cached product prices and link to the saved order
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productCache.get(itemReq.getProductId());
            java.math.BigDecimal unitPrice = java.math.BigDecimal.valueOf(product.getPrice());
            java.math.BigDecimal subtotal = unitPrice.multiply(java.math.BigDecimal.valueOf(itemReq.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                    .orderId(createdOrder.getOrderId())
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Return refreshed order with items
        return getOrderById(createdOrder.getOrderId());
    }

    @Override
    public Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.withFilters(userId, filter);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
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
    public Page<OrderResponse> getOrdersByUserPaginated(Integer userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
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
    public Page<OrderResponse> getOrdersByStatusPaginated(OrderStatus status, Pageable pageable) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
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

    @Override
    public Page<OrderResponse> getOrdersByDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        Page<Order> orderPage = orderRepository.findByDateRange(startDate, endDate, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public Page<OrderResponse> getAllOrdersPaginated(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    /**
     * STATE MACHINE PATTERN EXAMPLE
     * <p>
     * Order status transitions follow a specific workflow:
     * PENDING -> PROCESSING -> SHIPPED -> DELIVERED
     * -> CANCELLED (from PENDING or PROCESSING only)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        return convertToResponse(updatedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse confirmOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "confirm");
        }

        return updateOrderStatus(orderId, OrderStatus.PROCESSING);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse shipOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "ship");
        }

        return updateOrderStatus(orderId, OrderStatus.SHIPPED);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse completeOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "complete");
        }

        return updateOrderStatus(orderId, OrderStatus.DELIVERED);
    }

    /**
     * COMPLEX ROLLBACK SCENARIO - Order Cancellation
     * <p>
     * When cancelling an order, we must:
     * 1. Validate order can be canceled
     * 2. Release reserved inventory back to stock (Atomic operation)
     * 3. Update order status
     * <p>
     * If inventory release fails for any item, the status update will be rolled back,
     * ensuring the order doesn't appear cancelled while stock is still missing.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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

        Order cancelledOrder = orderRepository.save(order);

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
            orderItemRepository.deleteById(item.getOrderItemId());
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
                    .unitPrice(new java.math.BigDecimal(itemReq.getPrice().toString()))
                    .subtotal(new java.math.BigDecimal(itemReq.getQuantity() * itemReq.getPrice()))
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Update total amount
        order.setTotalAmount(new java.math.BigDecimal(newTotal));
    }

    order.setUpdatedAt(LocalDateTime.now());
    Order updatedOrder = orderRepository.save(order);
    
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

        orderRepository.deleteById(orderId);
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
            // Using the JPA relationship with @BatchSize for efficiency 
            // instead of calling userService in a loop (N+1)
            User user = order.getUser();
            if (user != null) {
                userName = user.getFirstName() + " " + user.getLastName();
            }
        } catch (Exception e) {
            // Fallback to Unknown if user cannot be fetched
        }

        // Using order.getOrderItems() instead of repository call
        // This leverages @BatchSize(size = 20) added to the collection
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            try {
                // Leveraging the lazy-loaded Product association within OrderItem
                // Hibernate will batch fetch these products too due to @BatchSize on Product class
                Product product = item.getProduct();
                String productName = product != null ? product.getProductName() : "Unknown Product";
                return orderItemMapper.toOrderItemResponse(item, productName);
            } catch (Exception e) {
                return orderItemMapper.toOrderItemResponse(item, "Unknown Product");
            }
        }).collect(Collectors.toList());

        return orderMapper.toOrderResponse(order, userName, itemResponses);
    }

    /**
     * PAYMENT WORKFLOW DEMONSTRATION
     * <p>
     * This method simulates a payment confirmation workflow.
     * 1. Updates payment status Atomically.
     * 2. Transitions order status to PROCESSING.
     * 3. Uses REQUIRED propagation to ensure both updates happen in one transaction.
     * <p>
     * If transitioning the order status fails (e.g., business rule violation), 
     * the payment status update will also be rolled back.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse processPayment(Integer orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Order is already paid");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(orderId, "CANCELLED", "process payment");
        }

        // Simulate payment gateway interaction or logging the transaction ID
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Save initial state before transition
        orderRepository.save(order);

        // Chain status update - this happens within the same transaction context
        return confirmOrder(orderId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse simulatePayment(Integer orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Simulation: Order " + orderId + " is already paid.");
        }

        // 1. Update Payment Status (First step of the transaction)
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 2. Demonstration of Rollback
        // Trigger failure if transactionId starts with "FAIL-"
        if (transactionId != null && transactionId.startsWith("FAIL-")) {
            throw new RuntimeException("Simulated payment gateway failure for order " + orderId);
        }

        // 3. Update Order Status (Final step) - transition to PROCESSING
        return confirmOrder(orderId);
    }
}
