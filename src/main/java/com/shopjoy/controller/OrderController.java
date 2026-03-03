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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Tag(name = "Order Management", description = "APIs for order creation and payment processing")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

        private final OrderService orderService;

        @Operation(summary = "Create a new order (async)", description = "Creates a new order asynchronously with order items, shipping address, and payment information")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found", content = @Content(mediaType = "application/json"))
        })
        @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
        @PostMapping
        public CompletableFuture<ResponseEntity<ApiResponse<OrderResponse>>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
                return orderService.createOrder(request)
                        .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Order created successfully")));
        }

        /**
         * Process payment for an order.
         *
         * @param id the id
         * @param transactionId the transaction id
         * @return the response entity
         */
        @Operation(summary = "Process order payment (async)", description = "Processes payment asynchronously and transitions order to PROCESSING")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment processing failed", content = @Content(mediaType = "application/json"))
        })
        @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
        @PatchMapping("/{id}/payment")
        public CompletableFuture<ResponseEntity<ApiResponse<OrderResponse>>> processPayment(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id,
                        @RequestParam String transactionId) {
                return orderService.processPayment(id, transactionId)
                        .thenApply(response -> ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully")));
        }
}
