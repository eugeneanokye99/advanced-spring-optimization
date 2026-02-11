package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.*;
import com.shopjoy.graphql.input.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for GraphQL input types to DTO request types.
 */
@Mapper(componentModel = "spring")
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
    @Mapping(target = "categoryId", expression = "java(input.categoryId() != null ? input.categoryId().intValue() : null)")
    CreateProductRequest toCreateProductRequest(CreateProductInput input);

    /**
     * Convert UpdateProductInput to UpdateProductRequest.
     */
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "price", expression = "java(input.price() != null ? input.price().doubleValue() : null)")
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
    UpdateCategoryRequest toUpdateCategoryRequest(UpdateCategoryInput input);

    /**
     * Convert CreateOrderInput to CreateOrderRequest.
     */
    @Mapping(target = "userId", expression = "java(input.userId().intValue())")
    CreateOrderRequest toCreateOrderRequest(CreateOrderInput input);

    /**
     * Convert OrderItemInput to CreateOrderItemRequest.
     */
    @Mapping(target = "productId", expression = "java(input.productId().intValue())")
    @Mapping(target = "price", expression = "java(input.price().doubleValue())")
    CreateOrderItemRequest toCreateOrderItemRequest(OrderItemInput input);

    /**
     * Convert UpdateOrderInput to UpdateOrderRequest.
     */
    UpdateOrderRequest toUpdateOrderRequest(UpdateOrderInput input);

    /**
     * Convert UpdateOrderItemInput to UpdateOrderItemRequest.
     */
    @Mapping(target = "orderItemId", expression = "java(input.orderItemId() != null ? input.orderItemId().intValue() : null)")
    @Mapping(target = "productId", expression = "java(input.productId().intValue())")
    @Mapping(target = "price", expression = "java(input.price().doubleValue())")
    UpdateOrderItemRequest toUpdateOrderItemRequest(UpdateOrderItemInput input);
}