import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getProductsWithFilters } from '../services/productService';
import { getAllCategories } from '../services/categoryService';
import { useAuth } from '../context/AuthContext';
import PublicNavbar from '../components/PublicNavbar';
import { 
    ShoppingCart, Search, ChevronDown, ChevronLeft, ChevronRight, 
    Star, TrendingUp, Sparkles, ArrowRight 
} from 'lucide-react';
import { showErrorAlert, showWarningToast } from '../utils/errorHandler';

const LandingPage = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [priceRange, setPriceRange] = useState({ min: '', max: '' });
    const [sortBy, setSortBy] = useState('productName');
    const [sortDirection, setSortDirection] = useState('ASC');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const { user } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (user) {
            const role = (user.userType || user.role || '').toUpperCase();
            if (role === 'ADMIN') {
                navigate('/admin/dashboard', { replace: true });
            } else if (role === 'CUSTOMER') {
                navigate('/customer/dashboard', { replace: true });
            }
        }
    }, [user, navigate]);

    const loadProducts = useCallback(async () => {
        try {
            setLoading(true);
            const filters = {
                page: currentPage,
                size: 12,
                isActive: true,
                sortBy: sortBy,
                sortDirection: sortDirection,
                searchTerm: searchTerm || undefined,
                categoryId: selectedCategory === 'all' ? undefined : parseInt(selectedCategory),
                minPrice: priceRange.min || undefined,
                maxPrice: priceRange.max || undefined,
            };

            const response = await getProductsWithFilters(filters);
            setProducts(response.data?.content || []);
            setTotalPages(response.data?.totalPages || 0);
            setTotalElements(response.data?.totalElements || 0);
        } catch (error) {
            console.error('Error loading products:', error);
            showErrorAlert(error, 'Failed to load products');
        } finally {
            setLoading(false);
        }
    }, [currentPage, searchTerm, selectedCategory, priceRange, sortBy, sortDirection]);

    useEffect(() => {
        loadProducts();
    }, [loadProducts]);

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const res = await getAllCategories();
                setCategories(res.data || []);
            } catch (error) {
                console.error('Error loading categories:', error);
            }
        };
        fetchCategories();
    }, []);

    const handleAddToCart = async (e, productId) => {
        e.stopPropagation();
        showWarningToast('Please sign in to add items to cart');
        setTimeout(() => navigate('/login'), 1500);
    };

    const handleSortChange = (value) => {
        const [field, direction] = value.split('-');
        setSortBy(field);
        setSortDirection(direction.toUpperCase());
        setCurrentPage(0);
    };

    const applyFilters = () => {
        setCurrentPage(0);
        loadProducts();
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <PublicNavbar />

            {/* Hero Section */}
            <div className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white">
                <div className="max-w-7xl mx-auto px-4 py-16 sm:py-20">
                    <div className="text-center">
                        <div className="flex items-center justify-center gap-2 mb-4">
                            <Sparkles className="w-6 h-6 text-yellow-300 animate-pulse" />
                            <span className="text-sm font-semibold text-blue-100 uppercase tracking-wider">
                                Welcome to ShopJoy
                            </span>
                        </div>
                        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold mb-6 leading-tight">
                            Discover Amazing Products<br />
                            <span className="text-blue-200">For Every Need</span>
                        </h1>
                        <p className="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
                            Browse thousands of products with confidence. Quality guaranteed.
                        </p>
                        {!user && (
                            <div className="flex flex-col sm:flex-row gap-4 justify-center">
                                <button
                                    onClick={() => navigate('/register')}
                                    className="px-8 py-3 bg-white text-blue-600 font-semibold rounded-lg hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl"
                                >
                                    Get Started Free
                                </button>
                                <button
                                    onClick={() => navigate('/login')}
                                    className="px-8 py-3 bg-blue-700 text-white font-semibold rounded-lg hover:bg-blue-800 transition-all border border-blue-500"
                                >
                                    Sign In
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 py-8">
                {/* Stats Bar */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 text-center">
                        <div className="flex items-center justify-center gap-2 text-blue-600 mb-2">
                            <TrendingUp className="w-5 h-5" />
                            <span className="text-2xl font-bold">{totalElements}</span>
                        </div>
                        <p className="text-gray-600 text-sm">Products Available</p>
                    </div>
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 text-center">
                        <div className="flex items-center justify-center gap-2 text-green-600 mb-2">
                            <Star className="w-5 h-5 fill-current" />
                            <span className="text-2xl font-bold">{categories.length}</span>
                        </div>
                        <p className="text-gray-600 text-sm">Categories</p>
                    </div>
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 text-center">
                        <div className="flex items-center justify-center gap-2 text-purple-600 mb-2">
                            <Sparkles className="w-5 h-5" />
                            <span className="text-2xl font-bold">100%</span>
                        </div>
                        <p className="text-gray-600 text-sm">Quality Guaranteed</p>
                    </div>
                </div>

                {/* Filters */}
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-gray-700">Search</label>
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <input
                                    type="text"
                                    placeholder="Search products..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    onKeyPress={(e) => e.key === 'Enter' && applyFilters()}
                                    className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-gray-700">Category</label>
                            <div className="relative">
                                <select
                                    value={selectedCategory}
                                    onChange={(e) => setSelectedCategory(e.target.value)}
                                    className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                                >
                                    <option value="all">All Categories</option>
                                    {categories.map(cat => (
                                        <option key={cat.categoryId} value={cat.categoryId}>
                                            {cat.categoryName}
                                        </option>
                                    ))}
                                </select>
                                <ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 pointer-events-none" />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-gray-700">Min Price</label>
                            <input
                                type="number"
                                placeholder="$0"
                                value={priceRange.min}
                                onChange={(e) => setPriceRange({ ...priceRange, min: e.target.value })}
                                className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-gray-700">Max Price</label>
                            <input
                                type="number"
                                placeholder="Any"
                                value={priceRange.max}
                                onChange={(e) => setPriceRange({ ...priceRange, max: e.target.value })}
                                className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-semibold text-gray-700">Sort By</label>
                            <select
                                onChange={(e) => handleSortChange(e.target.value)}
                                value={`${sortBy}-${sortDirection.toLowerCase()}`}
                                className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                            >
                                <option value="productName-asc">Name (A-Z)</option>
                                <option value="productName-desc">Name (Z-A)</option>
                                <option value="price-asc">Price (Low to High)</option>
                                <option value="price-desc">Price (High to Low)</option>
                            </select>
                        </div>
                    </div>

                    <div className="mt-4 flex justify-end">
                        <button
                            onClick={applyFilters}
                            className="px-6 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors"
                        >
                            Apply Filters
                        </button>
                    </div>
                </div>

                {/* Products Grid */}
                {loading ? (
                    <div className="flex justify-center items-center py-20">
                        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : products.length === 0 ? (
                    <div className="text-center py-20">
                        <div className="text-gray-400 mb-4">
                            <Search className="w-16 h-16 mx-auto" />
                        </div>
                        <h3 className="text-xl font-semibold text-gray-700 mb-2">No products found</h3>
                        <p className="text-gray-500">Try adjusting your filters or search terms</p>
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
                            {products.map(product => (
                                <div
                                    key={product.productId}
                                    className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all group"
                                >
                                    <div className="aspect-square bg-gray-100 relative overflow-hidden">
                                        {product.imageUrl ? (
                                            <img
                                                src={product.imageUrl}
                                                alt={product.productName}
                                                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                            />
                                        ) : (
                                            <div className="w-full h-full flex items-center justify-center text-gray-400">
                                                <ShoppingCart className="w-16 h-16" />
                                            </div>
                                        )}
                                        {product.stockQuantity < 10 && product.stockQuantity > 0 && (
                                            <div className="absolute top-2 right-2 bg-orange-500 text-white text-xs font-bold px-2 py-1 rounded-full">
                                                Only {product.stockQuantity} left
                                            </div>
                                        )}
                                        {product.stockQuantity === 0 && (
                                            <div className="absolute inset-0 bg-gray-900/50 flex items-center justify-center">
                                                <span className="bg-red-500 text-white px-4 py-2 rounded-lg font-semibold">
                                                    Out of Stock
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                    <div className="p-4">
                                        <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2 group-hover:text-blue-600 transition-colors">
                                            {product.productName}
                                        </h3>
                                        <p className="text-sm text-gray-500 mb-3 line-clamp-2">
                                            {product.description}
                                        </p>
                                        <div className="flex items-center justify-between">
                                            <span className="text-2xl font-bold text-blue-600">
                                                ${product.price?.toFixed(2)}
                                            </span>
                                            <button
                                                onClick={(e) => handleAddToCart(e, product.productId)}
                                                disabled={product.stockQuantity === 0}
                                                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors text-sm font-medium"
                                            >
                                                <ShoppingCart className="w-4 h-4" />
                                                Add
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Pagination */}
                        {totalPages > 1 && (
                            <div className="flex items-center justify-between bg-white rounded-xl p-4 shadow-sm border border-gray-100">
                                <button
                                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                    disabled={currentPage === 0}
                                    className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                >
                                    <ChevronLeft className="w-4 h-4" />
                                    Previous
                                </button>
                                <span className="text-sm text-gray-600">
                                    Page <span className="font-semibold">{currentPage + 1}</span> of{' '}
                                    <span className="font-semibold">{totalPages}</span>
                                </span>
                                <button
                                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                                    disabled={currentPage >= totalPages - 1}
                                    className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                >
                                    Next
                                    <ChevronRight className="w-4 h-4" />
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>

            {/* Footer CTA */}
            {!user && (
                <div className="bg-gradient-to-r from-blue-600 to-indigo-700 mt-16">
                    <div className="max-w-7xl mx-auto px-4 py-16 text-center text-white">
                        <h2 className="text-3xl font-bold mb-4">Ready to Start Shopping?</h2>
                        <p className="text-blue-100 mb-8 text-lg">
                            Create an account to unlock exclusive deals and personalized recommendations
                        </p>
                        <button
                            onClick={() => navigate('/register')}
                            className="inline-flex items-center gap-2 px-8 py-3 bg-white text-blue-600 font-semibold rounded-lg hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl"
                        >
                            Sign Up Now
                            <ArrowRight className="w-5 h-5" />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LandingPage;
