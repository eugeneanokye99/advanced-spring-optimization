package com.shopjoy.dto.mapper;

import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Inventory entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapperStruct {

    /**
     * Convert Inventory entity to InventoryResponse with product name.
     *
     * @param inventory the inventory entity
     * @param productName the product name
     * @return the inventory response
     */
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "stockQuantity", source = "inventory.quantityInStock")
    InventoryResponse toInventoryResponse(Inventory inventory);

    /**
     * Convert Inventory entity to InventoryResponse with manual product name.
     */
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "stockQuantity", source = "inventory.quantityInStock")
    InventoryResponse toInventoryResponse(Inventory inventory, String productName);
}