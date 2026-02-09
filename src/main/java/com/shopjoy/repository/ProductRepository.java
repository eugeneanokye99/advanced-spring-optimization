package com.shopjoy.repository;

import com.shopjoy.dto.filter.ProductFilter;
import com.shopjoy.entity.Product;
import com.shopjoy.util.Page;
import com.shopjoy.util.Pageable;
import com.shopjoy.util.SortValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class ProductRepository implements IProductRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Product> productRowMapper = (rs, _) -> {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setCostPrice(rs.getDouble("cost_price"));
        product.setSku(rs.getString("sku"));
        product.setBrand(rs.getString("brand"));
        product.setImageUrl(rs.getString("image_url"));
        product.setActive(rs.getBoolean("is_active"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) {
            product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return product;
    };

    /**
     * Instantiates a new Product repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Product> findById(Integer productId) {
        if (productId == null)
            return Optional.empty();

        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products WHERE product_id = ?
                \s""";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, productRowMapper, productId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products ORDER BY product_name
                \s""";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    @Transactional()
    public Product save(Product product) {
        String sql = """
                 INSERT INTO products (category_id, product_name, description, price, cost_price,
                                     sku, brand, image_url, is_active, created_at, updated_at)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                 RETURNING product_id
                \s""";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setDouble(5, product.getCostPrice());
            ps.setString(6, product.getSku());
            ps.setString(7, product.getBrand());
            ps.setString(8, product.getImageUrl());
            ps.setBoolean(9, product.isActive());
            ps.setObject(10, product.getCreatedAt());
            ps.setObject(11, product.getUpdatedAt());
            return ps;
        }, keyHolder);

        product.setProductId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return product;
    }

    @Override
    @Transactional()
    public Product update(Product product) {
        String sql = """
                 UPDATE products\s
                 SET category_id = ?, product_name = ?, description = ?, price = ?, cost_price = ?,
                     sku = ?, brand = ?, image_url = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE product_id = ?
                \s""";

        jdbcTemplate.update(sql,
                product.getCategoryId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getCostPrice(),
                product.getSku(),
                product.getBrand(),
                product.getImageUrl(),
                product.isActive(),
                product.getProductId());

        return product;
    }

    @Override
    @Transactional()
    public boolean delete(Integer productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        return jdbcTemplate.update(sql, productId) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM products";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsById(Integer productId) {
        if (productId == null)
            return false;
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, productId);
        return count != null && count > 0;
    }

    /**
     * Find by category id list.
     *
     * @param categoryId the category id
     * @return the list
     */
    public List<Product> findByCategoryId(Integer categoryId) {
        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products WHERE category_id = ? ORDER BY product_name
                \s""";
        return jdbcTemplate.query(sql, productRowMapper, categoryId);
    }

    /**
     * Find by name containing list.
     *
     * @param keyword the keyword
     * @return the list
     */
    public List<Product> findByNameContaining(String keyword) {
        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products WHERE product_name ILIKE ? ORDER BY product_name
                \s""";
        return jdbcTemplate.query(sql, productRowMapper, "%" + keyword + "%");
    }

    /**
     * Find by price range list.
     *
     * @param minPrice the min price
     * @param maxPrice the max price
     * @return the list
     */
    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products WHERE price BETWEEN ? AND ? ORDER BY price
                \s""";
        return jdbcTemplate.query(sql, productRowMapper, minPrice, maxPrice);
    }

    /**
     * Count by category long.
     *
     * @param categoryId the category id
     * @return the long
     */
    public long countByCategory(Integer categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, categoryId);
        return count != null ? count : 0L;
    }

    public Page<Product> findAllPaginated(Pageable pageable, String sortBy, String sortDirection) {
        String safeSort = SortValidator.getSafeProductSortField(sortBy);
        String safeDirection = SortValidator.getSafeDirection(sortDirection);

        String sql = String.format("""
                SELECT product_id, category_id, product_name, description, price, cost_price,
                       sku, brand, image_url, is_active, created_at, updated_at
                FROM products
                ORDER BY %s %s
                LIMIT ? OFFSET ?
                """, safeSort, safeDirection);

        List<Product> products = jdbcTemplate.query(sql, productRowMapper, pageable.getSize(), pageable.getOffset());
        long total = count();

        return new Page<>(products, pageable, total);
    }

    public Page<Product> findProductsWithFilters(ProductFilter filter, Pageable pageable, String sortBy,
            String sortDirection) {
        StringBuilder sql = new StringBuilder("""
                SELECT product_id, category_id, product_name, description, price, cost_price,
                       sku, brand, image_url, is_active, created_at, updated_at
                FROM products
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();
        buildFilterConditions(sql, filter, params);

        String safeSort = SortValidator.getSafeProductSortField(sortBy);
        String safeDirection = SortValidator.getSafeDirection(sortDirection);
        sql.append(String.format(" ORDER BY %s %s", safeSort, safeDirection));
        sql.append(" LIMIT ? OFFSET ?");

        params.add(pageable.getSize());
        params.add(pageable.getOffset());

        List<Product> products = jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
        long total = countProductsWithFilters(filter);

        return new Page<>(products, pageable, total);
    }

    private void buildFilterConditions(StringBuilder sql, ProductFilter filter, List<Object> params) {
        if (filter.getMinPrice() != null) {
            sql.append(" AND price >= ?");
            params.add(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            sql.append(" AND price <= ?");
            params.add(filter.getMaxPrice());
        }

        if (filter.getCategoryId() != null) {
            sql.append(" AND category_id = ?");
            params.add(filter.getCategoryId());
        }

        if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
            sql.append(" AND (product_name ILIKE ? OR description ILIKE ?)");
            String searchPattern = "%" + filter.getSearchTerm() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (filter.getIsActive() != null) {
            sql.append(" AND is_active = ?");
            params.add(filter.getIsActive());
        }
    }

    private long countProductsWithFilters(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();
        buildFilterConditions(sql, filter, params);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    public Page<Product> searchProductsPaginated(String searchTerm, Pageable pageable) {
        String sql = """
                SELECT product_id, category_id, product_name, description, price, cost_price,
                       sku, brand, image_url, is_active, created_at, updated_at
                FROM products
                WHERE product_name ILIKE ? OR description ILIKE ?
                ORDER BY product_name
                LIMIT ? OFFSET ?
                """;
        String searchPattern = "%" + searchTerm + "%";
        List<Product> products = jdbcTemplate.query(sql, productRowMapper, searchPattern, searchPattern,
                pageable.getSize(), pageable.getOffset());

        String countSql = "SELECT COUNT(*) FROM products WHERE product_name ILIKE ? OR description ILIKE ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, searchPattern, searchPattern);

        return new Page<>(products, pageable, total != null ? total : 0);
    }

    public List<Product> findAllWithFilters(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT product_id, category_id, product_name, description, price, cost_price,
                       sku, brand, image_url, is_active, created_at, updated_at
                FROM products
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();
        buildFilterConditions(sql, filter, params);

        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    public List<Product> findRecentlyAdded(int limit) {
        String sql = """
                 SELECT product_id, category_id, product_name, description, price, cost_price,
                        sku, brand, image_url, is_active, created_at, updated_at
                 FROM products ORDER BY created_at DESC LIMIT ?
                \s""";
        return jdbcTemplate.query(sql, productRowMapper, limit);
    }
}
