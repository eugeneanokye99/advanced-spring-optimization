package com.shopjoy.service;

import com.shopjoy.dto.filter.ProductFilter;
import com.shopjoy.dto.request.CreateProductRequest;
import com.shopjoy.dto.request.UpdateProductRequest;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * The interface Product service.
 */
public interface ProductService {

    /**
     * Create product product response.
     *
     * @param request the request
     * @return the product response
     */
    ProductResponse createProduct(CreateProductRequest request);

    /**
     * Gets product by id.
     *
     * @param productId the product id
     * @return the product by id
     */
    ProductResponse getProductById(Integer productId);

    /**
     * Retrieves multiple products by their IDs in a single batch.
     * Useful for optimizing GraphQL N+1 queries.
     * 
     * @param productIds list of product IDs to retrieve
     * @return list of product response DTOs
     */
    List<ProductResponse> getProductsByIds(List<Integer> productIds);

    /**
     * Gets all products.
     *
     * @return the all products
     */
    List<ProductResponse> getAllProducts();

    /**
     * Gets active products.
     *
     * @return the active products
     */
    List<ProductResponse> getActiveProducts();

    /**
     * Gets products by category.
     *
     * @param categoryId the category id
     * @return the products by category
     */
    List<ProductResponse> getProductsByCategory(Integer categoryId);

    /**
     * Retrieves products for multiple categories in a single batch.
     * 
     * @param categoryIds list of category IDs
     * @return list of product response DTOs
     */
    List<ProductResponse> getProductsByCategories(List<Integer> categoryIds);

    /**
     * Search products by name list.
     *
     * @param keyword the keyword
     * @return the list
     */
    List<ProductResponse> searchProductsByName(String keyword);

    /**
     * Gets products by price range.
     *
     * @param minPrice the min price
     * @param maxPrice the max price
     * @return the products by price range
     */
    List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Update product product response.
     *
     * @param productId the product id
     * @param request   the request
     * @return the product response
     */
    ProductResponse updateProduct(Integer productId, UpdateProductRequest request);

    /**
     * Update product price product response.
     *
     * @param productId the product id
     * @param newPrice  the new price
     * @return the product response
     */
    ProductResponse updateProductPrice(Integer productId, double newPrice);

    /**
     * Activate product product response.
     *
     * @param productId the product id
     * @return the product response
     */
    ProductResponse activateProduct(Integer productId);

    /**
     * Deactivate product product response.
     *
     * @param productId the product id
     * @return the product response
     */
    ProductResponse deactivateProduct(Integer productId);

    /**
     * Delete product.
     *
     * @param productId the product id
     */
    void deleteProduct(Integer productId);

    /**
     * Gets total product count.
     *
     * @return the total product count
     */
    long getTotalProductCount();

    /**
     * Gets product count by category.
     *
     * @param categoryId the category id
     * @return the product count by category
     */
    long getProductCountByCategory(Integer categoryId);

    /**
     * Gets products paginated.
     *
     * @param pageable      the pageable
     * @param sortBy        the sort by
     * @param sortDirection the sort direction
     * @return the products paginated
     */
    Page<ProductResponse> getProductsPaginated(Pageable pageable, String sortBy, String sortDirection);

    /**
     * Search products paginated page.
     *
     * @param searchTerm the search term
     * @param pageable   the pageable
     * @return the page
     */
    Page<ProductResponse> searchProductsPaginated(String searchTerm, Pageable pageable);

    /**
     * Gets products with filters.
     *
     * @param filter        the filter
     * @param pageable      the pageable
     * @param sortBy        the sort by
     * @param sortDirection the sort direction
     * @return the products with filters
     */
    Page<ProductResponse> getProductsWithFilters(ProductFilter filter, Pageable pageable, String sortBy,
            String sortDirection);

    Product searchById(Integer id);

    List<ProductResponse> getRecentlyAddedProducts(int limit);
}
