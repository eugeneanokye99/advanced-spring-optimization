package com.shopjoy.repository;

import com.shopjoy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByCategoryIdIn(List<Integer> categoryIds);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContaining(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
    
    long countByCategoryId(Integer categoryId);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findRecentlyAdded(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(COALESCE(:searchTerm, '') = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<Product> findWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("brand") String brand,
            @Param("active") Boolean active,
            Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(COALESCE(:searchTerm, '') = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:active IS NULL OR p.active = :active)")
    List<Product> findAllWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("brand") String brand,
            @Param("active") Boolean active);
}
