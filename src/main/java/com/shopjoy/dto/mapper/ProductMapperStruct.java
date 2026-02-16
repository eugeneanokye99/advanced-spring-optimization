package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateProductRequest;
import com.shopjoy.dto.request.UpdateProductRequest;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Product;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

/**
 * MapStruct mapper for Product entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring", // Generate Spring component
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface ProductMapperStruct {

    /**
     * Maps CreateProductRequest to Product entity.
     * 
     * @param request the create product request
     * @return the mapped product entity
     */
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", source = "isActive", defaultValue = "true")
    @Mapping(target = "costPrice", defaultValue = "0.0")
    Product toProduct(CreateProductRequest request);

    /**
     * Maps Product entity to ProductResponse without additional data.
     * 
     * @param product the product entity
     * @return the mapped product response
     */
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "stockQuantity", source = "inventory.quantityInStock")
    @Mapping(target = "active", source = "product.active")
    ProductResponse toProductResponse(Product product);

    /**
     * Maps Product entity to ProductResponse with explicit additional data.
     */
    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "stockQuantity", source = "stockQuantity")
    @Mapping(target = "active", source = "product.active")
    ProductResponse toProductResponse(Product product, String categoryName, int stockQuantity);

    /**
     * Updates existing Product entity from UpdateProductRequest.
     * Only maps non-null values from the request.
     * 
     * @param request the update request
     * @param product the existing product to update
     */
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "categoryId", ignore = true) // Usually not updated via product update
    @Mapping(target = "sku", ignore = true) // Usually not updated via product update
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", source = "isActive")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    /**
     * Custom method to handle default values during creation.
     */
    @AfterMapping
    default void setDefaults(@MappingTarget Product product, CreateProductRequest request) {
        if (product.getCostPrice() == null || product.getCostPrice().compareTo(java.math.BigDecimal.ZERO) == 0) {
            product.setCostPrice(java.math.BigDecimal.ZERO);
        }
        if (request.getIsActive() == null) {
            product.setActive(true);
        }
    }
}