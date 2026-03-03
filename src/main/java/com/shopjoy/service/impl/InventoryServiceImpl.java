package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.InventoryMapperStruct;
import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.entity.Inventory;
import com.shopjoy.exception.*;
import com.shopjoy.repository.InventoryRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.service.InventoryService;
import lombok.AllArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final ProductRepository productRepository;
    private final InventoryMapperStruct inventoryMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager"),
        @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
    })
    public InventoryResponse createInventory(Integer productId, int initialStock, int reorderLevel) {
        Inventory inventory = new Inventory();
        inventory.setProduct(productRepository.getReferenceById(productId));
        inventory.setQuantityInStock(initialStock);
        inventory.setReorderLevel(reorderLevel);

        validateInventoryData(inventory);

        Optional<Inventory> existing = inventoryRepository.findByProductId(productId);
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Inventory", "productId", productId);
        }

        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory createdInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(createdInventory);
    }

    @Override
    @Cacheable(value = "inventoryByProduct", key = "#productId", unless = "#result == null", cacheManager = "shortCacheManager")
    public InventoryResponse getInventoryByProduct(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Cacheable(value = "inventory", key = "'inStock-' + #productId", cacheManager = "shortCacheManager")
    public boolean isProductInStock(Integer productId) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.getQuantityInStock() > 0)
                .orElse(false);
    }

    @Override
    @Cacheable(value = "inventory", key = "'available-' + #productId + '-' + #quantity", cacheManager = "shortCacheManager")
    public boolean hasAvailableStock(Integer productId, int quantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.getQuantityInStock() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(
        put = { @CachePut(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager") },
        evict = {
            @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
        }
    )
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
    @Caching(
        put = { @CachePut(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager") },
        evict = {
            @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
        }
    )
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
    @Caching(
        put = { @CachePut(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager") },
        evict = {
            @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
        }
    )
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
    @Caching(evict = {
        @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager"),
        @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
    })
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
    @Caching(evict = {
        @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager"),
        @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "outOfStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
    })
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
    @Cacheable(value = "lowStock", cacheManager = "shortCacheManager")
    public List<InventoryResponse> getLowStockProducts() {
        return inventoryRepository.findLowStock().stream()
                .map(inventoryMapper::toInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "outOfStock", cacheManager = "shortCacheManager")
    public List<InventoryResponse> getOutOfStockProducts() {
        return inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getQuantityInStock() == 0)
                .map(inventoryMapper::toInventoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(
        put = { @CachePut(value = "inventoryByProduct", key = "#productId", cacheManager = "shortCacheManager") },
        evict = {
            @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
            @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
        }
    )
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

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "inventory", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "lowStock", allEntries = true, cacheManager = "shortCacheManager"),
        @CacheEvict(value = "products", allEntries = true, cacheManager = "cacheManager")
    })
    public void updateStockBatch(java.util.Map<Integer, Integer> inventoryUpdates) {
        if (inventoryUpdates == null || inventoryUpdates.isEmpty()) {
            return;
        }

        java.util.Map<Integer, Exception> failures = new java.util.concurrent.ConcurrentHashMap<>();

        // Use parallel stream for processing updates in parallel
        inventoryUpdates.entrySet().parallelStream().forEach(entry -> {
            Integer productId = entry.getKey();
            Integer quantityChange = entry.getValue();

            if (quantityChange == 0) return;

            try {
                if (quantityChange > 0) {
                    addStock(productId, quantityChange);
                } else {
                    removeStock(productId, Math.abs(quantityChange));
                }
            } catch (Exception e) {
                failures.put(productId, e);
            }
        });

        if (!failures.isEmpty()) {
            throw new BatchOperationException(
                "Batch inventory update partially failed for " + failures.size() + " items", 
                failures
            );
        }
    }

    private void validateInventoryData(Inventory inventory) {
        if (inventory == null) {
            throw new ValidationException("Inventory data cannot be null");
        }

        if (inventory.getProduct() == null || inventory.getProduct().getId() <= 0) {
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
