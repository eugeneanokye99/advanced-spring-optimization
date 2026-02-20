import { Link } from 'react-router-dom';
import { ShoppingBag } from 'lucide-react';

const PublicNavbar = () => {
    return (
        <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    <Link to="/" className="flex items-center space-x-2 group">
                        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-2 rounded-lg group-hover:shadow-md transition-shadow">
                            <ShoppingBag className="w-6 h-6 text-white" />
                        </div>
                        <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                            ShopJoy
                        </span>
                    </Link>

                    <div className="flex items-center space-x-4">
                        <Link
                            to="/login"
                            className="px-4 py-2 text-gray-700 hover:text-blue-600 font-medium transition-colors"
                        >
                            Sign In
                        </Link>
                        <Link
                            to="/register"
                            className="px-6 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-lg hover:shadow-lg transition-all"
                        >
                            Get Started
                        </Link>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default PublicNavbar;
