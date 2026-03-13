import { createContext, useContext, useState, useEffect } from 'react';
import { authenticateUser } from '../services/userService';
import { formatErrorMessage, isAuthenticationError } from '../utils/errorHandler';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';

const AuthContext = createContext(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Check if user is logged in on mount
        try {
            const storedUser = localStorage.getItem('user');
            const storedToken = localStorage.getItem('token');

            if (storedUser && storedToken) {
                const parsedUser = JSON.parse(storedUser);
                // Handle legacy user object format or clear invalid session
                if (parsedUser.userId && !parsedUser.id) {
                    console.warn('Migrating legacy user object');
                    parsedUser.id = parsedUser.userId;
                    localStorage.setItem('user', JSON.stringify(parsedUser));
                }

                if (parsedUser.id) {
                    setUser(parsedUser);
                } else {
                    localStorage.removeItem('user');
                    localStorage.removeItem('token');
                }
            }
        } catch (e) {
            console.warn('Failed to restore session from localStorage:', e);
            localStorage.removeItem('user');
            localStorage.removeItem('token');
        } finally {
            setLoading(false);
        }
    }, []);

    const login = async (username, password) => {
        try {
            // api.js returns ApiResponse: { success, data: { token, refreshToken, ... }, message }
            const apiResponse = await authenticateUser({ username, password });
            const loginResponse = apiResponse?.data || apiResponse;

            if (!loginResponse?.token) {
                throw new Error('No token received from server');
            }

            const token = loginResponse.token;
            const decodedToken = jwtDecode(token);

            const userData = {
                id: decodedToken.userId,
                userId: decodedToken.userId,
                username: decodedToken.sub,
                userType: decodedToken.role,
                role: decodedToken.role
            };

            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(userData);

            return userData;
        } catch (error) {
            console.error('Authentication error:', error);

            const formattedError = new Error();
            if (isAuthenticationError(error)) {
                formattedError.message = error.message || 'Invalid username or password';
            } else {
                formattedError.message = formatErrorMessage(error) || 'Login failed';
            }
            throw formattedError;
        }
    };

    /**
     * OAuth2-specific login function
     * Handles authentication with pre-validated user data and JWT token
     * 
     * @param {Object} userData - User object from decoded JWT
     * @param {string} token - JWT token from OAuth2 backend
     */
    const loginWithOAuth2 = (userData, token) => {
        try {
            // Store token and user data
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));
            
            // Update context state
            setUser(userData);
            
            console.log('OAuth2 login successful:', userData);
        } catch (error) {
            console.error('OAuth2 login error:', error);
            throw new Error('Failed to complete OAuth2 login');
        }
    };

    const logout = async () => {
        const token = localStorage.getItem('token');

        // Clear local state first so no further authenticated requests are made
        setUser(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');

        if (token) {
            try {
                await api.post('/auth/logout', {}, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
            } catch (error) {
                // Ignore — token may already be expired/blacklisted
                console.warn('Logout request failed (token may already be invalid):', error.message);
            }
        }
    };

    const value = {
        user,
        login,
        loginWithOAuth2,
        logout,
        loading,
        isAuthenticated: !!user,
        isAdmin: user?.userType?.toUpperCase() === 'ADMIN',
        isCustomer: user?.userType?.toUpperCase() === 'CUSTOMER',
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
