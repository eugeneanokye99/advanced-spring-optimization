package com.shopjoy.graphql.input;

import java.util.List;

public record UpdateOrderInput(
    String shippingAddress,
    String paymentMethod,
    String notes,
    List<UpdateOrderItemInput> orderItems
) {}
