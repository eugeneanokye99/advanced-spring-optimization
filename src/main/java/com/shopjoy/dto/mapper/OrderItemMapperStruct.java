package com.shopjoy.dto.mapper;

import com.shopjoy.dto.response.OrderItemResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.OrderItem;
import org.mapstruct.*;

/**
 * MapStruct mapper for OrderItem entity and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderItemMapperStruct {

    /**
     * Maps OrderItem to OrderItemResponse with product info.
     */
    @Mapping(source = "item.id", target = "id")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "categoryName", source = "product.categoryName")
    @Mapping(target = "productId", source = "item.product.id")
    OrderItemResponse toOrderItemResponse(OrderItem item, ProductResponse product);

    /**
     * Maps OrderItem to OrderItemResponse with product name.
     */
    @Mapping(source = "item.id", target = "id")
    @Mapping(target = "productId", source = "item.product.id")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "categoryName", source = "item.product.category.categoryName")
    OrderItemResponse toOrderItemResponse(OrderItem item, String productName);

    /**
     * Maps OrderItem to OrderItemResponse without product name.
     */
    @Mapping(source = "item.id", target = "id")
    @Mapping(target = "productId", source = "item.product.id")
    @Mapping(target = "productName", source = "item.product.productName")
    @Mapping(target = "categoryName", source = "item.product.category.categoryName")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    /**
     * Enhances OrderItemResponse with product info from source OrderItem.
     */
    @AfterMapping
    default void enhanceWithProductInfo(@MappingTarget OrderItemResponse.OrderItemResponseBuilder target, OrderItem source) {
        if (target.build().getProductName() == null) {
            target.productName("Product #" + source.getProduct().getId());
        }
    }
}