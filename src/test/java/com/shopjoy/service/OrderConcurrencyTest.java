package com.shopjoy.service;

import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.Product;
import com.shopjoy.repository.InventoryRepository;
import com.shopjoy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Integer testProductId;
    private final int INITIAL_STOCK = 10;

    @BeforeEach
    void setUp() {
        // Setup a test product with limited stock
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getInventory() != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No product with inventory found for test"));
        
        testProductId = product.getId();
        
        var inventory = product.getInventory();
        inventory.setQuantityInStock(INITIAL_STOCK);
        inventoryRepository.save(inventory);
    }

    @Test
    void testConcurrentOrderCreationExhaustsStockCorrectly() throws InterruptedException {
        int numberOfThreads = 15; // More threads than stock
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(() -> {
                latch.await(); // Wait for sync start
                
                CreateOrderRequest request = new CreateOrderRequest();
                request.setUserId(1); // Assume user 1 exists
                request.setShippingAddress("Test Address");
                
                CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
                itemRequest.setProductId(testProductId);
                itemRequest.setQuantity(1);
                request.setOrderItems(List.of(itemRequest));

                try {
                    orderService.createOrder(request).join();
                    successfulOrders.incrementAndGet();
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (var task : tasks) {
            futures.add(executorService.submit(task));
        }

        latch.countDown(); // Start all threads
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Verify results
        int remainingStock = inventoryRepository.findByProductId(testProductId).get().getQuantityInStock();
        
        System.out.println("Successful orders: " + successfulOrders.get());
        System.out.println("Failed orders: " + failedOrders.get());
        System.out.println("Remaining stock: " + remainingStock);

        assertEquals(INITIAL_STOCK, successfulOrders.get(), "Successful orders should match available stock");
        assertEquals(0, remainingStock, "Stock should be completely exhausted");
        assertEquals(numberOfThreads - INITIAL_STOCK, failedOrders.get(), "Remaining threads should have failed");
    }
}
