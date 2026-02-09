package com.shopjoy.repository;

import com.shopjoy.entity.Review;
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
 * The type Review repository.
 */
@Repository
@Transactional(readOnly = true)
public class ReviewRepository implements IReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Review> reviewRowMapper = (rs, _) -> {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setTitle(rs.getString("title"));
        review.setComment(rs.getString("comment"));
        review.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
        review.setHelpfulCount(rs.getInt("helpful_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) review.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) review.setUpdatedAt(updated.toLocalDateTime());
        return review;
    };

    /**
     * Instantiates a new Review repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Review> findById(Integer reviewId) {
        if (reviewId == null) return Optional.empty();
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findAll() {
        return jdbcTemplate.query("SELECT * FROM reviews ORDER BY created_at DESC", reviewRowMapper);
    }

    @Override
    @Transactional()
    public Review save(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, helpful_count) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING review_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getTitle());
            ps.setString(5, review.getComment());
            ps.setBoolean(6, review.isVerifiedPurchase());
            ps.setInt(7, review.getHelpfulCount());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    @Override
    @Transactional()
    public Review update(Review review) {
        String sql = "UPDATE reviews SET rating = ?, title = ?, comment = ?, updated_at = CURRENT_TIMESTAMP WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getRating(), review.getTitle(), review.getComment(), review.getReviewId());
        return review;
    }

    @Override
    @Transactional()
    public boolean delete(Integer reviewId) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId) > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsById(Integer reviewId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews WHERE review_id = ?", Long.class, reviewId);
        return count != null && count > 0;
    }

    /**
     * Find by product id list.
     *
     * @param productId the product id
     * @return the list
     */
    public List<Review> findByProductId(int productId) {
        return jdbcTemplate.query("SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC", 
            reviewRowMapper, productId);
    }

    /**
     * Find by user id list.
     *
     * @param userId the user id
     * @return the list
     */
    public List<Review> findByUserId(int userId) {
        return jdbcTemplate.query("SELECT * FROM reviews WHERE user_id = ? ORDER BY created_at DESC", 
            reviewRowMapper, userId);
    }

    /**
     * Increment helpful count.
     *
     * @param reviewId the review id
     */
    @Transactional()
    public void incrementHelpfulCount(int reviewId) {
        jdbcTemplate.update("UPDATE reviews SET helpful_count = helpful_count + 1 WHERE review_id = ?", reviewId);
    }

    /**
     * Gets average rating.
     *
     * @param productId the product id
     * @return the average rating
     */
    public Double getAverageRating(int productId) {
        return jdbcTemplate.queryForObject("SELECT AVG(rating) FROM reviews WHERE product_id = ?", Double.class, productId);
    }


    /**
     * Has user reviewed product boolean.
     *
     * @param userId    the user id
     * @param productId the product id
     * @return the boolean
     */
    public boolean hasUserReviewedProduct(int userId, int productId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND product_id = ?", 
            Long.class, userId, productId);
        return count != null && count > 0;
    }
}
