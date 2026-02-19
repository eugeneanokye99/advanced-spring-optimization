package com.shopjoy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Category response containing category details")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    
    @Schema(description = "Category unique identifier", example = "1")
    private Integer id;
    
    @Schema(description = "Category name", example = "Laptops")
    private String categoryName;
    
    @Schema(description = "Category description", example = "Portable computers for personal and business use")
    private String description;
    
    @Schema(description = "Category creation timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime createdAt;

}
