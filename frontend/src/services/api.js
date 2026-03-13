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
        try {
            const token = localStorage.getItem('token');
            const userRaw = localStorage.getItem('user');
            const user = userRaw ? JSON.parse(userRaw) : {};

            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }

            if (user?.id) {
                config.headers['X-User-Id'] = user.id;
            }
        } catch (e) {
            console.warn('api interceptor: failed to read auth from localStorage', e);
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
        // If 401 Unauthorized, only redirect to login if we have no token at all
        // and we are not already on an auth-related page
        if (error.response?.status === 401) {
            const url = error.config?.url || '';
            const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/register');
            const isOAuth2Page = window.location.pathname.includes('/oauth2');
            const isLoginPage = window.location.pathname === '/login';
            const hasToken = !!localStorage.getItem('token');

            if (!isAuthRequest && !isOAuth2Page && !isLoginPage && !hasToken) {
                console.warn('No token found and received 401. Redirecting to login.');
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/login';
            }
        }

        const detailedError = createDetailedError(error.response);
        return Promise.reject(detailedError);
    }
);

export default api;
