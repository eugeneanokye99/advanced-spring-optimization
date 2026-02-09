package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.ReviewMapper;
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
public class ReviewServiceImpl implements ReviewService {



    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Instantiates a new Review service.
     *
     * @param reviewRepository the review repository
     * @param orderRepository  the order repository
     */
    public ReviewServiceImpl(ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = ReviewMapper.toReviewResponse(review);

        userRepository.findById(review.getUserId())
                .ifPresent(user -> response.setUserName(user.getFirstName() + " " + user.getLastName()));

        productRepository.findById(review.getProductId())
                .ifPresent(product -> response.setProductName(product.getProductName()));

        return response;
    }

    @Override
    @Transactional()
    public ReviewResponse createReview(CreateReviewRequest request) {
        Review review = ReviewMapper.toReview(request);
        validateReviewData(review);

        if (reviewRepository.hasUserReviewedProduct(review.getUserId(), review.getProductId())) {
            throw new BusinessException("User has already reviewed this product");
        }

        boolean hasPurchased = orderRepository.hasUserPurchasedProduct(
                review.getUserId(), review.getProductId());

        if (!hasPurchased) {
            // User reviewing without purchase - allowed but noted
        }

        review.setCreatedAt(LocalDateTime.now());
        review.setHelpfulCount(0);

        Review createdReview = reviewRepository.save(review);

        return convertToResponse(createdReview);
    }

    @Override
    public ReviewResponse getReviewById(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return convertToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Integer userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    public ReviewResponse updateReview(Integer reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        ReviewMapper.updateReviewFromRequest(review, request);
        validateReviewData(review);

        Review updatedReview = reviewRepository.update(review);
        return convertToResponse(updatedReview);
    }

    @Override
    @Transactional()
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        reviewRepository.delete(reviewId);
    }

    @Override
    public List<ReviewResponse> getReviewsByRating(Integer productId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("rating", "must be between 1 and 5");
        }
        return reviewRepository.findByProductId(productId).stream()
                .filter(review -> review.getRating() == rating)
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public double getAverageRating(Integer productId) {
        return reviewRepository.getAverageRating(productId);
    }

    @Override
    @Transactional()
    public ReviewResponse markReviewAsHelpful(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        reviewRepository.incrementHelpfulCount(reviewId);
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        return convertToResponse(review);
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private void validateReviewData(Review review) {
        if (review == null) {
            throw new ValidationException("Review data cannot be null");
        }

        if (review.getUserId() <= 0) {
            throw new ValidationException("userId", "is required");
        }

        if (review.getProductId() <= 0) {
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
