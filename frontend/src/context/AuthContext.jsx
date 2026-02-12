import { createContext, useContext, useState, useEffect } from 'react';
import { authenticateUser } from '../services/userService';
import { formatErrorMessage, isAuthenticationError } from '../utils/errorHandler';

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
            setUser(JSON.parse(storedUser));
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        try {
            const response = await authenticateUser({ username, password });
            const userData = response.data || response;
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
            return userData;
        } catch (error) {
            console.error('Authentication error:', error);
            
            // Format error message for better user experience
            const formattedError = new Error();
            
            if (isAuthenticationError(error)) {
                formattedError.message = error.message || 'Invalid username or password';
            } else {
                formattedError.message = formatErrorMessage(error) || 'Login failed';
            }
            
            throw formattedError;
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
    };

    const value = {
        user,
        login,
        logout,
        loading,
        isAuthenticated: !!user,
        isAdmin: user?.userType?.toUpperCase() === 'ADMIN',
        isCustomer: user?.userType?.toUpperCase() === 'CUSTOMER',
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
