package com.shopjoy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for CartItem.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    
    private Integer cartItemId;
    private Integer userId;
    private Integer productId;
    private String productName;
    private double productPrice;
    private int quantity;
    private LocalDateTime addedAt;

}
