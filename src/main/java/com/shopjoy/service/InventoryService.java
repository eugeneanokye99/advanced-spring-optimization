package com.shopjoy.service;

import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;

import java.util.List;

/**
 * Service interface for Inventory management operations.
 * Handles stock tracking, updates, and low-stock alerts.
 */
public interface InventoryService {
    
    /**
     * Creates inventory record for a new product.
     * 
     * @param productId the product ID
     * @param initialStock the initial stock quantity
     * @param reorderLevel the reorder level
     * @return the created inventory response
     * @throws ValidationException if inventory data is invalid
     * @throws DuplicateResourceException if inventory already exists for product
     */
    InventoryResponse createInventory(Integer productId, int initialStock, int reorderLevel);
    
    /**
     * Retrieves inventory for a specific product.
     * 
     * @param productId the product ID
     * @return the inventory response
     * @throws ResourceNotFoundException if inventory not found
     */
    InventoryResponse getInventoryByProduct(Integer productId);
    
    /**
     * Checks if a product is in stock.
     * 
     * @param productId the product ID
     * @return true if product has stock, false otherwise
     */
    boolean isProductInStock(Integer productId);
    
    /**
     * Checks if a product has sufficient quantity available.
     * 
     * @param productId the product ID
     * @param quantity the required quantity
     * @return true if sufficient stock available, false otherwise
     */
    boolean hasAvailableStock(Integer productId, int quantity);
    
    /**
     * Updates the stock quantity for a product.
     * 
     * @param productId the product ID
     * @param newQuantity the new stock quantity
     * @return the updated inventory response
     * @throws ResourceNotFoundException if inventory not found
     * @throws ValidationException if quantity is negative
     */
    InventoryResponse updateStock(Integer productId, int newQuantity);
    
    /**
     * Adds stock to a product (restock operation).
     * 
     * @param productId the product ID
     * @param quantity the quantity to add
     * @return the updated inventory response
     * @throws ResourceNotFoundException if inventory not found
     * @throws ValidationException if quantity is invalid
     */
    InventoryResponse addStock(Integer productId, int quantity);
    
    /**
     * Removes stock from a product (sale or damage).
     * Validates that sufficient stock is available.
     * 
     * @param productId the product ID
     * @param quantity the quantity to remove
     * @return the updated inventory response
     * @throws ResourceNotFoundException if inventory not found
     * @throws InsufficientStockException if not enough stock available
     * @throws ValidationException if quantity is invalid
     */
    InventoryResponse removeStock(Integer productId, int quantity);
    
    /**
     * Reserves stock for an order (decreases available quantity).
     * Used during order creation to ensure atomicity.
     * 
     * @param productId the product ID
     * @param quantity the quantity to reserve
     * @throws ResourceNotFoundException if inventory not found
     * @throws InsufficientStockException if not enough stock available
     */
    void reserveStock(Integer productId, int quantity);
    
    /**
     * Releases reserved stock (increases available quantity).
     * Used when order is cancelled.
     * 
     * @param productId the product ID
     * @param quantity the quantity to release
     * @throws ResourceNotFoundException if inventory not found
     */
    void releaseStock(Integer productId, int quantity);
    
    /**
     * Retrieves all products with low stock (at or below reorder level).
     * 
     * @return list of low-stock inventory responses
     */
    List<InventoryResponse> getLowStockProducts();
    
    /**
     * Retrieves all out-of-stock products.
     * 
     * @return list of out-of-stock inventory responses
     */
    List<InventoryResponse> getOutOfStockProducts();
    
    /**
     * Updates the reorder level for a product.
     * 
     * @param productId the product ID
     * @param reorderLevel the new reorder level
     * @return the updated inventory response
     * @throws ResourceNotFoundException if inventory not found
     * @throws ValidationException if reorder level is invalid
     */
    InventoryResponse updateReorderLevel(Integer productId, int reorderLevel);

    /**
     * Retrieves inventory for multiple products in a single batch.
     * 
     * @param productIds the list of product IDs
     * @return list of inventory responses
     */
    List<InventoryResponse> getInventoryByProducts(List<Integer> productIds);

    /**
     * Updates stock for multiple products in a batch using parallel processing.
     * 
     * @param inventoryUpdates map of product ID to quantity change (positive to add, negative to remove)
     */
    void updateStockBatch(java.util.Map<Integer, Integer> inventoryUpdates);
}
