package com.shopjoy.aspect;

import com.shopjoy.util.AspectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The type Security audit aspect.
 */
@Aspect
@Component
public class SecurityAuditAspect {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Audit user creation.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.UserService.create*(..))")
    public void auditUserCreation(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: USER CREATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit user update.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.UserService.update*(..))")
    public void auditUserUpdate(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: USER UPDATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit user deletion.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.UserService.delete*(..))")
    public void auditUserDeletion(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.warn("[{}] AUDIT: USER DELETED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit order creation.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.OrderService.create*(..))")
    public void auditOrderCreation(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: ORDER CREATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit order update.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.OrderService.update*(..))")
    public void auditOrderUpdate(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: ORDER UPDATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit product creation.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.ProductService.create*(..))")
    public void auditProductCreation(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: PRODUCT CREATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit product update.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.ProductService.update*(..))")
    public void auditProductUpdate(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: PRODUCT UPDATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit product deletion.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.ProductService.delete*(..))")
    public void auditProductDeletion(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.warn("[{}] AUDIT: PRODUCT DELETED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit inventory update.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.InventoryService.updateStock*(..))")
    public void auditInventoryUpdate(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: INVENTORY UPDATED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit stock reservation.
     *
     * @param joinPoint the join point
     */
    @AfterReturning("execution(* com.shopjoy.service.InventoryService.reserveStock*(..))")
    public void auditStockReservation(JoinPoint joinPoint) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT: STOCK RESERVED - Method: {}, Arguments: {}", 
            timestamp, joinPoint.getSignature().getName(), args);
    }

    /**
     * Audit method object.
     *
     * @param joinPoint the join point
     * @param auditable the auditable
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String methodSignature = AspectUtils.extractMethodSignature(joinPoint);
        String action = auditable.action();
        String description = auditable.description();
        String args = AspectUtils.sanitizeArgs(joinPoint.getArgs());
        
        auditLogger.info("[{}] AUDIT START: Action={}, Description={}, Method={}, Arguments={}", 
            timestamp, action, description, methodSignature, args);
        
        try {
            Object result = joinPoint.proceed();
            
            String endTimestamp = LocalDateTime.now().format(timeFormatter);
            auditLogger.info("[{}] AUDIT SUCCESS: Action={}, Method={}", 
                endTimestamp, action, methodSignature);
            
            return result;
        } catch (Exception e) {
            String endTimestamp = LocalDateTime.now().format(timeFormatter);
            auditLogger.error("[{}] AUDIT FAILURE: Action={}, Method={}, Exception={}", 
                endTimestamp, action, methodSignature, e.getMessage());
            throw e;
        }
    }

    /**
     * Audit data access.
     *
     * @param joinPoint the join point
     * @param result    the result
     */
    @AfterReturning(pointcut = "execution(* com.shopjoy.service.*.get*(..)) || execution(* com.shopjoy.service.*.find*(..))", returning = "result")
    public void auditDataAccess(JoinPoint joinPoint, Object result) {
        if (result != null) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            String className = AspectUtils.extractClassName(joinPoint);
            String methodName = AspectUtils.extractMethodName(joinPoint);
            
            auditLogger.debug("[{}] AUDIT: DATA ACCESS - {}.{} returned: {}", 
                timestamp, className, methodName, result.getClass().getSimpleName());
        }
    }
}
