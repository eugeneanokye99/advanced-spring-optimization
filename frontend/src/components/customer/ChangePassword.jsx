import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { changePassword } from '../../services/userService';
import toast from 'react-hot-toast';
import { Lock, Eye, EyeOff, Shield, AlertCircle } from 'lucide-react';

const ChangePassword = () => {
    const { user } = useAuth();
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [passwordStrength, setPasswordStrength] = useState({
        hasLength: false,
        hasUppercase: false,
        hasLowercase: false,
        hasNumber: false
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));

        // Check password strength for new password
        if (name === 'newPassword') {
            setPasswordStrength({
                hasLength: value.length >= 8,
                hasUppercase: /[A-Z]/.test(value),
                hasLowercase: /[a-z]/.test(value),
                hasNumber: /\d/.test(value)
            });
        }
    };

    const isPasswordValid = () => {
        return Object.values(passwordStrength).every(Boolean);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validation
        if (!formData.currentPassword) {
            toast.error('Please enter your current password');
            return;
        }

        if (!formData.newPassword) {
            toast.error('Please enter a new password');
            return;
        }

        if (!isPasswordValid()) {
            toast.error('New password does not meet requirements');
            return;
        }

        if (formData.newPassword !== formData.confirmPassword) {
            toast.error('New passwords do not match');
            return;
        }

        if (formData.currentPassword === formData.newPassword) {
            toast.error('New password must be different from current password');
            return;
        }

        setLoading(true);

        try {
            const payload = {
                currentPassword: formData.currentPassword,
                newPassword: formData.newPassword
            };

            await changePassword(user.id, payload);
            toast.success('Password changed successfully');
            
            // Reset form
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setPasswordStrength({
                hasLength: false,
                hasUppercase: false,
                hasLowercase: false,
                hasNumber: false
            });
        } catch (error) {
            const errorMessage = error.response?.data?.message 
                || error.response?.data?.error 
                || 'Failed to change password';
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto">
            {/* Header */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
                    <Shield className="w-8 h-8 text-primary-600" />
                    Change Password
                </h1>
                <p className="mt-2 text-gray-600">
                    Keep your account secure by using a strong, unique password
                </p>
            </div>

            {/* Security Notice */}
            <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg flex gap-3">
                <AlertCircle className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                <div className="text-sm text-blue-800">
                    <p className="font-medium mb-1">Password Security Tips:</p>
                    <ul className="list-disc list-inside space-y-1">
                        <li>Use a unique password that you don't use elsewhere</li>
                        <li>Never share your password with anyone</li>
                        <li>Consider using a password manager</li>
                    </ul>
                </div>
            </div>

            {/* Change Password Form */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Current Password */}
                    <div>
                        <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-2">
                            Current Password
                        </label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <Lock className="h-5 w-5 text-gray-400" />
                            </div>
                            <input
                                type={showCurrentPassword ? 'text' : 'password'}
                                id="currentPassword"
                                name="currentPassword"
                                value={formData.currentPassword}
                                onChange={handleChange}
                                className="block w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                                placeholder="Enter your current password"
                                required
                            />
                            <button
                                type="button"
                                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                            >
                                {showCurrentPassword ? (
                                    <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                ) : (
                                    <Eye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                )}
                            </button>
                        </div>
                    </div>

                    {/* New Password */}
                    <div>
                        <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-2">
                            New Password
                        </label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <Lock className="h-5 w-5 text-gray-400" />
                            </div>
                            <input
                                type={showNewPassword ? 'text' : 'password'}
                                id="newPassword"
                                name="newPassword"
                                value={formData.newPassword}
                                onChange={handleChange}
                                className="block w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                                placeholder="Enter your new password"
                                required
                            />
                            <button
                                type="button"
                                onClick={() => setShowNewPassword(!showNewPassword)}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                            >
                                {showNewPassword ? (
                                    <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                ) : (
                                    <Eye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                )}
                            </button>
                        </div>
                        
                        {/* Password Strength Indicator */}
                        {formData.newPassword && (
                            <div className="mt-3 space-y-2">
                                <p className="text-sm font-medium text-gray-700">Password Requirements:</p>
                                <div className="space-y-1">
                                    <div className={`flex items-center text-sm ${passwordStrength.hasLength ? 'text-green-600' : 'text-gray-500'}`}>
                                        <div className={`w-4 h-4 rounded-full mr-2 flex items-center justify-center ${passwordStrength.hasLength ? 'bg-green-100' : 'bg-gray-100'}`}>
                                            {passwordStrength.hasLength && '✓'}
                                        </div>
                                        At least 8 characters
                                    </div>
                                    <div className={`flex items-center text-sm ${passwordStrength.hasUppercase ? 'text-green-600' : 'text-gray-500'}`}>
                                        <div className={`w-4 h-4 rounded-full mr-2 flex items-center justify-center ${passwordStrength.hasUppercase ? 'bg-green-100' : 'bg-gray-100'}`}>
                                            {passwordStrength.hasUppercase && '✓'}
                                        </div>
                                        One uppercase letter
                                    </div>
                                    <div className={`flex items-center text-sm ${passwordStrength.hasLowercase ? 'text-green-600' : 'text-gray-500'}`}>
                                        <div className={`w-4 h-4 rounded-full mr-2 flex items-center justify-center ${passwordStrength.hasLowercase ? 'bg-green-100' : 'bg-gray-100'}`}>
                                            {passwordStrength.hasLowercase && '✓'}
                                        </div>
                                        One lowercase letter
                                    </div>
                                    <div className={`flex items-center text-sm ${passwordStrength.hasNumber ? 'text-green-600' : 'text-gray-500'}`}>
                                        <div className={`w-4 h-4 rounded-full mr-2 flex items-center justify-center ${passwordStrength.hasNumber ? 'bg-green-100' : 'bg-gray-100'}`}>
                                            {passwordStrength.hasNumber && '✓'}
                                        </div>
                                        One number
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Confirm New Password */}
                    <div>
                        <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                            Confirm New Password
                        </label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <Lock className="h-5 w-5 text-gray-400" />
                            </div>
                            <input
                                type={showConfirmPassword ? 'text' : 'password'}
                                id="confirmPassword"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className="block w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                                placeholder="Confirm your new password"
                                required
                            />
                            <button
                                type="button"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                            >
                                {showConfirmPassword ? (
                                    <EyeOff className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                ) : (
                                    <Eye className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                                )}
                            </button>
                        </div>
                        {formData.confirmPassword && formData.newPassword !== formData.confirmPassword && (
                            <p className="mt-1 text-sm text-red-600">Passwords do not match</p>
                        )}
                    </div>

                    {/* Submit Button */}
                    <div className="flex items-center justify-end gap-4 pt-4">
                        <button
                            type="button"
                            onClick={() => setFormData({
                                currentPassword: '',
                                newPassword: '',
                                confirmPassword: ''
                            })}
                            className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading || !isPasswordValid() || formData.newPassword !== formData.confirmPassword}
                            className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
                        >
                            {loading ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                    Changing Password...
                                </>
                            ) : (
                                <>
                                    <Shield className="w-4 h-4" />
                                    Change Password
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ChangePassword;
