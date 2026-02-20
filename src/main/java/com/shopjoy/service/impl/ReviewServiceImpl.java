package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.ReviewMapperStruct;
import com.shopjoy.dto.request.CreateReviewRequest;
import com.shopjoy.dto.request.UpdateReviewRequest;
import com.shopjoy.dto.response.ReviewResponse;
import com.shopjoy.entity.Review;
import com.shopjoy.exception.BusinessException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.repository.ReviewRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.ReviewService;

import com.shopjoy.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Review service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapperStruct reviewMapper;



    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "reviews", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "reviewsByProduct", key = "#request.productId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "reviewsByUser", key = "#request.userId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "productRating", key = "#request.productId", cacheManager = "mediumCacheManager")
    })
    public ReviewResponse createReview(CreateReviewRequest request) {
        if (reviewRepository.hasReviewed(request.getUserId(), request.getProductId())) {
            throw new BusinessException("User has already reviewed this product");
        }

        Review review = reviewMapper.toReview(request);
        review.setUser(userRepository.getReferenceById(request.getUserId()));
        review.setProduct(productRepository.getReferenceById(request.getProductId()));
        
        validateReviewData(review);

        boolean hasPurchased = orderRepository.hasUserPurchasedProduct(
                request.getUserId(), request.getProductId());

        review.setCreatedAt(LocalDateTime.now());
        review.setHelpfulCount(0);

        Review createdReview = reviewRepository.save(review);

        return reviewMapper.toReviewResponse(createdReview);
    }

    @Override
    @Cacheable(value = "review", key = "#reviewId", unless = "#result == null", cacheManager = "mediumCacheManager")
    public ReviewResponse getReviewById(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return reviewMapper.toReviewResponse(review);
    }

    @Override
    @Cacheable(value = "reviewsByProduct", key = "#productId", cacheManager = "mediumCacheManager")
    public List<ReviewResponse> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "reviewsByUser", key = "#userId", cacheManager = "mediumCacheManager")
    public List<ReviewResponse> getReviewsByUser(Integer userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    @Caching(
        put = { @CachePut(value = "review", key = "#reviewId", cacheManager = "mediumCacheManager") },
        evict = {
            @CacheEvict(value = "reviews", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "reviewsByProduct", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "reviewsByUser", allEntries = true, cacheManager = "mediumCacheManager"),
            @CacheEvict(value = "productRating", allEntries = true, cacheManager = "mediumCacheManager")
        }
    )
    public ReviewResponse updateReview(Integer reviewId, UpdateReviewRequest request) {
        Integer reviewOwnerId = reviewRepository.findUserIdByReviewId(reviewId);
        if (reviewOwnerId == null) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(reviewOwnerId)) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        reviewMapper.updateReviewFromRequest(request, review);
        validateReviewData(review);

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "review", key = "#reviewId", cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "reviews", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "reviewsByProduct", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "reviewsByUser", allEntries = true, cacheManager = "mediumCacheManager"),
        @CacheEvict(value = "productRating", allEntries = true, cacheManager = "mediumCacheManager")
    })
    public void deleteReview(Integer reviewId) {
        Integer reviewOwnerId = reviewRepository.findUserIdByReviewId(reviewId);
        if (reviewOwnerId == null) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(reviewOwnerId)) {
            throw new AccessDeniedException("You do not have permission to delete this review");
        }

        reviewRepository.deleteById(reviewId);
    }

    @Override
    public List<ReviewResponse> getReviewsByRating(Integer productId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("rating", "must be between 1 and 5");
        }
        return reviewRepository.findByProductId(productId).stream()
                .filter(review -> review.getRating() == rating)
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "productRating", key = "#productId", cacheManager = "mediumCacheManager")
    public double getAverageRating(Integer productId) {
        return reviewRepository.getAverageRating(productId);
    }

    @Override
    @Transactional()
    @CachePut(value = "review", key = "#reviewId", cacheManager = "mediumCacheManager")
    public ReviewResponse markReviewAsHelpful(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Override
    @Cacheable(value = "reviews", cacheManager = "mediumCacheManager")
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    private void validateReviewData(Review review) {
        if (review == null) {
            throw new ValidationException("Review data cannot be null");
        }

        if (review.getUser() == null || review.getUser().getId() <= 0) {
            throw new ValidationException("userId", "is required");
        }

        if (review.getProduct() == null || review.getProduct().getId() <= 0) {
            throw new ValidationException("productId", "is required");
        }

        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ValidationException("rating", "must be between 1 and 5");
        }

        if (review.getComment() != null && review.getComment().length() > 1000) {
            throw new ValidationException("comment", "must not exceed 1000 characters");
        }
    }
}
