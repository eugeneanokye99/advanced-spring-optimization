package com.shopjoy.repository;

import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The type Order repository.
 */
@Repository
@Transactional(readOnly = true)
public class OrderRepository implements IOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = (rs, _) -> {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null)
            order.setOrderDate(orderDate.toLocalDateTime());
        order.setTotalAmount(rs.getDouble("total_amount"));
        String statusStr = rs.getString("status");
        order.setStatus(statusStr != null ? OrderStatus.fromString(statusStr) : null);
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        String paymentStatusStr = rs.getString("payment_status");
        order.setPaymentStatus(paymentStatusStr != null ? PaymentStatus.fromString(paymentStatusStr) : null);
        order.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null)
            order.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null)
            order.setUpdatedAt(updated.toLocalDateTime());
        return order;
    };

    /**
     * Instantiates a new Order repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        if (orderId == null)
            return Optional.empty();
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, orderRowMapper, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() {
        return jdbcTemplate.query("SELECT * FROM orders ORDER BY order_date DESC", orderRowMapper);
    }

    @Override
    @Transactional()
    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address, payment_method, payment_status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING order_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setObject(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus() != null ? order.getStatus().toString().toLowerCase() : null);
            ps.setString(5, order.getShippingAddress());
            ps.setString(6, order.getPaymentMethod());
            ps.setString(7,
                    order.getPaymentStatus() != null ? order.getPaymentStatus().toString().toLowerCase() : null);
            ps.setString(8, order.getNotes());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null)
            order.setOrderId(keyHolder.getKey().intValue());
        return order;
    }

@Override
@Transactional()
public Order update(Order order) {
    String sql = "UPDATE orders SET status = ?, payment_status = ?, payment_method = ?, " +
                 "shipping_address = ?, notes = ?, total_amount = ?, updated_at = ? WHERE order_id = ?";
    jdbcTemplate.update(sql,
            order.getStatus() != null ? order.getStatus().toString().toLowerCase() : null,
            order.getPaymentStatus() != null ? order.getPaymentStatus().toString().toLowerCase() : null,
            order.getPaymentMethod(),
            order.getShippingAddress(),
            order.getNotes(),
            order.getTotalAmount(),
            LocalDateTime.now(),
            order.getOrderId());
    return order;
}

    @Override
    @Transactional()
    public boolean delete(Integer orderId) {
        return jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", orderId) > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsById(Integer orderId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders WHERE order_id = ?", Long.class, orderId);
        return count != null && count > 0;
    }

    /**
     * Find by user id list.
     *
     * @param userId the user id
     * @return the list
     */
    public List<Order> findByUserId(int userId) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC",
                orderRowMapper, userId);
    }

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    public List<Order> findByStatus(OrderStatus status) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC",
                orderRowMapper, status.toString().toLowerCase());
    }

    /**
     * Find by date range list.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the list
     */
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC",
                orderRowMapper, startDate, endDate);
    }

    /**
     * Has user purchased product boolean.
     *
     * @param userId    the user id
     * @param productId the product id
     * @return the boolean
     */
    public boolean hasUserPurchasedProduct(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.user_id = ? AND oi.product_id = ? AND o.status IN ('delivered', 'shipped')";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, productId);
        return count != null && count > 0;
    }
}
