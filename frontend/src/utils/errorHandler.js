/**
 * Enhanced error handling utilities for better user experience
 */

/**
 * Formats error for user display
 * @param {Error} error - Error object from API
 * @param {boolean} showDetails - Whether to show detailed field errors
 * @returns {string} - Formatted error message
 */
export const formatErrorMessage = (error, showDetails = false) => {
    if (!error) return 'An unknown error occurred';
    
    if (showDetails && error.hasDetails) {
        return error.detailedMessage || error.message;
    }
    
    return error.message;
};

/**
 * Creates a user-friendly alert with detailed error information
 * @param {Error} error - Error object from API
 * @param {string} fallbackMessage - Fallback message if no specific error
 */
export const showErrorAlert = (error, fallbackMessage = 'Operation failed') => {
    const message = formatErrorMessage(error, true);
    alert(message || fallbackMessage);
};

/**
 * Creates error notification object for UI components
 * @param {Error} error - Error object from API  
 * @returns {Object} - Error notification object
 */
export const createErrorNotification = (error) => {
    return {
        type: 'error',
        title: 'Error',
        message: formatErrorMessage(error),
        details: error.hasDetails ? {
            fieldErrors: error.fieldErrors,
            generalErrors: error.generalErrors,
            errorCodes: error.errorCodes
        } : null,
        timestamp: new Date().toISOString()
    };
};

/**
 * Extracts validation errors for form handling
 * @param {Error} error - Error object from API
 * @returns {Object} - Field errors object {fieldName: errorMessage}
 */
export const extractFieldErrors = (error) => {
    if (!error?.errors) return {};
    
    const fieldErrors = {};
    error.errors.forEach(err => {
        if (err.field) {
            fieldErrors[err.field] = err.message;
        }
    });
    
    return fieldErrors;
};

/**
 * Checks if error is a specific type based on error codes
 * @param {Error} error - Error object from API
 * @param {string} errorCode - Error code to check for
 * @returns {boolean} - Whether error contains the specific code
 */
export const isErrorType = (error, errorCode) => {
    return error?.errorCodes?.includes(errorCode) || false;
};

/**
 * Common error type checks
 */
export const isValidationError = (error) => isErrorType(error, 'VALIDATION_ERROR');
export const isNotFoundError = (error) => isErrorType(error, 'NOT_FOUND');
export const isUnauthorizedError = (error) => isErrorType(error, 'UNAUTHORIZED');
export const isDuplicateError = (error) => isErrorType(error, 'DUPLICATE_ENTRY');
export const isInsufficientStockError = (error) => isErrorType(error, 'INSUFFICIENT_STOCK');

/**
 * Network error handler
 * @param {Error} error - Error object
 * @returns {string} - User-friendly message for network errors
 */
export const handleNetworkError = (error) => {
    if (!navigator.onLine) {
        return 'You appear to be offline. Please check your internet connection.';
    }
    
    if (error.code === 'NETWORK_ERROR' || !error.response) {
        return 'Unable to connect to server. Please try again later.';
    }
    
    return formatErrorMessage(error);
};