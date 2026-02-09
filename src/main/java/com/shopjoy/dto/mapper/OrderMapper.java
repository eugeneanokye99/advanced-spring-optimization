package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.Order;
import java.util.List;

/**
 * Mapper for Order entity and DTOs.
 */
public class OrderMapper {

    /**
     * To order.
     *
     * @param request the request
     * @return the order
     */
    public static Order toOrder(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(request.getNotes());
        order.setTotalAmount(request.getTotalAmount());
        order.setPaymentMethod(request.getPaymentMethod());

        return order;
    }

    /**
     * To order response.
     *
     * @param order the order
     * @return the order response
     */
    public static OrderResponse toOrderResponse(Order order, String userName, List<OrderItemResponse> orderItems) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .userName(userName)
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .notes(order.getNotes())
                .orderItems(orderItems)
                .createdAt(order.getCreatedAt())
                .build();
    }

}
