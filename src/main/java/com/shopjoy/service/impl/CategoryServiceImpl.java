package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.CategoryMapperStruct;
import com.shopjoy.dto.request.CreateCategoryRequest;
import com.shopjoy.dto.request.UpdateCategoryRequest;
import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.entity.Category;
import com.shopjoy.exception.BusinessException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.CategoryRepository;
import com.shopjoy.service.CategoryService;
import com.shopjoy.service.ProductService;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Category service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final CategoryMapperStruct categoryMapper;
    
    @Override
    @Transactional()
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        
        validateCategoryData(category);
        
        category.setCreatedAt(LocalDateTime.now());
        Category createdCategory = categoryRepository.save(category);
        
        return categoryMapper.toCategoryResponse(createdCategory);
    }
    
    @Override
    @Cacheable(value = "category", key = "#categoryId", unless = "#result == null")
    public CategoryResponse getCategoryById(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return categoryMapper.toCategoryResponse(category);
    }
    
    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getCategoriesByIds(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Integer> distinctIds = categoryIds.stream()
                .distinct()
                .filter(java.util.Objects::nonNull)
                .toList();
        
        List<Category> categories = categoryRepository.findAllById(distinctIds);
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }


    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional()
    @Caching(
        put = { @CachePut(value = "category", key = "#categoryId") },
        evict = { @CacheEvict(value = "categories", allEntries = true) }
    )
    public CategoryResponse updateCategory(Integer categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        categoryMapper.updateCategoryFromRequest(request, category);
        validateCategoryData(category);
        
        Category updatedCategory = categoryRepository.save(category);
        
        return categoryMapper.toCategoryResponse(updatedCategory);
    }
    
    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "category", key = "#categoryId"),
        @CacheEvict(value = "categories", allEntries = true)
    })
    public void deleteCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        long productCount = productService.getProductCountByCategory(categoryId);
        if (productCount > 0) {
            throw new BusinessException(
                    String.format("Cannot delete category with %d products", productCount));
        }
        
        categoryRepository.deleteById(categoryId);
    }
    
    private void validateCategoryData(Category category) {
        if (category == null) {
            throw new ValidationException("Category data cannot be null");
        }
        
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new ValidationException("categoryName", "must not be empty");
        }
        
        if (category.getCategoryName().length() > 100) {
            throw new ValidationException("categoryName", "must not exceed 100 characters");
        }
    }
}
