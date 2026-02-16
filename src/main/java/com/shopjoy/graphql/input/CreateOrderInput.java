package com.shopjoy.graphql.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderInput(
        @NotNull(message = "User ID is required")
        Long userId,

        String shippingAddress,
        String notes,
        String paymentMethod,
        BigDecimal totalAmount,
        
        @NotEmpty(message = "Order items cannot be empty")
        @Valid
        List<OrderItemInput> orderItems
) {}
