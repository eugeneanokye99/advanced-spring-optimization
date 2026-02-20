package com.shopjoy.repository;

import com.shopjoy.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductId(int productId);
    List<Review> findByUserId(int userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRating(@Param("productId") int productId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.user.id = :userId AND r.product.id = :productId")
    boolean hasReviewed(@Param("userId") int userId, @Param("productId") int productId);
    
    @Query("SELECT r.user.id FROM Review r WHERE r.id = :reviewId")
    Integer findUserIdByReviewId(@Param("reviewId") Integer reviewId);
}
