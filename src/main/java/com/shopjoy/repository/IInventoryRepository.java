package com.shopjoy.repository;

import com.shopjoy.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface IInventoryRepository extends GenericRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(int productId);
    void updateStock(int productId, int quantity);
    void incrementStock(int productId, int increment);
    void decrementStock(int productId, int decrement);
    List<Inventory> findLowStock();
}
