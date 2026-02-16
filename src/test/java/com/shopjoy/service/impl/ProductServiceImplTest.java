package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.ProductMapperStruct;
import com.shopjoy.dto.request.CreateProductRequest;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Category;
import com.shopjoy.entity.Product;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.CategoryRepository;
import com.shopjoy.repository.InventoryRepository;
import com.shopjoy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapperStruct productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponse productResponse;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1)
                .productName("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .costPrice(BigDecimal.valueOf(50.0))
                .active(true)
                .category(Category.builder().id(1).build())
                .build();

        productResponse = new ProductResponse();
        productResponse.setProductId(1);
        productResponse.setProductName("Test Product");
        productResponse.setPrice(100.0);

        createRequest = CreateProductRequest.builder()
                .productName("Test Product")
                .price(100.0)
                .costPrice(50.0)
                .categoryId(1)
                .initialStock(10)
                .build();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnResponse() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.getProductById(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        verify(productRepository).findById(1);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProduct_ShouldSaveProductAndInventory() {
        // Arrange
        Category category = new Category();
        category.setId(1);

        when(productMapper.toProduct(createRequest)).thenReturn(product);
        when(categoryRepository.getReferenceById(1)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.createProduct(createRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(inventoryRepository).save(any());
        verify(categoryRepository).getReferenceById(1);
    }
}
