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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private ProductResponse convertToResponse(Product product) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(com.shopjoy.entity.Category::getCategoryName)
                .orElse("Unknown");
        int stock = inventoryRepository.findByProductId(product.getProductId())
                .map(com.shopjoy.entity.Inventory::getQuantityInStock)
                .orElse(0);
        return productMapper.toProductResponse(product, categoryName, stock);
    }

    /**
     * Batch converts products to responses, fetching categories and inventories in bulk.
     */
    private List<ProductResponse> convertToResponses(List<Product> products) {
        if (products.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Fetch all categories in one query
        List<Integer> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(
                        com.shopjoy.entity.Category::getCategoryId,
                        com.shopjoy.entity.Category::getCategoryName));
        
        // Fetch all inventories in one query
        List<Integer> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
        Map<Integer, Integer> inventoryMap = inventoryRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(
                        com.shopjoy.entity.Inventory::getProductId,
                        com.shopjoy.entity.Inventory::getQuantityInStock));
        
        return products.stream()
                .map(product -> productMapper.toProductResponse(
                        product,
                        categoryMap.getOrDefault(product.getCategoryId(), "Unknown"),
                        inventoryMap.getOrDefault(product.getProductId(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = productMapper.toProduct(request);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setActive(true);

        validateProductData(product);

        Product createdProduct = productRepository.save(product);

        // Create initial inventory entry
        com.shopjoy.entity.Inventory inventory = new com.shopjoy.entity.Inventory();
        inventory.setProductId(createdProduct.getProductId());
        inventory.setQuantityInStock(request.getInitialStock() != null ? request.getInitialStock() : 0);
        inventory.setReorderLevel(5);
        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        return convertToResponse(createdProduct);
    }

    @Override
    @Cacheable(value = "product", key = "#productId", unless = "#result == null")
    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return convertToResponse(product);
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getProductsByIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Integer> distinctIds = productIds.stream()
                .distinct()
                .filter(java.util.Objects::nonNull)
                .toList();
        
        List<Product> products = productRepository.findAllById(distinctIds);
        return convertToResponses(products);
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        return convertToResponses(productRepository.findAll());
    }

    @Override
    @Cacheable(value = "activeProducts")
    public List<ProductResponse> getActiveProducts() {
        List<Product> activeProducts = productRepository.findAll().stream()
                .filter(Product::isActive)
                .collect(Collectors.toList());
        return convertToResponses(activeProducts);
    }

    @Override
    @Cacheable(value = "productsByCategory", key = "#categoryId")
    public List<ProductResponse> getProductsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new ValidationException("Category ID cannot be null");
        }
        return convertToResponses(productRepository.findByCategoryId(categoryId));
    }

    @Override
    @Cacheable(value = "productsByCategory")
    public List<ProductResponse> getProductsByCategories(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return convertToResponses(productRepository.findByCategoryIdIn(categoryIds));
    }
 

    @Override
    public List<ProductResponse> searchProductsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }
        return convertToResponses(productRepository.findByProductNameContainingIgnoreCase(keyword));
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0) {
            throw new ValidationException("Minimum price cannot be negative");
        }
        if (maxPrice < minPrice) {
            throw new ValidationException("Maximum price must be greater than or equal to minimum price");
        }
        return convertToResponses(productRepository.findByPriceBetween(minPrice, maxPrice));
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
    })
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productMapper.updateProductFromRequest(request, existingProduct);
        existingProduct.setUpdatedAt(LocalDateTime.now());

        validateProductData(existingProduct);

        Product updatedProduct = productRepository.save(existingProduct);

        return convertToResponse(updatedProduct);
    }

    @Override
    @Transactional()
    @Auditable(action = "UPDATE_PRICE", description = "Updating product price")
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
    })
    public ProductResponse updateProductPrice(Integer productId, double newPrice) {
        if (newPrice < 0) {
            throw new ValidationException("price", "must not be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setPrice(new java.math.BigDecimal(newPrice));
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return convertToResponse(updatedProduct);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
    })
    public ProductResponse activateProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(productRepository.save(product));
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
    })
    public ProductResponse deactivateProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(productRepository.save(product));
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "product", key = "#productId"),
        @CacheEvict(value = {"products", "activeProducts", "productsByCategory"}, allEntries = true)
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
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponse> responseList = convertToResponses(productPage.getContent());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Page<ProductResponse> searchProductsPaginated(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword cannot be empty");
        }

        Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        List<ProductResponse> responseList = convertToResponses(productPage.getContent());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Page<ProductResponse> getProductsWithFilters(ProductFilter filter, Pageable pageable, String sortBy,
            String sortDirection) {
        // Handle null filter by creating an empty one
        if (filter == null) {
            filter = ProductFilter.builder().build();
        }
        
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null &&
                filter.getMinPrice() > filter.getMaxPrice()) {
            throw new ValidationException("minPrice", "must be less than or equal to maxPrice");
        }

        // Default database sorting/pagination
        Page<Product> productPage = productRepository.findWithFilters(
                filter.getSearchTerm(),
                filter.getCategoryId(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getBrand(),
                filter.getActive(),
                pageable);

        List<ProductResponse> responseList = convertToResponses(productPage.getContent());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }

    @Override
    public Product searchById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProductResponse> getRecentlyAddedProducts(int limit) {

        return convertToResponses(productRepository.findRecentlyAdded(PageRequest.of(0, limit)));
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

        if (product.getCategoryId() <= 0) {
            throw new ValidationException("categoryId", "must be a valid category ID");
        }

        if (product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("price", "must not be negative");
        }

        if (product.getCostPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("costPrice", "must not be negative");
        }


    }
}
