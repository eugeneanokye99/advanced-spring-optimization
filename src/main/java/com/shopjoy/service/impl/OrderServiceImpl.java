package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.OrderMapperStruct;
import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
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
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.UserService;
import com.shopjoy.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
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
    private final EmailService emailService;

    @Override
    @Async("appTaskExecutor")
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByUser", key = "#request.userId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "ordersByStatus", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "pendingOrders", allEntries = true, cacheManager = "mediumCacheManager")
    })
    public CompletableFuture<OrderResponse> createOrder(CreateOrderRequest request) {
        long startTime = System.nanoTime();

        try {
            log.info("Starting async order creation for user {}", request.getUserId());

            validateCreateOrderRequest(request);

            Map<Integer, ProductResponse> productsById = fetchProducts(request.getOrderItems());

            BigDecimal totalAmount = validateStockAndCalculateTotal(request.getOrderItems(), productsById);

            reserveInventory(request.getOrderItems());

            Order createdOrder = buildAndSaveOrder(request, totalAmount);

            createAndSaveOrderItems(createdOrder, request.getOrderItems(), productsById);

            String username = userService.getUserById(request.getUserId()).getUsername();
            String userEmail = userService.getUserById(request.getUserId()).getEmail();

            securityAuditService.logEvent(
                username,
                SecurityEventType.ORDER_CREATED,
                String.format("Order #%d created with %d items, total amount: $%.2f",
                    createdOrder.getId(),
                    request.getOrderItems().size(),
                    totalAmount),
                true
            );

            Order refreshedOrder = orderRepository.findById(createdOrder.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", createdOrder.getId()));

            OrderResponse orderResponse = orderMapper.toOrderResponse(refreshedOrder);

            emailService.sendOrderConfirmationEmail(orderResponse, userEmail);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Async order creation completed in {}ms. Order ID: {}", executionTimeMs, orderResponse.getId());

            return CompletableFuture.completedFuture(orderResponse);

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Async order creation failed after {}ms: {}", executionTimeMs, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }


    @Override
    @Async("appTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CompletableFuture<OrderResponse> processPayment(Integer orderId, String transactionId) {
        long startTime = System.nanoTime();

        try {
            log.info("Starting async payment processing for order {}", orderId);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                throw new ValidationException("Order is already paid");
            }

            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new InvalidOrderStateException(orderId, "CANCELLED", "process payment");
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.PROCESSING);
            order.setUpdatedAt(LocalDateTime.now());

            Order updatedOrder = orderRepository.save(order);
            OrderResponse orderResponse = orderMapper.toOrderResponse(updatedOrder);

            String userEmail = userService.getUserById(order.getUser().getId()).getEmail();
            emailService.sendPaymentConfirmationEmail(orderResponse, userEmail);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Async payment processing completed in {}ms. Order ID: {}", executionTimeMs, orderId);

            return CompletableFuture.completedFuture(orderResponse);

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Async payment processing failed after {}ms: {}", executionTimeMs, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
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

    @Override
    @Cacheable(value = "order", key = "#orderId", unless = "#result == null", cacheManager = "mediumCacheManager")
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Cacheable(value = "orders", cacheManager = "mediumCacheManager")
    public Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "order", key = "#orderId", cacheManager = "mediumCacheManager")
    })
    public OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "order", key = "#orderId", cacheManager = "mediumCacheManager")
    })
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
        orderItems.forEach(item ->
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity())
        );

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        String userEmail = userService.getUserById(order.getUser().getId()).getEmail();
        emailService.sendOrderCancellationEmail(orderId, userEmail);

        return orderMapper.toOrderResponse(cancelledOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "order", key = "#orderId", cacheManager = "mediumCacheManager")
    })
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "order", key = "#orderId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "orders", allEntries = true, cacheManager = "mediumCacheManager")
    })
    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(),
                "delete (can only delete PENDING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item ->
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity())
        );

        orderRepository.deleteById(orderId);
    }

}

