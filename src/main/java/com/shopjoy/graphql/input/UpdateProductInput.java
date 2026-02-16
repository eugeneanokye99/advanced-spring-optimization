package com.shopjoy.graphql.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateProductInput(
        String name,
        String description,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @DecimalMin(value = "0.0", inclusive = false, message = "Cost price must be greater than 0")
        BigDecimal costPrice,

        String brand,
        String imageUrl,
        Boolean isActive,
        
        @Min(value = 0, message = "Stock quantity must be non-negative")
        Integer stockQuantity,
        
        Long categoryId
) {}
