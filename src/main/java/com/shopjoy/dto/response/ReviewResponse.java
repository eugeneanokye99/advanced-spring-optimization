package com.shopjoy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for Review.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer productId;
    private String productName;
    private int rating;
    private String title;
    private String comment;
    private LocalDateTime createdAt;

}
