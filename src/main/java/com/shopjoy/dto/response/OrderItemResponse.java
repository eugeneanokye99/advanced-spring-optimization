package com.shopjoy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response containing details of a single order item")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    @Schema(description = "Order item unique identifier", example = "1")
    private Integer orderItemId;

    @Schema(description = "Product unique identifier", example = "101")
    private Integer productId;

    @Schema(description = "Product name", example = "Dell XPS 15")
    private String productName;

    @Schema(description = "Quantity ordered", example = "2")
    private int quantity;

    @Schema(description = "Price per unit at the time of order", example = "1299.99")
    private double unitPrice;

    @Schema(description = "Subtotal for this item (quantity * price)", example = "2599.98")
    private double subtotal;

    @Schema(description = "Category name of the product", example = "Electronics")
    private String categoryName;
}
