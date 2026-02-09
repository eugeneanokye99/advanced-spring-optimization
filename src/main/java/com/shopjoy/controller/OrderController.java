package com.shopjoy.controller;

import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The type Order controller.
 */
@Tag(name = "Order Management", description = "APIs for managing orders including creation, status transitions, and order queries")
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
        @PostMapping
        public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
                OrderResponse response = orderService.createOrder(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Order created successfully"));
        }

        /**
         * Gets order by id.
         *
         * @param id the id
         * @return the order by id
         */
        @Operation(summary = "Get order by ID", description = "Retrieves an order's details including order items and status by its unique identifier")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id) {
                OrderResponse response = orderService.getOrderById(id);
                return ResponseEntity.ok(ApiResponse.success(response, "Order retrieved successfully"));
        }

        /**
         * Gets orders by user.
         *
         * @param userId the user id
         * @return the orders by user
         */
        @Operation(summary = "Get orders by user", description = "Retrieves all orders placed by a specific user")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User orders retrieved successfully", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(
                        @Parameter(description = "User unique identifier", required = true, example = "1") @PathVariable Integer userId) {
                List<OrderResponse> response = orderService.getOrdersByUser(userId);
                return ResponseEntity.ok(ApiResponse.success(response, "User orders retrieved successfully"));
        }

        /**
         * Gets orders by status.
         *
         * @param status the status
         * @return the orders by status
         */
        @Operation(summary = "Get orders by status", description = "Retrieves all orders with a specific status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders by status retrieved successfully", content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/status/{status}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
                        @Parameter(description = "Order status filter", required = true, example = "PENDING") @PathVariable OrderStatus status) {
                List<OrderResponse> response = orderService.getOrdersByStatus(status);
                return ResponseEntity.ok(ApiResponse.success(response, "Orders by status retrieved successfully"));
        }

        /**
         * Gets orders by date range.
         *
         * @param startDate the start date
         * @param endDate   the end date
         * @return the orders by date range
         */
        @Operation(summary = "Get orders by date range", description = "Retrieves orders created within a specified date range")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders by date range retrieved successfully", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/date-range")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByDateRange(
                        @Parameter(description = "Start date (ISO format)", required = true, example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @Parameter(description = "End date (ISO format)", required = true, example = "2024-12-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
                List<OrderResponse> response = orderService.getOrdersByDateRange(startDate, endDate);
                return ResponseEntity.ok(ApiResponse.success(response, "Orders by date range retrieved successfully"));
        }

        /**
         * Update order status response entity.
         *
         * @param id     the id
         * @param status the status
         * @return the response entity
         */
        @Operation(summary = "Update order status", description = "Updates an order's status to a new value")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content(mediaType = "application/json"))
        })
        @PatchMapping("/{id}/status")
        public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id,
                        @Parameter(description = "New order status", required = true, example = "CONFIRMED") @RequestParam OrderStatus status) {
                OrderResponse response = orderService.updateOrderStatus(id, status);
                return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
        }

        /**
         * Confirm order response entity.
         *
         * @param id the id
         * @return the response entity
         */
        @Operation(summary = "Confirm order", description = "Confirms a pending order, transitioning it from PENDING to CONFIRMED status")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order confirmed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be confirmed in current state", content = @Content(mediaType = "application/json"))
        })
        @PatchMapping("/{id}/confirm")
        public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id) {
                OrderResponse response = orderService.confirmOrder(id);
                return ResponseEntity.ok(ApiResponse.success(response, "Order confirmed successfully"));
        }

        /**
         * Ship order response entity.
         *
         * @param id the id
         * @return the response entity
         */
        @Operation(summary = "Ship order", description = "Marks a confirmed order as shipped, transitioning it from CONFIRMED to SHIPPED status")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order shipped successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be shipped in current state", content = @Content(mediaType = "application/json"))
        })
        @PatchMapping("/{id}/ship")
        public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id) {
                OrderResponse response = orderService.shipOrder(id);
                return ResponseEntity.ok(ApiResponse.success(response, "Order shipped successfully"));
        }

        /**
         * Complete order response entity.
         *
         * @param id the id
         * @return the response entity
         */
        @Operation(summary = "Complete order", description = "Marks a shipped order as delivered/completed, transitioning it from SHIPPED to DELIVERED status")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be completed in current state", content = @Content(mediaType = "application/json"))
        })
        @PatchMapping("/{id}/complete")
        public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id) {
                OrderResponse response = orderService.completeOrder(id);
                return ResponseEntity.ok(ApiResponse.success(response, "Order completed successfully"));
        }

        /**
         * Cancel order response entity.
         *
         * @param id the id
         * @return the response entity
         */
        @Operation(summary = "Cancel order", description = "Cancels an order, transitioning it to CANCELLED status and restoring product stock")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order cancelled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be cancelled in current state", content = @Content(mediaType = "application/json"))
        })
        @PatchMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
                        @Parameter(description = "Order unique identifier", required = true, example = "1") @PathVariable Integer id) {
                OrderResponse response = orderService.cancelOrder(id);
                return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
        }

        /**
         * Gets all orders.
         *
         * @return the all orders
         */
        @Operation(summary = "Get all orders", description = "Retrieves all orders in the system. Typically used by administrators.")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All orders retrieved successfully", content = @Content(mediaType = "application/json"))
        })
        @GetMapping
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
                List<OrderResponse> response = orderService.getAllOrders();
                return ResponseEntity.ok(ApiResponse.success(response, "All orders retrieved successfully"));
        }

        /**
         * Gets pending orders.
         *
         * @return the pending orders
         */
        @Operation(summary = "Get pending orders", description = "Retrieves all orders with PENDING status awaiting confirmation")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending orders retrieved successfully", content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/pending")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getPendingOrders() {
                List<OrderResponse> response = orderService.getPendingOrders();
                return ResponseEntity.ok(ApiResponse.success(response, "Pending orders retrieved successfully"));
        }

        @Operation(summary = "Update order", description = "Updates order details (shipping address, payment method, notes). Only allowed for PENDING orders.")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be updated (not PENDING)", content = @Content(mediaType = "application/json"))
        })
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
                        @PathVariable Integer id,
                        @Valid @RequestBody UpdateOrderRequest request) {
                OrderResponse response = orderService.updateOrder(id, request);
                return ResponseEntity.ok(ApiResponse.success(response, "Order updated successfully"));
        }

        @Operation(summary = "Delete order", description = "Deletes an order and restores inventory. Only allowed for PENDING orders.")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order deleted successfully", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order cannot be deleted (not PENDING)", content = @Content(mediaType = "application/json"))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Integer id) {
                orderService.deleteOrder(id);
                return ResponseEntity.ok(ApiResponse.success(null, "Order deleted successfully"));
        }
}
