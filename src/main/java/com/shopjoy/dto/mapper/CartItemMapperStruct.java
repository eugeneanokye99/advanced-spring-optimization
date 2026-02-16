package com.shopjoy.dto.mapper;

import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for CartItem entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface CartItemMapperStruct {

    /**
     * Convert CartItem entity to CartItemResponse with additional product information.
     *
     * @param cartItem the cart item entity
     * @param productName the product name
     * @param price the product price
     * @return the cart item response
     */
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "productPrice", source = "product.price")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    /**
     * Convert CartItem entity to CartItemResponse with manual product information.
     */
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productPrice", source = "price")
    CartItemResponse toCartItemResponse(CartItem cartItem, String productName, double price);
}