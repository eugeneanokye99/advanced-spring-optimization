package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.OrderMapperStruct;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.*;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.OrderItemRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ProductService productService;
    @Mock
    private UserService userService;
    @Mock
    private OrderMapperStruct orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Order order;
    private CreateOrderRequest createOrderRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        order = new Order();
        order.setId(1);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PENDING);

        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1);
        createOrderRequest.setShippingAddress("123 Street");
        createOrderRequest.setOrderItems(Collections.singletonList(itemRequest));

        productResponse = new ProductResponse();
        productResponse.setProductId(1);
        productResponse.setProductName("Test Product");
        productResponse.setPrice(50.0);
        productResponse.setActive(true);
    }

    @Test
    @DisplayName("Create Order - Success")
    void createOrder_Success() {
        when(userService.getUserById(1)).thenReturn(null);
        when(productService.getProductById(1)).thenReturn(productResponse);
        when(inventoryService.hasAvailableStock(1, 2)).thenReturn(true);
        
        when(orderMapper.toOrder(any(CreateOrderRequest.class))).thenReturn(order);
        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.getReferenceById(1)).thenReturn(new Product());
        
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        OrderResponse or = new OrderResponse();
        or.setOrderId(1);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(or);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1);
        verify(inventoryService).reserveStock(1, 2);
    }

    @Test
    @DisplayName("Get Order By Id - Success")
    void getOrderById_Success() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        OrderResponse or = new OrderResponse();
        or.setOrderId(1);
        when(orderMapper.toOrderResponse(order)).thenReturn(or);

        OrderResponse result = orderService.getOrderById(1);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get Order By Id - Not Found")
    void getOrderById_NotFound() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
