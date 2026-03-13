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
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Tag(name = "Order Management", description = "APIs for order creation and payment processing")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order (async)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<OrderResponse>>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request)
                .thenApply(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Order created successfully")))
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    String message = cause != null ? cause.getMessage() : "Order creation failed";
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error(message));
                });
    }

    @Operation(summary = "Process order payment (async)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment processing failed")
    })
    @PatchMapping("/{id}/payment")
    public CompletableFuture<ResponseEntity<ApiResponse<OrderResponse>>> processPayment(
            @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id,
            @RequestParam String transactionId) {

        return orderService.processPayment(id, transactionId)
                .thenApply(response -> ResponseEntity
                        .ok(ApiResponse.success(response, "Payment processed successfully")))
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    String message = cause != null ? cause.getMessage() : "Payment processing failed";
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error(message));
                });
    }
}
