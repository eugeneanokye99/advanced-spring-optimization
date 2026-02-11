package com.shopjoy.service;

import com.shopjoy.dto.request.CreateCategoryRequest;
import com.shopjoy.dto.request.UpdateCategoryRequest;
import com.shopjoy.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for Category management operations.
 * DTO-FIRST DESIGN: All methods accept and return DTOs, not entities.
 * Service layer handles all DTO ↔ Entity mapping internally.
 */
public interface CategoryService {
    
    CategoryResponse createCategory(CreateCategoryRequest request);
    
    CategoryResponse getCategoryById(Integer categoryId);
    
    /**
     * Retrieves multiple categories by their IDs in a single batch.
     * Useful for optimizing GraphQL N+1 queries.
     * 
     * @param categoryIds list of category IDs to retrieve
     * @return list of category response DTOs
     */
    List<CategoryResponse> getCategoriesByIds(List<Integer> categoryIds);
    
    List<CategoryResponse> getAllCategories();
    
    List<CategoryResponse> getTopLevelCategories();
    
    List<CategoryResponse> getSubcategories(Integer parentCategoryId);
    
    boolean hasSubcategories(Integer categoryId);
    
    CategoryResponse updateCategory(Integer categoryId, UpdateCategoryRequest request);
    
    void deleteCategory(Integer categoryId);
    
    CategoryResponse moveCategory(Integer categoryId, Integer newParentId);
}
