package com.shopjoy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request for updating an existing product - all fields are optional")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @Schema(description = "Updated product name", example = "Dell XPS 15 Laptop (2024 Edition)")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String productName;
    
    @Schema(description = "Updated product description", example = "High-performance laptop with 12th Gen Intel Core i7")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Schema(description = "Updated selling price", example = "1399.99")
    @Positive(message = "Price must be positive")
    private Double price;
    
    @Schema(description = "Updated cost price", example = "999.99")
    @Positive(message = "Cost price must be positive")
    private Double costPrice;
    
    @Schema(description = "Updated brand name", example = "Dell")
    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;
    
    @Schema(description = "Updated image URL", example = "https://example.com/images/dell-xps15-2024.jpg")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
    
    @Schema(description = "Updated active status", example = "false")
    private Boolean isActive;

    @Schema(description = "Updated category ID", example = "1")
    private Integer categoryId;
}
