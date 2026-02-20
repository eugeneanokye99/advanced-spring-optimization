package com.shopjoy.repository;

import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserId(int userId);
    Page<Order> findByUserId(int userId, Pageable pageable);
    
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status <> 'CANCELLED'")
    boolean hasUserPurchasedProduct(@Param("userId") int userId, @Param("productId") int productId);
    
    @Query("SELECT o.user.id FROM Order o WHERE o.id = :orderId")
    Integer findUserIdByOrderId(@Param("orderId") Integer orderId);
}
