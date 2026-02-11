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
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        
        validateCategoryData(category);
        
        if (category.getParentCategoryId() != null) {
            categoryRepository.findById(category.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", category.getParentCategoryId()));
        }
        
        category.setCreatedAt(LocalDateTime.now());
        Category createdCategory = categoryRepository.save(category);
        
        return categoryMapper.toCategoryResponse(createdCategory);
    }
    
    @Override
    public CategoryResponse getCategoryById(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return categoryMapper.toCategoryResponse(category);
    }
    
    @Override
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
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findTopLevelCategories().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> getSubcategories(Integer parentCategoryId) {
        if (parentCategoryId == null) {
            throw new ValidationException("Parent category ID cannot be null");
        }
        return categoryRepository.findSubcategories(parentCategoryId).stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasSubcategories(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException("Category ID cannot be null");
        }
        return categoryRepository.hasSubcategories(categoryId);
    }
    
    @Override
    @Transactional()
    public CategoryResponse updateCategory(Integer categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        categoryMapper.updateCategoryFromRequest(request, category);
        validateCategoryData(category);
        
        if (category.getParentCategoryId() != null) {
            if (category.getParentCategoryId().equals(category.getCategoryId())) {
                throw new BusinessException("Category cannot be its own parent");
            }
            
            if (wouldCreateCircularReference(category.getCategoryId(), category.getParentCategoryId())) {
                throw new BusinessException("Moving category would create circular reference");
            }
        }
        
        Category updatedCategory = categoryRepository.save(category);
        
        return categoryMapper.toCategoryResponse(updatedCategory);
    }
    
    @Override
    @Transactional()
    public void deleteCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        if (hasSubcategories(categoryId)) {
            throw new BusinessException("Cannot delete category with subcategories");
        }
        
        long productCount = productService.getProductCountByCategory(categoryId);
        if (productCount > 0) {
            throw new BusinessException(
                    String.format("Cannot delete category with %d products", productCount));
        }
        
        categoryRepository.deleteById(categoryId);
    }
    
    @Override
    @Transactional()
    public CategoryResponse moveCategory(Integer categoryId, Integer newParentId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        if (newParentId != null) {
            if (newParentId.equals(categoryId)) {
                throw new BusinessException("Category cannot be its own parent");
            }
            
            categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", newParentId));
            
            if (wouldCreateCircularReference(categoryId, newParentId)) {
                throw new BusinessException("Moving category would create circular reference");
            }
        }
        
        category.setParentCategoryId(newParentId);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }
    
    private boolean wouldCreateCircularReference(Integer categoryId, Integer newParentId) {
        Integer currentId = newParentId;
        while (currentId != null) {
            if (currentId.equals(categoryId)) {
                return true;
            }
            Category parent = categoryRepository.findById(currentId).orElse(null);
            currentId = (parent != null) ? parent.getParentCategoryId() : null;
        }
        return false;
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
