package com.shopjoy.repository;

import com.shopjoy.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(int productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock <= i.reorderLevel")
    List<Inventory> findLowStock();
    
    List<Inventory> findByProductIdIn(List<Integer> productIds);
}
