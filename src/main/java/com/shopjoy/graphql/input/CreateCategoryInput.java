package com.shopjoy.graphql.input;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryInput(
        @NotBlank(message = "Category name is required")
        String name,
        
        String description
) {}
