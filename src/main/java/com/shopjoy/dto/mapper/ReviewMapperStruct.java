package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateReviewRequest;
import com.shopjoy.dto.request.UpdateReviewRequest;
import com.shopjoy.dto.response.ReviewResponse;
import com.shopjoy.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Review entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapperStruct {

    /**
     * Convert CreateReviewRequest to Review entity.
     *
     * @param request the create review request
     * @return the review entity
     */
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isVerifiedPurchase", ignore = true)
    Review toReview(CreateReviewRequest request);

    /**
     * Convert Review entity to ReviewResponse.
     *
     * @param review the review entity
     * @return the review response
     */
    @Mapping(target = "userName", expression = "java(review.getUser() != null ? review.getUser().getFirstName() + \" \" + review.getUser().getLastName() : \"Unknown User\")")
    @Mapping(target = "productName", source = "product.productName")
    ReviewResponse toReviewResponse(Review review);

    /**
     * Update Review entity from UpdateReviewRequest.
     *
     * @param request the update request
     * @param review the review entity to update
     */
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verifiedPurchase", ignore = true)
    void updateReviewFromRequest(UpdateReviewRequest request, @MappingTarget Review review);
}