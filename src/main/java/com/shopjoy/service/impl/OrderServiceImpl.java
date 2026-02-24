package com.shopjoy.service.impl;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.mapper.OrderMapperStruct;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderItemRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderItem;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.OrderItemRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.specification.OrderSpecification;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.UserService;
import com.shopjoy.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Order service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderMapperStruct orderMapper;
    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByUser", key = "#request.userId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager")
    })
    public OrderResponse createOrder(CreateOrderRequest request) {
        validateCreateOrderRequest(request);
        
        Map<Integer, ProductResponse> productsById = fetchProducts(request.getOrderItems());
        
        BigDecimal totalAmount = validateStockAndCalculateTotal(request.getOrderItems(), productsById);
        
        reserveInventory(request.getOrderItems());
        
        Order createdOrder = buildAndSaveOrder(request, totalAmount);
        
        createAndSaveOrderItems(createdOrder, request.getOrderItems(), productsById);

        String username = userService.getUserById(request.getUserId()).getUsername();
        securityAuditService.logEvent(
            username,
            SecurityEventType.ORDER_CREATED,
            String.format("Order #%d created with %d items, total amount: $%.2f",
                createdOrder.getId(),
                request.getOrderItems().size(),
                totalAmount),
            true
        );

        // Refresh the order to get all relationships loaded
        Order refreshedOrder = orderRepository.findById(createdOrder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", createdOrder.getId()));

        return orderMapper.toOrderResponse(refreshedOrder);
    }

    @Override
    @Cacheable(value = "orders", cacheManager = "mediumCacheManager")
    public Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.withFilters(userId, filter);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    @Cacheable(value = "order", key = "#orderId", unless = "#result == null", cacheManager = "mediumCacheManager")
    public OrderResponse getOrderById(Integer orderId) {
        Integer orderOwnerId = orderRepository.findUserIdByOrderId(orderId);
        if (orderOwnerId == null) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(orderOwnerId)) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Cacheable(value = "ordersByUser", key = "#userId", cacheManager = "mediumCacheManager")
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByUserPaginated(Integer userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    @Cacheable(value = "ordersByStatus", key = "#status.name()", cacheManager = "mediumCacheManager")
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "ordersByStatusPaginated", key = "#status.name() + '_' + #pageable.toString()", cacheManager = "mediumCacheManager")
    public Page<OrderResponse> getOrdersByStatusPaginated(OrderStatus status, Pageable pageable) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
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
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
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
        Page<Order> orderPage = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    @Cacheable(value = "orders", cacheManager = "mediumCacheManager")
    public Page<OrderResponse> getAllOrdersPaginated(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
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
    @Caching(
        put = { @CachePut(value = "order", key = "#orderId", cacheManager = "mediumCacheManager") },
        evict = {
            @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "ordersByUser", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager")
        }
    )
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can update order status");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(orderId, currentStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        String username = String.valueOf(SecurityUtil.getCurrentUsername());
        securityAuditService.logEvent(
            username,
            SecurityEventType.ORDER_STATUS_CHANGED,
            String.format("Order #%d status changed from %s to %s",
                orderId, currentStatus, newStatus),
            true
        );

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse confirmOrder(Integer orderId) {
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can confirm orders");
        }
        
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
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can ship orders");
        }
        
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
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can complete orders");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "complete");
        }

        return updateOrderStatus(orderId, OrderStatus.DELIVERED);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(
        put = { @CachePut(value = "order", key = "#orderId", cacheManager = "mediumCacheManager") },
        evict = {
            @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "ordersByUser", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager")
        }
    )
    public OrderResponse cancelOrder(Integer orderId) {
        Integer orderOwnerId = orderRepository.findUserIdByOrderId(orderId);
        if (orderOwnerId == null) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(orderOwnerId)) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(
                    orderId,
                    order.getStatus().toString(),
                    "cancel (can only cancel PENDING or PROCESSING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        orderItems.forEach(item -> 
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity())
        );

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        String username = userService.getUserById(orderOwnerId).getUsername();
        securityAuditService.logEvent(
            username,
            SecurityEventType.ORDER_CANCELLED,
            String.format("Order #%d cancelled, %d item(s) returned to inventory",
                orderId, orderItems.size()),
            true
        );

        return orderMapper.toOrderResponse(cancelledOrder);
    }

    @Override
    @Cacheable(value = "pendingOrders", cacheManager = "mediumCacheManager")
    public List<OrderResponse> getPendingOrders() {
        return getOrdersByStatus(OrderStatus.PENDING);
    }

    @Override
    @Cacheable(value = "orders", cacheManager = "mediumCacheManager")
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Caching(evict = {
                @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
                @CacheEvict(value = "ordersByUser", allEntries = true, cacheManager = "mediumCacheManager"),
                @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
                @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager")
            },
            put = {
                @CachePut(value = "order", key = "#orderId", cacheManager = "mediumCacheManager")
            }
    )
    public OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateOrderModifiable(order);

        updateOrderFields(order, request);

        if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
            updateOrderItems(order, request.getOrderItems());
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        
        return orderMapper.toOrderResponse(updatedOrder);
    }


    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "order", key = "#orderId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByUser", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager")
    })
    public void deleteOrder(Integer orderId) {
        Integer orderOwnerId = orderRepository.findUserIdByOrderId(orderId);
        if (orderOwnerId == null) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(orderOwnerId)) {
            throw new AccessDeniedException("You do not have permission to delete this order");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "delete (can only delete PENDING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item -> 
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity())
        );

        orderRepository.deleteById(orderId);
    }

    private void validateStatusTransition(Integer orderId, OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    orderId,
                    currentStatus.toString(),
                    "change status of cancelled or delivered orders");
        }

        switch (newStatus) {
            case PENDING:
                break;
            case PROCESSING:
                if (currentStatus != OrderStatus.PENDING) {
                    throw new InvalidOrderStateException(
                            orderId, currentStatus.toString(),
                            "move to PROCESSING from non-PENDING status");
                }
                break;
            case SHIPPED:
                if (currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            orderId, currentStatus.toString(),
                            "move to SHIPPED from non-PROCESSING status");
                }
                break;
            case DELIVERED:
                if (currentStatus != OrderStatus.SHIPPED) {
                    throw new InvalidOrderStateException(
                            orderId, currentStatus.toString(),
                            "move to DELIVERED from non-SHIPPED status");
                }
                break;
            case CANCELLED:
                if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            orderId, currentStatus.toString(),
                            "cancel orders that are not PENDING or PROCESSING");
                }
                break;
        }
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

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.save(order);

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

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (transactionId != null && transactionId.startsWith("FAIL-")) {
            throw new RuntimeException("Simulated payment gateway failure for order " + orderId);
        }
        return confirmOrder(orderId);
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        userService.getUserById(request.getUserId());

        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new ValidationException("Shipping address is required");
        }

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
    }

    private Map<Integer, ProductResponse> fetchProducts(List<CreateOrderItemRequest> items) {
        List<Integer> productIds = items.stream()
                .map(CreateOrderItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResponse> products = productService.getProductsByIds(productIds);

        Map<Integer, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, java.util.function.Function.identity()));

        if (productMap.size() != productIds.size()) {
            List<Integer> foundIds = new java.util.ArrayList<>(productMap.keySet());
            List<Integer> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Products", "ids", missingIds);
        }

        return productMap;
    }

    private BigDecimal validateStockAndCalculateTotal(List<CreateOrderItemRequest> items, Map<Integer, ProductResponse> productsById) {
        return items.stream()
                .map(itemReq -> {
                    ProductResponse product = productsById.get(itemReq.getProductId());
                    if (!product.isActive()) {
                        throw new ValidationException("Product " + product.getProductName() + " is not active");
                    }
                    if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                        throw new ValidationException("Insufficient stock for product: " + product.getProductName());
                    }
                    return BigDecimal.valueOf(product.getPrice())
                            .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void reserveInventory(List<CreateOrderItemRequest> items) {
        items.forEach(itemReq -> 
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity())
        );
    }

    private Order buildAndSaveOrder(CreateOrderRequest request, BigDecimal totalAmount) {
        Order order = orderMapper.toOrder(request);
        order.setUser(userRepository.getReferenceById(request.getUserId()));
        order.setTotalAmount(totalAmount);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        
        if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod("CASH");
        }
        
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    private void createAndSaveOrderItems(Order order, List<CreateOrderItemRequest> items, Map<Integer, ProductResponse> productsById) {
        List<OrderItem> orderItems = items.stream()
                .map(itemReq -> {
                    ProductResponse product = productsById.get(itemReq.getProductId());
                    BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                    return OrderItem.builder()
                            .order(order)
                            .product(productRepository.getReferenceById(itemReq.getProductId()))
                            .quantity(itemReq.getQuantity())
                            .unitPrice(unitPrice)
                            .subtotal(subtotal)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);
    }

    private void validateOrderModifiable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(order.getId(), order.getStatus().toString(), 
                "update (can only update PENDING orders)");
        }
    }

    private void updateOrderFields(Order order, UpdateOrderRequest request) {
        if (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty()) {
            order.setShippingAddress(request.getShippingAddress());
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
    }

    private void updateOrderItems(Order order, List<UpdateOrderItemRequest> newItems) {
        List<OrderItem> existingItems = orderItemRepository.findByOrderId(order.getId());
        
        existingItems.forEach(item -> 
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity())
        );

        existingItems.forEach(item -> orderItemRepository.deleteById(item.getId()));

        BigDecimal newTotal = newItems.stream()
            .map(itemReq -> {
                ProductResponse product = productService.getProductById(itemReq.getProductId());
                if (!product.isActive()) {
                    throw new ValidationException("Product " + product.getProductName() + " is not active");
                }
                if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                    throw new ValidationException("Insufficient stock for product: " + product.getProductName());
                }
                inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
                return BigDecimal.valueOf(itemReq.getPrice())
                        .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItem> newOrderItems = newItems.stream()
                .map(itemReq -> OrderItem.builder()
                        .order(order)
                        .product(productRepository.getReferenceById(itemReq.getProductId()))
                        .quantity(itemReq.getQuantity())
                        .unitPrice(BigDecimal.valueOf(itemReq.getPrice()))
                        .subtotal(BigDecimal.valueOf(itemReq.getPrice()).multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        orderItemRepository.saveAll(newOrderItems);

        order.setTotalAmount(newTotal);
    }
}