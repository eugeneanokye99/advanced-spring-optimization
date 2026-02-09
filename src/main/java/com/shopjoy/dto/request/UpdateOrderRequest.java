package com.shopjoy.dto.request;

import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO for updating an existing order.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    
    private OrderStatus status;
    
    private PaymentStatus paymentStatus;
    
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;
    
    @Size(max = 100, message = "Payment method cannot exceed 100 characters")
    private String paymentMethod;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    @Valid
    private List<UpdateOrderItemRequest> orderItems;
}
