package com.shopjoy.exception;

import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler that catches all exceptions thrown in the application
 * and converts them to consistent API responses with appropriate HTTP status codes.
 * This class ensures:
 * - Consistent error response format across the entire API
 * - Appropriate HTTP status codes for different error types
 * - Detailed validation error information
 * - Proper logging for debugging
 * - Security by not exposing internal details in production
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * Handles missing static resources (favicon, browser dev tools requests, etc.).
     * Suppresses logging for common browser-generated requests.
     * Returns 404 Not Found without logging noise.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        // Suppress common browser requests that aren't actual errors
        if (path.contains("favicon.ico") ||
                path.contains(".well-known") ||
                path.contains("graphiql")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Log actual missing resources
        logger.warn("Resource not found: {}", path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    /**
     * Handles validation errors from @Valid annotation on request DTOs.
     * Returns 400 Bad Request with detailed field-level error information.
     * Example: When CreateProductRequest has @NotBlank on productName but client sends null
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        List<ErrorDetail> errors = new ArrayList<>();
        
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ErrorDetail error = new ErrorDetail(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue(),
                    fieldError.getCode() != null ? fieldError.getCode().toUpperCase() : "VALIDATION_ERROR"
            );
            errors.add(error);
        }
        
        ApiResponse<Object> response = ApiResponse.validationError(
                "Validation failed for one or more fields",
                errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    

    /**
     * Handles ResourceNotFoundException - when entity not found by ID.
     * Returns 404 Not Found.
     * Example: GET /api/v1/products/999 when product 999 doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.info("Resource not found: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getFieldName() != null ? ex.getFieldName() : "resource",
                ex.getMessage(),
                ex.getFieldValue(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handles DuplicateResourceException - when trying to create a resource that already exists.
     * Returns 409 Conflict.
     * Example: Creating user with email that already exists
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        logger.warn("Duplicate resource: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.conflict(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handles ValidationException - business rule validation failures.
     * Returns 400 Bad Request.
     * Example: Price cannot be negative, stock quantity invalid, etc.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessValidation(ValidationException ex) {
        logger.warn("Business validation failed: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles InsufficientStockException - when order quantity exceeds available stock.
     * Returns 400 Bad Request.
     * Example: Trying to order 100 units when only 50 are in stock
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientStock(InsufficientStockException ex) {
        logger.warn("Insufficient stock: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                "quantity",
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles InvalidOperationException - when operation cannot be performed due to business rules.
     * Returns 400 Bad Request.
     * Example: Canceling an already shipped order
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOperation(InvalidOperationException ex) {
        logger.warn("Invalid operation: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getOperation() != null ? ex.getOperation() : "operation",
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles InvalidOrderStateException - order state transition errors.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOrderState(InvalidOrderStateException ex) {
        logger.warn("Invalid order state: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles malformed JSON requests.
     * Returns 400 Bad Request.
     * Example: Invalid JSON syntax, missing quotes, wrong data type
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        
        String message = "Malformed JSON request. Please check your request body format.";
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null && causeMessage.contains("Cannot deserialize")) {
                message = "Invalid data type in request. Please check field types.";
            }
        }
        
        ErrorDetail error = new ErrorDetail(
                "request",
                message,
                "MALFORMED_JSON"
        );
        
        ApiResponse<Object> response = ApiResponse.error(message, error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    

    
    /**
     * Handles AuthenticationException - when user credentials are invalid.
     * Returns 401 Unauthorized.
     * Example: Invalid username/password, expired session, etc.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                "credentials",
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handles general BusinessException - business rule violations.
     * Returns 400 Bad Request.
     * This is a catch-all for business exceptions that don't have specific handlers.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        logger.warn("Business exception: {}", ex.getMessage());
        
        ErrorDetail error = new ErrorDetail(
                ex.getMessage(),
                ex.getErrorCode()
        );
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles database constraint violations (unique constraints, foreign key violations, check constraints, etc.).
     * Returns 409 Conflict for constraint violations, 400 for validation/deletion restrictions.
     * Example: Violating unique email constraint, trying to delete referenced records, invalid enum values
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation. This operation conflicts with existing data.";
        String errorCode = "DATA_INTEGRITY_VIOLATION";
        String field = null;
        HttpStatus status = HttpStatus.CONFLICT;
        
        // Try to provide more specific message based on constraint violation
        if (ex.getMessage() != null) {
            String errorMsg = ex.getMessage().toLowerCase();
            
            if (errorMsg.contains("unique") || errorMsg.contains("duplicate")) {
                message = "A record with this value already exists. Please use a unique value.";
                errorCode = "DUPLICATE_VALUE";
                
            } else if (errorMsg.contains("check constraint") || errorMsg.contains("violates check")) {
                // Handle CHECK constraint violations - these are validation errors
                status = HttpStatus.BAD_REQUEST;
                errorCode = "INVALID_VALUE";
                
                // Extract constraint name and provide specific messages
                if (errorMsg.contains("address_type_check") || errorMsg.contains("addresses_address_type")) {
                    message = "Invalid address type. Please select a valid address type (Home, Work, Shipping, Billing, or Other).";
                    field = "addressType";
                    errorCode = "INVALID_ADDRESS_TYPE";
                } else if (errorMsg.contains("user_type_check") || errorMsg.contains("users_user_type")) {
                    message = "Invalid user type. Please select a valid user type.";
                    field = "userType";
                    errorCode = "INVALID_USER_TYPE";
                } else if (errorMsg.contains("status_check") || errorMsg.contains("order_status")) {
                    message = "Invalid order status. Please select a valid status.";
                    field = "status";
                    errorCode = "INVALID_STATUS";
                } else if (errorMsg.contains("payment_status_check")) {
                    message = "Invalid payment status. Please select a valid payment status.";
                    field = "paymentStatus";
                    errorCode = "INVALID_PAYMENT_STATUS";
                } else if (errorMsg.contains("rating_check") || errorMsg.contains("reviews_rating")) {
                    message = "Invalid rating. Rating must be between 1 and 5.";
                    field = "rating";
                    errorCode = "INVALID_RATING";
                } else if (errorMsg.contains("price") && errorMsg.contains("check")) {
                    message = "Invalid price. Price must be a positive value.";
                    field = "price";
                    errorCode = "INVALID_PRICE";
                } else if (errorMsg.contains("quantity") && errorMsg.contains("check")) {
                    message = "Invalid quantity. Quantity must be a positive value.";
                    field = "quantity";
                    errorCode = "INVALID_QUANTITY";
                } else {
                    // Generic check constraint message
                    message = "The provided value is not valid. Please check your input and try again.";
                }
                
            } else if (errorMsg.contains("foreign key") && errorMsg.contains("restrict")) {
                // Handle foreign key RESTRICT violations (cannot delete because referenced)
                if (errorMsg.contains("order_items") && errorMsg.contains("product")) {
                    message = "Cannot delete this product because it is part of existing customer orders. " +
                             "Products that have been ordered cannot be removed for order history integrity.";
                    errorCode = "PRODUCT_IN_ORDERS";
                } else if (errorMsg.contains("order") && errorMsg.contains("customer")) {
                    message = "Cannot delete this customer because they have existing orders. " +
                             "Customers with order history cannot be removed.";
                    errorCode = "CUSTOMER_HAS_ORDERS";
                } else if (errorMsg.contains("products") && errorMsg.contains("category")) {
                    message = "Cannot delete this category because it contains products. " +
                             "Please move or delete all products in this category first.";
                    errorCode = "CATEGORY_HAS_PRODUCTS";
                } else {
                    message = "Cannot delete this record because it is referenced by other data. " +
                             "Please check for related records that depend on this item.";
                    errorCode = "REFERENCED_RECORD";
                }
                status = HttpStatus.BAD_REQUEST; // Use 400 for deletion restrictions
                
            } else if (errorMsg.contains("foreign key")) {
                message = "Invalid reference. The specified related record does not exist.";
                errorCode = "INVALID_REFERENCE";
                status = HttpStatus.BAD_REQUEST;
                
            } else if (errorMsg.contains("not-null") || errorMsg.contains("null")) {
                message = "Required field is missing. Please provide all required fields.";
                errorCode = "MISSING_REQUIRED_FIELD";
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        ErrorDetail error = new ErrorDetail(
                field,
                message,
                null,
                errorCode
        );
        
        ApiResponse<Object> response = status == HttpStatus.BAD_REQUEST ? 
                ApiResponse.error(message, error) : ApiResponse.conflict(message, error);
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Handles general database/JDBC errors.
     * Returns 500 Internal Server Error.
     * IMPORTANT: Does not expose internal database details to clients for security.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDatabaseError(DataAccessException ex) {
        logger.error("Database error occurred", ex);
        
        String message = "A database error occurred. Please try again later.";
        
        ErrorDetail error = new ErrorDetail(
                message,
                "DATABASE_ERROR"
        );
        
        ApiResponse<Object> response = ApiResponse.error(message, error);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handles all other uncaught exceptions.
     * Returns 500 Internal Server Error.
     * This is the catch-all handler that should log full details for debugging
     * but return a safe generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        String message = "An unexpected error occurred. Please try again later.";
        
        ErrorDetail error = new ErrorDetail(
                message,
                "INTERNAL_ERROR"
        );
        
        ApiResponse<Object> response = ApiResponse.error(message, error);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
