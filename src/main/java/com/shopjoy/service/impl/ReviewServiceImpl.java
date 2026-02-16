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

import lombok.AllArgsConstructor;
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
    public ReviewResponse createReview(CreateReviewRequest request) {
        if (reviewRepository.existsByUser_IdAndProduct_Id(request.getUserId(), request.getProductId())) {
            throw new BusinessException("User has already reviewed this product");
        }

        Review review = reviewMapper.toReview(request);
        review.setUser(userRepository.getReferenceById(request.getUserId()));
        review.setProduct(productRepository.getReferenceById(request.getProductId()));
        
        validateReviewData(review);

        boolean hasPurchased = orderRepository.hasUserPurchasedProduct(
                request.getUserId(), request.getProductId());

        if (!hasPurchased) {
            // User reviewing without purchase - allowed but noted
        }

        review.setCreatedAt(LocalDateTime.now());
        review.setHelpfulCount(0);

        Review createdReview = reviewRepository.save(review);

        return reviewMapper.toReviewResponse(createdReview);
    }

    @Override
    public ReviewResponse getReviewById(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return reviewMapper.toReviewResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProduct_Id(productId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Integer userId) {
        return reviewRepository.findByUser_Id(userId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    public ReviewResponse updateReview(Integer reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        reviewMapper.updateReviewFromRequest(request, review);
        validateReviewData(review);

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Override
    @Transactional()
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        reviewRepository.deleteById(reviewId);
    }

    @Override
    public List<ReviewResponse> getReviewsByRating(Integer productId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("rating", "must be between 1 and 5");
        }
        return reviewRepository.findByProduct_Id(productId).stream()
                .filter(review -> review.getRating() == rating)
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageRating(Integer productId) {
        return reviewRepository.getAverageRating(productId);
    }

    @Override
    @Transactional()
    public ReviewResponse markReviewAsHelpful(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Override
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
