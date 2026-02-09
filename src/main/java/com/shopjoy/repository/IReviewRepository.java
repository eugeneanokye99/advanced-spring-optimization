package com.shopjoy.repository;

import com.shopjoy.entity.Review;

import java.util.List;

public interface IReviewRepository extends GenericRepository<Review, Integer> {
    List<Review> findByProductId(int productId);
    List<Review> findByUserId(int userId);
    void incrementHelpfulCount(int reviewId);
    Double getAverageRating(int productId);
    boolean hasUserReviewedProduct(int userId, int productId);
}
