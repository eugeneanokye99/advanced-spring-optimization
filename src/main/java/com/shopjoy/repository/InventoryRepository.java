package com.shopjoy.repository;

import com.shopjoy.entity.Inventory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * The type Inventory repository.
 */
@Repository
@Transactional(readOnly = true)
public class InventoryRepository implements IInventoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Inventory> inventoryRowMapper = (rs, _) -> {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(rs.getInt("inventory_id"));
        inventory.setProductId(rs.getInt("product_id"));
        inventory.setQuantityInStock(rs.getInt("quantity_in_stock"));
        inventory.setReorderLevel(rs.getInt("reorder_level"));
        inventory.setWarehouseLocation(rs.getString("warehouse_location"));
        Timestamp lastRestocked = rs.getTimestamp("last_restocked");
        if (lastRestocked != null) inventory.setLastRestocked(lastRestocked.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) inventory.setUpdatedAt(updated.toLocalDateTime());
        return inventory;
    };

    /**
     * Instantiates a new Inventory repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Inventory> findById(Integer inventoryId) {
        if (inventoryId == null) return Optional.empty();
        String sql = "SELECT * FROM inventory WHERE inventory_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, inventoryRowMapper, inventoryId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Inventory> findAll() {
        return jdbcTemplate.query("SELECT * FROM inventory", inventoryRowMapper);
    }

    @Override
    @Transactional()
    public Inventory save(Inventory inventory) {
        String sql = "INSERT INTO inventory (product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING inventory_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, inventory.getProductId());
            ps.setInt(2, inventory.getQuantityInStock());
            ps.setInt(3, inventory.getReorderLevel());
            ps.setString(4, inventory.getWarehouseLocation());
            ps.setObject(5, inventory.getLastRestocked());
            ps.setObject(6, inventory.getUpdatedAt());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) inventory.setInventoryId(keyHolder.getKey().intValue());
        return inventory;
    }

    @Override
    @Transactional()
    public Inventory update(Inventory inventory) {
        String sql = "UPDATE inventory SET quantity_in_stock = ?, reorder_level = ?, warehouse_location = ?, updated_at = CURRENT_TIMESTAMP WHERE inventory_id = ?";
        jdbcTemplate.update(sql, inventory.getQuantityInStock(), inventory.getReorderLevel(), 
            inventory.getWarehouseLocation(), inventory.getInventoryId());
        return inventory;
    }

    @Override
    @Transactional()
    public boolean delete(Integer inventoryId) {
        return jdbcTemplate.update("DELETE FROM inventory WHERE inventory_id = ?", inventoryId) > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory", Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsById(Integer inventoryId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory WHERE inventory_id = ?", Long.class, inventoryId);
        return count != null && count > 0;
    }

    /**
     * Find by product id optional.
     *
     * @param productId the product id
     * @return the optional
     */
    public Optional<Inventory> findByProductId(int productId) {
        String sql = "SELECT * FROM inventory WHERE product_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, inventoryRowMapper, productId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Update stock.
     *
     * @param productId the product id
     * @param quantity  the quantity
     */
    @Transactional()
    public void updateStock(int productId, int quantity) {
        jdbcTemplate.update("UPDATE inventory SET quantity_in_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?",
                quantity, productId);
    }

    /**
     * Increment stock.
     *
     * @param productId the product id
     * @param increment the increment
     */
    @Transactional()
    public void incrementStock(int productId, int increment) {
        jdbcTemplate.update("UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?",
                increment, productId);
    }

    /**
     * Decrement stock.
     *
     * @param productId the product id
     * @param decrement the decrement
     */
    @Transactional()
    public void decrementStock(int productId, int decrement) {
        jdbcTemplate.update("UPDATE inventory SET quantity_in_stock = quantity_in_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ? AND quantity_in_stock >= ?",
                decrement, productId, decrement);
    }

    /**
     * Find low stock list.
     *
     * @return the list
     */
    public List<Inventory> findLowStock() {
        return jdbcTemplate.query("SELECT * FROM inventory WHERE quantity_in_stock <= reorder_level", inventoryRowMapper);
    }


}
