package com.shopjoy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "Request for creating a new order with shipping and payment information")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @Schema(description = "User ID placing the order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;

    @Schema(description = "Complete shipping address for order delivery", example = "123 Main St, Apt 4B, New York, NY 10001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    @Schema(description = "Additional order notes or special instructions", example = "Please call before delivery")
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    @Schema(description = "Payment method for the order", example = "CASH", allowableValues = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH_ON_DELIVERY", "MOBILE_MONEY"})
    @Size(max = 100, message = "Payment method cannot exceed 100 characters")
    private String paymentMethod;

    @Schema(description = "List of items in the order", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Order must have at least one item")
    private List<CreateOrderItemRequest> orderItems;

    @Schema(description = "Total amount for the order", example = "1299.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;
}
