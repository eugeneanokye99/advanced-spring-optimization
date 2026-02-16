package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.*;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import com.shopjoy.graphql.input.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for GraphQL input types to DTO request types.
 */
@Mapper(
    componentModel = "spring",
    imports = {OrderStatus.class, PaymentStatus.class}
)
public interface GraphQLMapperStruct {

    /**
     * Convert CreateUserInput to CreateUserRequest.
     */
    CreateUserRequest toCreateUserRequest(CreateUserInput input);

    /**
     * Convert UpdateUserInput to UpdateUserRequest.
     */
    UpdateUserRequest toUpdateUserRequest(UpdateUserInput input);

    /**
     * Convert CreateProductInput to CreateProductRequest.
     */
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "price", expression = "java(input.price() != null ? input.price().doubleValue() : null)")
    @Mapping(target = "costPrice", expression = "java(input.costPrice() != null ? input.costPrice().doubleValue() : null)")
    @Mapping(target = "initialStock", source = "stockQuantity")
    @Mapping(target = "categoryId", expression = "java(input.categoryId() != null ? input.categoryId().intValue() : null)")
    CreateProductRequest toCreateProductRequest(CreateProductInput input);

    /**
     * Convert UpdateProductInput to UpdateProductRequest.
     */
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "price", expression = "java(input.price() != null ? input.price().doubleValue() : null)")
    @Mapping(target = "costPrice", expression = "java(input.costPrice() != null ? input.costPrice().doubleValue() : null)")
    @Mapping(target = "categoryId", expression = "java(input.categoryId() != null ? input.categoryId().intValue() : null)")
    UpdateProductRequest toUpdateProductRequest(UpdateProductInput input);

    /**
     * Convert CreateCategoryInput to CreateCategoryRequest.
     */
    @Mapping(target = "categoryName", source = "name")
    @Mapping(target = "parentCategoryId", expression = "java(input.parentCategoryId() != null ? input.parentCategoryId().intValue() : null)")
    CreateCategoryRequest toCreateCategoryRequest(CreateCategoryInput input);

    /**
     * Convert UpdateCategoryInput to UpdateCategoryRequest.
     */
    @Mapping(target = "categoryName", source = "name")
    @Mapping(target = "parentCategoryId", expression = "java(input.parentCategoryId() != null ? input.parentCategoryId().intValue() : null)")
    UpdateCategoryRequest toUpdateCategoryRequest(UpdateCategoryInput input);

    /**
     * Convert CreateOrderInput to CreateOrderRequest.
     */
    @Mapping(target = "userId", expression = "java(input.userId() != null ? input.userId().intValue() : null)")
    @Mapping(target = "totalAmount", expression = "java(input.totalAmount() != null ? input.totalAmount().doubleValue() : null)")
    CreateOrderRequest toCreateOrderRequest(CreateOrderInput input);

    /**
     * Convert OrderItemInput to CreateOrderItemRequest.
     */
    @Mapping(target = "productId", expression = "java(input.productId() != null ? input.productId().intValue() : null)")
    @Mapping(target = "price", expression = "java(input.price() != null ? input.price().doubleValue() : null)")
    CreateOrderItemRequest toCreateOrderItemRequest(OrderItemInput input);

    /**
     * Convert UpdateOrderInput to UpdateOrderRequest.
     */
    @Mapping(target = "status", expression = "java(input.status() != null ? OrderStatus.valueOf(input.status()) : null)")
    @Mapping(target = "paymentStatus", expression = "java(input.paymentStatus() != null ? PaymentStatus.valueOf(input.paymentStatus()) : null)")
    UpdateOrderRequest toUpdateOrderRequest(UpdateOrderInput input);

    /**
     * Convert UpdateOrderItemInput to UpdateOrderItemRequest.
     */
    @Mapping(target = "orderItemId", expression = "java(input.orderItemId() != null ? input.orderItemId().intValue() : null)")
    @Mapping(target = "productId", expression = "java(input.productId() != null ? input.productId().intValue() : null)")
    @Mapping(target = "price", expression = "java(input.price() != null ? input.price().doubleValue() : null)")
    UpdateOrderItemRequest toUpdateOrderItemRequest(UpdateOrderItemInput input);
}