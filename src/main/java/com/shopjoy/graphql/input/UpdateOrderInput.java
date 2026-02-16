package com.shopjoy.graphql.input;

import java.util.List;

public record UpdateOrderInput(
    String status,
    String paymentStatus,
    String shippingAddress,
    String paymentMethod,
    String notes,
    List<UpdateOrderItemInput> orderItems
) {}
