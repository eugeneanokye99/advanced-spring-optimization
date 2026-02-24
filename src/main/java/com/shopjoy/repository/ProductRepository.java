package com.shopjoy.repository;

import com.shopjoy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    /**
     * Find all products with inventory eagerly loaded using EntityGraph.
     * This avoids N+1 queries when accessing inventory data.
     *
     * @return list of products with inventory pre-loaded
     */
    @EntityGraph(value = "Product.withInventoryAndCategory", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithInventory();

    /**
     * Find all products with inventory using pagination.
     *
     * @param pageable pagination information
     * @return page of products with inventory pre-loaded
     */
    @EntityGraph(value = "Product.withInventoryAndCategory", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithInventory(Pageable pageable);
    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByCategoryIdIn(List<Integer> categoryIds);
    
    @Query("select p from Product p where upper(p.productName) like upper(concat('%', ?1, '%'))")
    List<Product> findByProductName(String keyword);
    
    List<Product> findByPriceBetween(BigDecimal price, BigDecimal price2);
    
    long countByCategoryId(Integer categoryId);
    
    @Query("""
            select p from Product p
            where upper(p.productName) like upper(concat('%', ?1, '%')) or upper(p.description) like upper(concat('%', ?2, '%'))""")
    Page<Product> findByProductCategory(String productName, String description, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findRecentlyAdded(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(COALESCE(:searchTerm, '') = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', COALESCE(:searchTerm, ''), '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
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

}
