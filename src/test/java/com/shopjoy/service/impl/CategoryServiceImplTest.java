package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.CategoryMapperStruct;
import com.shopjoy.dto.request.CreateCategoryRequest;
import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.entity.Category;
import com.shopjoy.repository.CategoryRepository;
import com.shopjoy.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CategoryMapperStruct categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1)
                .categoryName("Electronics")
                .build();

        categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryId(1);
        categoryResponse.setCategoryName("Electronics");
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturnResponse() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isEqualTo(1);
    }

    @Test
    void createCategory_ShouldSave() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .categoryName("Electronics")
                .build();

        when(categoryMapper.toCategory(request)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }
}
