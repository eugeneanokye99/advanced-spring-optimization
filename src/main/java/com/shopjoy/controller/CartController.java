package com.shopjoy.controller;

import com.shopjoy.dto.request.AddToCartRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.CartItemResponse;
import com.shopjoy.service.CartService;
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

import java.util.List;

/**
 * The type Cart controller.
 */
@Tag(name = "Shopping Cart", description = "APIs for managing user shopping carts including adding, updating, and removing items")
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    /**
     * Instantiates a new Cart controller.
     *
     * @param cartService the cart service
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Add to cart response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to the user's shopping cart with specified quantity"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Item added to cart successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartItemResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or insufficient stock",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User or product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        CartItemResponse response = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Item added to cart successfully"));
    }

    /**
     * Update cart item quantity response entity.
     *
     * @param cartItemId the cart item id
     * @param quantity   the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Update cart item quantity",
            description = "Updates the quantity of an item in the shopping cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart item quantity updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartItemResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Insufficient stock for requested quantity",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemQuantity(
            @Parameter(description = "Cart item unique identifier", required = true, example = "1")
            @PathVariable Integer cartItemId,
            @Parameter(description = "New quantity", required = true, example = "3")
            @RequestParam Integer quantity) {
        CartItemResponse response = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item quantity updated successfully"));
    }

    /**
     * Remove from cart response entity.
     *
     * @param cartItemId the cart item id
     * @return the response entity
     */
    @Operation(
            summary = "Remove item from cart",
            description = "Removes a specific item from the shopping cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Item removed from cart successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @Parameter(description = "Cart item unique identifier", required = true, example = "1")
            @PathVariable Integer cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from cart successfully"));
    }

    /**
     * Gets cart items.
     *
     * @param userId the user id
     * @return the cart items
     */
    @Operation(
            summary = "Get user's cart items",
            description = "Retrieves all items in a user's shopping cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart items retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItems(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        List<CartItemResponse> response = cartService.getCartItems(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart items retrieved successfully"));
    }

    /**
     * Clear cart response entity.
     *
     * @param userId the user id
     * @return the response entity
     */
    @Operation(
            summary = "Clear cart",
            description = "Removes all items from a user's shopping cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart cleared successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
    }

    /**
     * Gets cart total.
     *
     * @param userId the user id
     * @return the cart total
     */
    @Operation(
            summary = "Get cart total",
            description = "Calculates the total price of all items in the user's cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart total calculated successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<ApiResponse<Double>> getCartTotal(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        double total = cartService.getCartTotal(userId);
        return ResponseEntity.ok(ApiResponse.success(total, "Cart total calculated successfully"));
    }

    /**
     * Gets cart item count.
     *
     * @param userId the user id
     * @return the cart item count
     */
    @Operation(
            summary = "Get cart item count",
            description = "Returns the total number of items in the user's cart"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart item count retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        int count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Cart item count retrieved successfully"));
    }
}
