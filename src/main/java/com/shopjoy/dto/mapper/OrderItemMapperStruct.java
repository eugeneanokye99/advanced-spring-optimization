package com.shopjoy.dto.mapper;

import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.OrderItem;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

/**
 * MapStruct mapper for OrderItem entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring", // Generate Spring component
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface OrderItemMapperStruct {

    /**
     * Maps OrderItem entity to OrderItemResponse with product info.
     * 
     * @param item the order item entity
     * @param product the product response to include
     * @return the mapped order item response
     */
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "categoryName", source = "product.categoryName")
    @Mapping(target = "productId", source = "item.productId")
    OrderItemResponse toOrderItemResponse(OrderItem item, ProductResponse product);

    /**
     * Maps OrderItem entity to OrderItemResponse with product name.
     * 
     * @param item the order item entity
     * @param productName the product name to include
     * @return the mapped order item response
     */
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "categoryName", source = "item.product.category.categoryName")
    OrderItemResponse toOrderItemResponse(OrderItem item, String productName);

    /**
     * Maps OrderItem entity to OrderItemResponse without product name.
     * 
     * @param item the order item entity
     * @return the mapped order item response
     */
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "categoryName", source = "product.category.categoryName")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    /**
     * Maps OrderItem entity to OrderItemResponse and calculates product name from productId.
     * Custom method for when product name needs to be derived.
     * 
     * @param item the order item entity
     * @return the mapped order item response with derived product name
     */
    @AfterMapping
    default void enhanceWithProductInfo(@MappingTarget OrderItemResponse.OrderItemResponseBuilder target, OrderItem source) {
        // This can be enhanced with product service lookup if needed
        if (target.build().getProductName() == null) {
            target.productName("Product #" + source.getProductId());
        }
    }
}