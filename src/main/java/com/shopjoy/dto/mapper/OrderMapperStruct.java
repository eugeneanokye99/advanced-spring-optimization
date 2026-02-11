package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.Order;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for Order entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring", // Generate Spring component
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface OrderMapperStruct {

    /**
     * Maps CreateOrderRequest to Order entity.
     * 
     * @param request the create order request
     * @return the mapped order entity
     */
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toOrder(CreateOrderRequest request);

    /**
     * Maps Order entity to OrderResponse with additional data.
     * 
     * @param order the order entity
     * @param userName the user name to include
     * @param orderItems the order items to include
     * @return the mapped order response
     */
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse toOrderResponse(Order order, String userName, List<OrderItemResponse> orderItems);

    /**
     * Maps Order entity to OrderResponse without additional data.
     * 
     * @param order the order entity
     * @return the mapped order response
     */
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    OrderResponse toOrderResponse(Order order);

    /**
     * Updates existing Order entity from CreateOrderRequest.
     * 
     * @param request the update request
     * @param order the existing order to update
     */
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateOrderFromRequest(CreateOrderRequest request, @MappingTarget Order order);
}