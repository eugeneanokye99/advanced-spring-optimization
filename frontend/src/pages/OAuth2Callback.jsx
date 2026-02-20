import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {jwtDecode} from 'jwt-decode';

/**
 * OAuth2Callback Component
 * 
 * Handles the redirect callback from backend OAuth2 authentication.
 * Extracts JWT token from URL query parameters, validates it,
 * updates auth context, and redirects to appropriate dashboard.
 * 
 * URL format: /oauth2/callback?token={jwt}&provider={provider}
 * Error format: /oauth2/callback?error={errorCode}
 */
const OAuth2Callback = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { loginWithOAuth2 } = useAuth();
    const [status, setStatus] = useState('processing'); // processing | success | error
    const [message, setMessage] = useState('Processing authentication...');

    useEffect(() => {
        const processOAuth2Callback = async () => {
            try {
                // Check for error parameter first
                const error = searchParams.get('error');
                if (error) {
                    setStatus('error');
                    setMessage(getErrorMessage(error));
                    setTimeout(() => {
                        navigate('/login', { 
                            state: { error: getErrorMessage(error) } 
                        });
                    }, 3000);
                    return;
                }

                // Extract token and provider from URL
                const token = searchParams.get('token');
                const provider = searchParams.get('provider');

                // Validate token presence
                if (!token) {
                    throw new Error('No authentication token received');
                }

                // Decode JWT to extract user information
                let decodedToken;
                try {
                    decodedToken = jwtDecode(token);
                } catch (decodeError) {
                    console.error('Failed to decode JWT:', decodeError);
                    throw new Error('Invalid authentication token');
                }

                // Validate token expiration
                const currentTime = Math.floor(Date.now() / 1000);
                if (decodedToken.exp && decodedToken.exp < currentTime) {
                    throw new Error('Authentication token has expired');
                }

                const userId = decodedToken.userId;
                const username = decodedToken.sub;
                const role = decodedToken.role;

                if (!userId || !username || !role) {
                    throw new Error('Invalid token structure - missing required fields');
                }

                const user = {
                    id: userId,
                    userId: userId,
                    username,
                    userType: role,
                    role: role,
                    oauthProvider: provider || 'google'
                };

                loginWithOAuth2(user, token);

                // Show success message
                setStatus('success');
                setMessage(`Welcome back, ${username}!`);

                // Redirect to appropriate dashboard based on role
                setTimeout(() => {
                    if (role === 'ADMIN') {
                        navigate('/admin/dashboard');
                    } else {
                        navigate('/customer/dashboard');
                    }
                }, 1500);

            } catch (error) {
                console.error('OAuth2 callback processing error:', error);
                setStatus('error');
                setMessage(error.message || 'Authentication failed. Please try again.');
                
                // Redirect to login after 3 seconds
                setTimeout(() => {
                    navigate('/login', { 
                        state: { error: error.message } 
                    });
                }, 3000);
            }
        };

        processOAuth2Callback();
    }, [searchParams, navigate, loginWithOAuth2]);

    /**
     * Maps error codes to user-friendly messages
     */
    const getErrorMessage = (errorCode) => {
        const errorMessages = {
            'oauth2_failed': 'OAuth2 authentication failed. Please try again.',
            'no_email': 'Unable to retrieve email from OAuth2 provider. Please ensure email permission is granted.',
            'user_creation_failed': 'Failed to create user account. Please try again or contact support.',
            'token_generation_failed': 'Failed to generate authentication token. Please try again.',
            'invalid_provider': 'Invalid OAuth2 provider. Please use a supported authentication method.',
            'access_denied': 'Access was denied. Please grant necessary permissions to continue.',
            'server_error': 'Server error occurred during authentication. Please try again later.'
        };

        return errorMessages[errorCode] || 'An unexpected error occurred. Please try again.';
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <div className="max-w-md w-full">
                <div className="bg-white rounded-lg shadow-lg p-8">
                    {/* Status Icon */}
                    <div className="flex justify-center mb-6">
                        {status === 'processing' && (
                            <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                        )}
                        {status === 'success' && (
                            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                                <svg className="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                            </div>
                        )}
                        {status === 'error' && (
                            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center">
                                <svg className="w-10 h-10 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </div>
                        )}
                    </div>

                    {/* Status Message */}
                    <div className="text-center">
                        <h2 className={`text-2xl font-bold mb-2 ${
                            status === 'success' ? 'text-green-700' :
                            status === 'error' ? 'text-red-700' :
                            'text-gray-800'
                        }`}>
                            {status === 'processing' && 'Authenticating...'}
                            {status === 'success' && 'Success!'}
                            {status === 'error' && 'Authentication Failed'}
                        </h2>
                        <p className="text-gray-600 mb-6">
                            {message}
                        </p>

                        {status === 'processing' && (
                            <div className="text-sm text-gray-500">
                                Please wait while we complete your sign in...
                            </div>
                        )}

                        {status === 'error' && (
                            <button
                                onClick={() => navigate('/login')}
                                className="mt-4 px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                            >
                                Return to Login
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OAuth2Callback;
