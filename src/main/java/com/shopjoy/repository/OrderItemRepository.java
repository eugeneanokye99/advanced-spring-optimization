package com.shopjoy.repository;

import com.shopjoy.entity.OrderItem;
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
 * The type Order item repository.
 */
@Repository
@Transactional(readOnly = true)
public class OrderItemRepository implements IOrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, _) -> {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setSubtotal(rs.getDouble("subtotal"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) item.setCreatedAt(created.toLocalDateTime());
        return item;
    };

    /**
     * Instantiates a new Order item repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public OrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<OrderItem> findById(Integer orderItemId) {
        if (orderItemId == null) return Optional.empty();
        String sql = "SELECT * FROM order_items WHERE order_item_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, orderItemRowMapper, orderItemId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<OrderItem> findAll() {
        return jdbcTemplate.query("SELECT * FROM order_items", orderItemRowMapper);
    }

    @Override
    @Transactional()
    public OrderItem save(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?) RETURNING order_item_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getSubtotal());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) item.setOrderItemId(keyHolder.getKey().intValue());
        return item;
    }

    @Override
    @Transactional()
    public OrderItem update(OrderItem item) {
        String sql = "UPDATE order_items SET quantity = ?, unit_price = ?, subtotal = ? WHERE order_item_id = ?";
        jdbcTemplate.update(sql, item.getQuantity(), item.getUnitPrice(), item.getSubtotal(), item.getOrderItemId());
        return item;
    }

    @Override
    @Transactional()
    public boolean delete(Integer orderItemId) {
        return jdbcTemplate.update("DELETE FROM order_items WHERE order_item_id = ?", orderItemId) > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_items", Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsById(Integer orderItemId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_items WHERE order_item_id = ?", Long.class, orderItemId);
        return count != null && count > 0;
    }

    /**
     * Find by order id list.
     *
     * @param orderId the order id
     * @return the list
     */
    public List<OrderItem> findByOrderId(int orderId) {
        return jdbcTemplate.query("SELECT * FROM order_items WHERE order_id = ?", orderItemRowMapper, orderId);
    }

}
