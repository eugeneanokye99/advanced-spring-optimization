package com.shopjoy.aspect;

import com.shopjoy.entity.Order;
import com.shopjoy.entity.Product;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidationAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationAspect.class);
    
    @Before("execution(* com.shopjoy.service.ProductService.create*(..)) && args(product,..)")
    public void validateProductBeforeCreation(Product product) {
        logger.debug("Validating product before creation: {}", product);

        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        
        if (product.getProductName().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters long");
        }
        
        logger.debug("Product validation passed");
    }
    
    @Before(value = "execution(* com.shopjoy.service.ProductService.update*(..)) && args(productId, product,..)", argNames = "productId,product")
    public void validateProductBeforeUpdate(Integer productId, Product product) {
        logger.debug("Validating product before update: {}", product);
        
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }

        if (product.getProductName() != null && product.getProductName().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters long");
        }
        
        logger.debug("Product update validation passed");
    }
    
    @Before("execution(* com.shopjoy.service.OrderService.create*(..)) && args(order,..)")
    public void validateOrderBeforeCreation(Order order) {
        logger.debug("Validating order before creation: {}", order);
        
        if (order.getUser() == null || order.getUser().getId() <= 0) {
            throw new IllegalArgumentException("Valid user ID is required for order");
        }

        logger.debug("Order validation passed");
    }
    
    @Before(value = "execution(* com.shopjoy.service.InventoryService.updateStock*(..)) && args(productId, quantity,..)", argNames = "productId,quantity")
    public void validateStockUpdate(Integer productId, Integer quantity) {
        logger.debug("Validating stock update for product {} with quantity {}", productId, quantity);
        
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID for stock update");
        }
        
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        logger.debug("Stock update validation passed");
    }
    
    @Before(value = "execution(* com.shopjoy.service.InventoryService.reserveStock*(..)) && args(productId, quantity,..)", argNames = "productId,quantity")
    public void validateStockReservation(Integer productId, Integer quantity) {
        logger.debug("Validating stock reservation for product {} with quantity {}", productId, quantity);
        
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID for stock reservation");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Stock reservation quantity must be positive");
        }
        
        logger.debug("Stock reservation validation passed");
    }
    
    @Before("execution(* com.shopjoy.service.ReviewService.create*(..))")
    public void validateReviewBeforeCreation() {
        logger.debug("Validating review before creation");
        
        logger.debug("Review validation passed");
    }
    
    @Before("execution(* com.shopjoy.service.UserService.create*(..))")
    public void validateUserBeforeCreation() {
        logger.debug("Validating user before creation");
        
        logger.debug("User validation passed");
    }
}
