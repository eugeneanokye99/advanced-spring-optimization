import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor for adding auth token
api.interceptors.request.use(
    (config) => {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const token = localStorage.getItem('token');
        
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        
        if (user.id) {
            config.headers['X-User-Id'] = user.id;
        }
        
        return config;
    },
    (error) => Promise.reject(error)
);

// Enhanced error handling utility
const createDetailedError = (errorResponse) => {
    const error = new Error();
    
    if (errorResponse?.data) {
        const { message, errors, success } = errorResponse.data;
        
        // Main error message
        error.message = message || 'An unexpected error occurred';
        
        // Additional error details
        error.success = success;
        error.errors = errors || [];
        error.hasDetails = errors && errors.length > 0;
        
        // Format detailed error information
        if (error.hasDetails) {
            const fieldErrors = errors
                .filter(err => err.field)
                .map(err => `${err.field}: ${err.message}`)
                .join(', ');
            
            const generalErrors = errors
                .filter(err => !err.field)
                .map(err => err.message)
                .join(', ');
                
            error.fieldErrors = fieldErrors;
            error.generalErrors = generalErrors;
            
            // Create a comprehensive message
            const errorParts = [];
            if (message) errorParts.push(message);
            if (fieldErrors) errorParts.push(`Field errors: ${fieldErrors}`);
            if (generalErrors && !message.includes(generalErrors)) {
                errorParts.push(generalErrors);
            }
            
            error.detailedMessage = errorParts.join('. ');
        }
        
        // Extract error codes for programmatic handling
        error.errorCodes = errors?.map(err => err.errorCode).filter(Boolean) || [];
    } else {
        error.message = errorResponse?.message || 'Network error occurred';
        error.success = false;
        error.errors = [];
        error.hasDetails = false;
    }
    
    return error;
};

// Response interceptor for enhanced error handling
api.interceptors.response.use(
    (response) => {
        // For successful responses, return the data directly or the full response
        // depending on whether it's wrapped in ApiResponse
        if (response.data && typeof response.data === 'object' && 'success' in response.data) {
            return response.data; // Return ApiResponse structure
        }
        return response.data; // Return raw data
    },
    (error) => {
        const detailedError = createDetailedError(error.response);
        return Promise.reject(detailedError);
    }
);

export default api;
