package com.shopjoy.controller;

import com.shopjoy.dto.request.CreateCategoryRequest;
import com.shopjoy.dto.request.UpdateCategoryRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Category controller.
 */
@Tag(name = "Category Management", description = "APIs for managing product categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Instantiates a new Category controller.
     *
     * @param categoryService the category service
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Create category response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Create a new category",
            description = "Creates a new product category"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Category with this name already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    /**
     * Gets category by id.
     *
     * @param id the id
     * @return the category by id
     */
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a category's details by its unique identifier"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Category unique identifier", required = true, example = "1")
            @PathVariable Integer id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Category retrieved successfully"));
    }

    /**
     * Gets all categories.
     *
     * @return the all categories
     */
    @Operation(
            summary = "Get all categories",
            description = "Retrieves a complete list of all categories in the system"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(response, "Categories retrieved successfully"));
    }

    /**
     * Update category response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Update category",
            description = "Updates an existing category's details including name and description"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category unique identifier", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }

    /**
     * Delete category response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @Operation(
            summary = "Delete category",
            description = "Permanently deletes a category from the system (fails if it has products)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Category deleted successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete category with products",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category unique identifier", required = true, example = "1")
            @PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
