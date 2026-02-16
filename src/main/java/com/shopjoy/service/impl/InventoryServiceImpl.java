package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.InventoryMapperStruct;
import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.entity.Inventory;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.InsufficientStockException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.InventoryRepository;
import com.shopjoy.service.InventoryService;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Inventory service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapperStruct inventoryMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public InventoryResponse createInventory(Integer productId, int initialStock, int reorderLevel) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantityInStock(initialStock);
        inventory.setReorderLevel(reorderLevel);

        validateInventoryData(inventory);

        Optional<Inventory> existing = inventoryRepository.findByProductId(inventory.getProductId());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Inventory", "productId", inventory.getProductId());
        }

        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory createdInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(createdInventory);
    }

    @Override
    public InventoryResponse getInventoryByProduct(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    public boolean isProductInStock(Integer productId) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.getQuantityInStock() > 0)
                .orElse(false);
    }

    @Override
    public boolean hasAvailableStock(Integer productId, int quantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.getQuantityInStock() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public InventoryResponse updateStock(Integer productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new ValidationException("quantityInStock", "cannot be negative");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        inventory.setQuantityInStock(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toInventoryResponse(savedInventory);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public InventoryResponse addStock(Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toInventoryResponse(savedInventory);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public InventoryResponse removeStock(Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        if (inventory.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(
                    productId,
                    quantity,
                    inventory.getQuantityInStock());
        }

        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toInventoryResponse(savedInventory);
    }

    /**
     * RESERVATION TRANSACTION LOGIC
     * <p>
     * This method is designed to be called within an existing order transaction.
     * It uses REQUIRED propagation to join the caller's transaction context.
     * If the order creation fails later, this stock decrement will be rolled back.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reserveStock(Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        if (inventory.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(
                    productId,
                    quantity,
                    inventory.getQuantityInStock());
        }

        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void releaseStock(Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
                
        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Override
    public List<InventoryResponse> getLowStockProducts() {
        return inventoryRepository.findLowStock().stream()
                .map(inventoryMapper::toInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryResponse> getOutOfStockProducts() {
        return inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getQuantityInStock() == 0)
                .map(inventoryMapper::toInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public InventoryResponse updateReorderLevel(Integer productId, int reorderLevel) {
        if (reorderLevel < 0) {
            throw new ValidationException("reorderLevel", "cannot be negative");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        inventory.setReorderLevel(reorderLevel);
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(updatedInventory);
    }

    @Override
    public List<InventoryResponse> getInventoryByProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return inventoryRepository.findByProductIdIn(productIds).stream()
                .map(inventoryMapper::toInventoryResponse)
                .collect(Collectors.toList());
    }

    private void validateInventoryData(Inventory inventory) {
        if (inventory == null) {
            throw new ValidationException("Inventory data cannot be null");
        }

        if (inventory.getProductId() <= 0) {
            throw new ValidationException("productId", "must be a valid product ID");
        }

        if (inventory.getQuantityInStock() < 0) {
            throw new ValidationException("quantityInStock", "cannot be negative");
        }

        if (inventory.getReorderLevel() < 0) {
            throw new ValidationException("reorderLevel", "cannot be negative");
        }
    }
}
