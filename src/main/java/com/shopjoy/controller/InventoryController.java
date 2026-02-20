package com.shopjoy.controller;

import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Inventory controller.
 */
@Tag(name = "Inventory Management", description = "APIs for managing product stock levels including stock updates, reservations, and low stock monitoring")
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Instantiates a new Inventory controller.
     *
     * @param inventoryService the inventory service
     */
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Gets inventory.
     *
     * @param productId the product id
     * @return the inventory
     */
    @Operation(
            summary = "Get product inventory",
            description = "Retrieves inventory information for a specific product including stock quantity and reorder levels"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId) {
        InventoryResponse response = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory retrieved successfully"));
    }

    /**
     * Gets inventory for multiple products in batch.
     *
     * @param productIds the product ids
     * @return the batch inventory
     */
    @Operation(
            summary = "Get product inventory in batch",
            description = "Retrieves inventory information for multiple products in a single request"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/products/batch")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryBatch(
            @Parameter(description = "List of product IDs", required = true, example = "1,2,3")
            @RequestParam List<Integer> productIds) {
        List<InventoryResponse> response = inventoryService.getInventoryByProducts(productIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Batch inventory retrieved successfully"));
    }

    /**
     * Is product in stock response entity.
     *
     * @param productId the product id
     * @return the response entity
     */
    @Operation(
            summary = "Check if product is in stock",
            description = "Verifies whether a product has any available stock"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock status retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/product/{productId}/in-stock")
    public ResponseEntity<ApiResponse<Boolean>> isProductInStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId) {
        boolean inStock = inventoryService.isProductInStock(productId);
        return ResponseEntity.ok(ApiResponse.success(inStock, "Stock status checked successfully"));
    }

    /**
     * Has available stock response entity.
     *
     * @param productId the product id
     * @param quantity  the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Check stock availability",
            description = "Verifies if sufficient stock is available for a specific quantity"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock availability checked successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/product/{productId}/available-stock")
    public ResponseEntity<ApiResponse<Boolean>> hasAvailableStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "Requested quantity", required = true, example = "5")
            @RequestParam Integer quantity) {
        boolean hasStock = inventoryService.hasAvailableStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(hasStock, "Available stock checked successfully"));
    }

    /**
     * Update stock response entity.
     *
     * @param productId   the product id
     * @param newQuantity the new quantity
     * @return the response entity
     */
    @Operation(
            summary = "Update stock quantity",
            description = "Sets the stock quantity to a specific value"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "New stock quantity", required = true, example = "100")
            @RequestParam Integer newQuantity) {
        InventoryResponse response = inventoryService.updateStock(productId, newQuantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock updated successfully"));
    }

    /**
     * Add stock response entity.
     *
     * @param productId the product id
     * @param quantity  the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Add stock",
            description = "Increases stock quantity by a specified amount"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/product/{productId}/add")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "Quantity to add", required = true, example = "50")
            @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock added successfully"));
    }

    /**
     * Remove stock response entity.
     *
     * @param productId the product id
     * @param quantity  the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Remove stock",
            description = "Decreases stock quantity by a specified amount"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock removed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Insufficient stock",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/product/{productId}/remove")
    public ResponseEntity<ApiResponse<InventoryResponse>> removeStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "Quantity to remove", required = true, example = "10")
            @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.removeStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock removed successfully"));
    }

    /**
     * Reserve stock response entity.
     *
     * @param productId the product id
     * @param quantity  the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Reserve stock",
            description = "Reserves a specific quantity of stock for an order, preventing it from being sold to others"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock reserved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Insufficient stock available",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/product/{productId}/reserve")
    public ResponseEntity<ApiResponse<Void>> reserveStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "Quantity to reserve", required = true, example = "3")
            @RequestParam Integer quantity) {
        inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock reserved successfully"));
    }

    /**
     * Release stock response entity.
     *
     * @param productId the product id
     * @param quantity  the quantity
     * @return the response entity
     */
    @Operation(
            summary = "Release reserved stock",
            description = "Releases previously reserved stock back to available inventory"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Stock released successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/product/{productId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "Quantity to release", required = true, example = "3")
            @RequestParam Integer quantity) {
        inventoryService.releaseStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock released successfully"));
    }

    /**
     * Gets low stock products.
     *
     * @return the low stock products
     */
    @Operation(
            summary = "Get low stock products",
            description = "Retrieves all products with stock quantities below their reorder levels"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Low stock products retrieved successfully",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockProducts() {
        List<InventoryResponse> response = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock products retrieved successfully"));
    }

    /**
     * Gets out of stock products.
     *
     * @return the out of stock products
     */
    @Operation(
            summary = "Get out of stock products",
            description = "Retrieves all products with zero available stock"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Out of stock products retrieved successfully",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getOutOfStockProducts() {
        List<InventoryResponse> response = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(ApiResponse.success(response, "Out of stock products retrieved successfully"));
    }

    /**
     * Update reorder level response entity.
     *
     * @param productId    the product id
     * @param reorderLevel the reorder level
     * @return the response entity
     */
    @Operation(
            summary = "Update reorder level",
            description = "Sets the minimum stock quantity that triggers a reorder notification"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reorder level updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/product/{productId}/reorder-level")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateReorderLevel(
            @Parameter(description = "Product unique identifier", required = true, example = "1")
            @PathVariable Integer productId,
            @Parameter(description = "New reorder level threshold", required = true, example = "20")
            @RequestParam Integer reorderLevel) {
        InventoryResponse response = inventoryService.updateReorderLevel(productId, reorderLevel);
        return ResponseEntity.ok(ApiResponse.success(response, "Reorder level updated successfully"));
    }
}
