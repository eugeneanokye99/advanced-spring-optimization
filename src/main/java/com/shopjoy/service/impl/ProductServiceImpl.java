package com.shopjoy.service.impl;

import com.shopjoy.aspect.Auditable;
import com.shopjoy.dto.filter.ProductFilter;
import com.shopjoy.dto.mapper.ProductMapperStruct;
import com.shopjoy.dto.request.CreateProductRequest;
import com.shopjoy.dto.request.UpdateProductRequest;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Product;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.CategoryRepository;
import com.shopjoy.repository.InventoryRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Product service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapperStruct productMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager")
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = productMapper.toProduct(request);
        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setActive(true);

        validateProductData(product);

        Product createdProduct = productRepository.save(product);

        com.shopjoy.entity.Inventory inventory = new com.shopjoy.entity.Inventory();
        inventory.setProduct(createdProduct);
        inventory.setQuantityInStock(request.getInitialStock() != null ? request.getInitialStock() : 0);
        inventory.setReorderLevel(5);
        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        return productMapper.toProductResponse(createdProduct);
    }

    @Override
    @Cacheable(value = "product", key = "#productId", unless = "#result == null")
    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return productMapper.toProductResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Integer> distinctIds = productIds.stream()
                .distinct()
                .filter(Objects::nonNull)
                .toList();
        
        return distinctIds.stream()
                .map(this::getProductById)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllWithInventory().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "activeProducts")
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findAllWithInventory().stream()
                .filter(Product::isActive)
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "productsByCategory", key = "#categoryId")
    public List<ProductResponse> getProductsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException("Category ID cannot be null");
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "productsByCategory")
    public List<ProductResponse> getProductsByCategories(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productRepository.findByCategoryIdIn(categoryIds).stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProductsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }
        return productRepository.findByProductName(keyword).stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Minimum price cannot be negative");
        }
        if (maxPrice.compareTo(minPrice) < 0) {
            throw new ValidationException("Maximum price must be greater than or equal to minimum price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "product", key = "#productId", cacheManager = "cacheManager") },
        evict = { @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager") }
    )
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productMapper.updateProductFromRequest(request, existingProduct);
        if (request.getCategoryId() != null) {
            existingProduct.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        }
        existingProduct.setUpdatedAt(LocalDateTime.now());

        validateProductData(existingProduct);

        Product updatedProduct = productRepository.save(existingProduct);

        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_PRICE", description = "Updating product price")
    @Caching(
        put = { @CachePut(value = "product", key = "#productId", cacheManager = "cacheManager") },
        evict = { @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager") }
    )
    public ProductResponse updateProductPrice(Integer productId, double newPrice) {
        if (newPrice < 0) {
            throw new ValidationException("price", "must not be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setPrice(BigDecimal.valueOf(newPrice));
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "product", key = "#productId", cacheManager = "cacheManager") },
        evict = { @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager") }
    )
    public ProductResponse activateProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "product", key = "#productId", cacheManager = "cacheManager") },
        evict = { @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager") }
    )
    public ProductResponse deactivateProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId", cacheManager = "cacheManager"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true, cacheManager = "cacheManager")
    })
    public void deleteProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        productRepository.deleteById(productId);
    }

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }

    @Override
    public long getProductCountByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException("Category ID cannot be null");
        }
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    public Page<ProductResponse> getProductsPaginated(Pageable pageable, String sortBy, String sortDirection) {
        Page<Product> productPage = productRepository.findAllWithInventory(pageable);

        List<ProductResponse> responseList = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Page<ProductResponse> searchProductsPaginated(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }

        Page<Product> productPage = productRepository.findByProductCategory(keyword, keyword, pageable);

        List<ProductResponse> responseList = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Page<ProductResponse> getProductsWithFilters(ProductFilter filter, Pageable pageable, String sortBy,
            String sortDirection) {
        if (filter == null) {
            filter = ProductFilter.builder().build();
        }
        
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null &&
                filter.getMinPrice() > filter.getMaxPrice()) {
            throw new ValidationException("minPrice", "must be less than or equal to maxPrice");
        }

        Page<Product> productPage = productRepository.findWithFilters(
                filter.getSearchTerm(),
                filter.getCategoryId(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getBrand(),
                filter.getActive(),
                pageable);

        List<ProductResponse> responseList = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Product searchById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProductResponse> getRecentlyAddedProducts(int limit) {
        return productRepository.findRecentlyAdded(PageRequest.of(0, limit)).stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    private void validateProductData(Product product) {
        if (product == null) {
            throw new ValidationException("Product data cannot be null");
        }

        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new ValidationException("productName", "must not be empty");
        }

        if (product.getProductName().length() > 200) {
            throw new ValidationException("productName", "must not exceed 200 characters");
        }

        if (product.getCategory() == null || product.getCategory().getId() <= 0) {
            throw new ValidationException("categoryId", "must be a valid category ID");
        }

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("price", "must not be negative");
        }

        if (product.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("costPrice", "must not be negative");
        }
    }
}

