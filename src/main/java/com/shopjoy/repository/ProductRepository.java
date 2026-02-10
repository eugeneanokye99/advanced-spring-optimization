package com.shopjoy.repository;

import com.shopjoy.dto.filter.ProductFilter;
import com.shopjoy.entity.Product;
import com.shopjoy.util.Page;
import com.shopjoy.util.Pageable;

import java.util.List;

public interface IProductRepository extends GenericRepository<Product, Integer> {
    List<Product> findByCategoryId(Integer categoryId);
    List<Product> findByNameContaining(String keyword);
    List<Product> findByPriceRange(double minPrice, double maxPrice);
    long countByCategory(Integer categoryId);
    Page<Product> findAllPaginated(Pageable pageable, String sortBy, String sortDirection);
    Page<Product> findProductsWithFilters(ProductFilter filter, Pageable pageable, String sortBy, String sortDirection);
    Page<Product> searchProductsPaginated(String searchTerm, Pageable pageable);
    List<Product> findAllWithFilters(ProductFilter filter);
    List<Product> findRecentlyAdded(int limit);
}
