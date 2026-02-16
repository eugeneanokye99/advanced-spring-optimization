package com.shopjoy.service.impl;

import com.shopjoy.dto.request.CreateReviewRequest;
import com.shopjoy.dto.response.ReviewResponse;
import com.shopjoy.entity.Product;
import com.shopjoy.entity.Review;
import com.shopjoy.entity.User;
import com.shopjoy.exception.BusinessException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.repository.ReviewRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.dto.mapper.ReviewMapperStruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewMapperStruct reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Product product;
    private Review review;
    private CreateReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        product = new Product();
        product.setId(1);
        product.setProductName("Test Product");

        review = new Review();
        review.setId(1);
        review.setUser(user);
        review.setProduct(product);
        review.setRating(5);
        review.setComment("Great product!");

        reviewRequest = new CreateReviewRequest();
        reviewRequest.setUserId(1);
        reviewRequest.setProductId(1);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great product!");
    }

    @Test
    @DisplayName("Create Review - Success")
    void createReview_Success() {
        when(reviewRepository.existsByUser_IdAndProduct_Id(1, 1)).thenReturn(false);
        when(reviewMapper.toReview(any(CreateReviewRequest.class))).thenReturn(review);
        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(productRepository.getReferenceById(1)).thenReturn(product);
        when(orderRepository.hasUserPurchasedProduct(1, 1)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toReviewResponse(any(Review.class))).thenReturn(new ReviewResponse());

        ReviewResponse response = reviewService.createReview(reviewRequest);

        assertThat(response).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Delete Review - Success")
    void deleteReview_Success() {
        when(reviewRepository.existsById(1)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1);

        reviewService.deleteReview(1);

        verify(reviewRepository).deleteById(1);
    }
}
