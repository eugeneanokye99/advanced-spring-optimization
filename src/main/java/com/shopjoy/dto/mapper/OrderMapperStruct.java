package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.Order;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Order entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapperStruct.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapperStruct {

    /**
     * Maps CreateOrderRequest to Order entity.
     * 
     * @param request the create order request
     * @return the mapped order entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toOrder(CreateOrderRequest request);

    /**
     * Maps Order entity to OrderResponse with additional data.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(order.getUser() != null ? order.getUser().getFirstName() + \" \" + order.getUser().getLastName() : \"Unknown User\")")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse toOrderResponse(Order order);

    /**
     * Maps Order entity to OrderResponse with explicit additional data.
     */
    @Mapping(target = "userId", source = "order.user.id")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse toOrderResponse(Order order, String userName, List<OrderItemResponse> orderItems);

    /**
     * Updates existing Order entity from CreateOrderRequest.
     * 
     * @param request the update request
     * @param order the existing order to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateOrderFromRequest(CreateOrderRequest request, @MappingTarget Order order);
}