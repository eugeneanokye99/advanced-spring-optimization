package com.shopjoy.controller;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The type Order controller.
 * Note: Most order operations (queries, status updates) use GraphQL.
 * This controller only handles order creation and payment processing.
 */
@Tag(name = "Order Management", description = "APIs for order creation and payment processing")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

        private final OrderService orderService;

        /**
         * Instantiates a new Order controller.
         *
         * @param orderService the order service
         */
        public OrderController(OrderService orderService) {
                this.orderService = orderService;
        }

        /**
         * Create order response entity.
         *
         * @param request the request
         * @return the response entity
         */
        @Operation(summary = "Create a new order", description = "Creates a new order with order items, shipping address, and payment information")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found", content = @Content(mediaType = "application/json"))
        })
        @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
        @PostMapping
        public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
                OrderResponse response = orderService.createOrder(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Order created successfully"));
        }

        /**
         * Process payment for an order.
         *
         * @param id the id
         * @param transactionId the transaction id
         * @return the response entity
         */
        @Operation(summary = "Process order payment", description = "Updates order payment status and transitions to PROCESSING")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment processing failed", content = @Content(mediaType = "application/json"))
        })
        @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
        @PatchMapping("/{id}/payment")
        public ResponseEntity<ApiResponse<OrderResponse>> processPayment(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id,
                        @RequestParam String transactionId) {
                OrderResponse response = orderService.processPayment(id, transactionId);
                return ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully"));
        }
}
