package com.shopjoy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request for updating category details - all fields are optional")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {
    
    @Schema(description = "Updated category name", example = "Gaming Laptops")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String categoryName;
    
    @Schema(description = "Updated category description", example = "High-performance laptops for gaming and creative work")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
