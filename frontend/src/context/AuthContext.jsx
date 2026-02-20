import { createContext, useContext, useState, useEffect } from 'react';
import { authenticateUser } from '../services/userService';
import { formatErrorMessage, isAuthenticationError } from '../utils/errorHandler';
import { jwtDecode } from 'jwt-decode';

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
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
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
                // Invalid user object, clear session
                localStorage.removeItem('user');
                setUser(null);
            }
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        try {
            const response = await authenticateUser({ username, password });
            const loginResponse = response.data || response;
            
            if (!loginResponse.token) {
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

            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
            localStorage.setItem('token', token);
            
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

    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
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
