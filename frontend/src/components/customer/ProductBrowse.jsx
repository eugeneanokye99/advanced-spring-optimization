import { useState, useEffect, useCallback } from 'react';
import { getProductsWithFilters } from '../../services/productService';
import { getAllCategories } from '../../services/categoryService';
import { addToCart } from '../../services/cartService';
import { useAuth } from '../../context/AuthContext';
import { ShoppingCart, Search, ChevronDown, Package, ChevronLeft, ChevronRight, Filter } from 'lucide-react';
import ProductDetailModal from './ProductDetailModal';
import { showErrorAlert, showSuccessToast, showWarningToast, isInsufficientStockError } from '../../utils/errorHandler';

const ProductBrowse = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [priceRange, setPriceRange] = useState({ min: '', max: '' });
    const [sortBy, setSortBy] = useState('product_name');
    const [sortDirection, setSortDirection] = useState('ASC');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [algorithm, setAlgorithm] = useState('DATABASE');
    const { user } = useAuth();

    const loadProducts = useCallback(async () => {
        try {
            setLoading(true);
            const filters = {
                page: currentPage,
                size: 8,
                isActive: true,
                sortBy: sortBy,
                sortDirection: sortDirection,
                algorithm: algorithm,
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
            showErrorAlert(error, 'Failed to load products. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [currentPage, searchTerm, selectedCategory, priceRange, sortBy, sortDirection, algorithm]);

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
                showErrorAlert(error, 'Failed to load categories');
            }
        };
        fetchCategories();
    }, []);

    const handleAddToCart = async (e, productId) => {
        e.stopPropagation();
        if (!user) {
            showWarningToast('Please login to add items to cart');
            return;
        }
        try {
            await addToCart({ userId: user.userId, productId, quantity: 1 });
            showSuccessToast('Product added to cart!');
        } catch (error) {
            console.error('Error adding to cart:', error);
            
            if (isInsufficientStockError(error)) {
                showErrorAlert(error, 'This item is out of stock');
            } else {
                showErrorAlert(error, 'Failed to add item to cart');
            }
        }
    };

    const handleSortChange = (value) => {
        const [field, direction] = value.split('-');
        setSortBy(field);
        setSortDirection(direction.toUpperCase());
        setCurrentPage(0);
    };

    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">Discover Products</h1>
                    <p className="text-gray-500 mt-1">Explore our premium selection curated just for you.</p>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                    <span className="font-medium text-gray-900">{totalElements}</span> products found
                </div>
            </div>

            {/* Filters Section */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Search</label>
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                            <input
                                type="text"
                                placeholder="Search by name..."
                                value={searchTerm}
                                onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
                                className="w-full pl-10 pr-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Category</label>
                        <div className="relative">
                            <select
                                value={selectedCategory}
                                onChange={(e) => { setSelectedCategory(e.target.value); setCurrentPage(0); }}
                                className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all"
                            >
                                <option value="all">All Categories</option>
                                {categories.map(cat => (
                                    <option key={cat.categoryId} value={cat.categoryId}>{cat.categoryName}</option>
                                ))}
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 pointer-events-none" />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Price Range</label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                placeholder="Min"
                                value={priceRange.min}
                                onChange={(e) => { setPriceRange({ ...priceRange, min: e.target.value }); setCurrentPage(0); }}
                                className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all"
                            />
                            <span className="text-gray-400">-</span>
                            <input
                                type="number"
                                placeholder="Max"
                                value={priceRange.max}
                                onChange={(e) => { setPriceRange({ ...priceRange, max: e.target.value }); setCurrentPage(0); }}
                                className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Sort By</label>
                        <div className="relative">
                            <select
                                onChange={(e) => handleSortChange(e.target.value)}
                                className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all"
                            >
                                <option value="product_name-asc">Name: A to Z</option>
                                <option value="product_name-desc">Name: Z to A</option>
                                <option value="price-asc">Price: Low to High</option>
                                <option value="price-desc">Price: High to Low</option>
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 pointer-events-none" />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-semibold text-gray-700 ml-1">Sorting Algorithm</label>
                        <div className="relative">
                            <select
                                value={algorithm}
                                onChange={(e) => { setAlgorithm(e.target.value); setCurrentPage(0); }}
                                className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary-500 text-sm transition-all font-mono text-xs md:text-sm"
                            >
                                <option value="DATABASE">Default (Database)</option>
                                <option value="QUICKSORT">QuickSort (O(n log n))</option>
                                <option value="MERGESORT">MergeSort (Stable)</option>
                                <option value="HEAPSORT">HeapSort (Low Memory)</option>
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 pointer-events-none" />
                        </div>
                    </div>
                </div>
            </div>

            {loading ? (
                <div className="flex flex-col items-center justify-center py-24">
                    <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mb-4"></div>
                    <p className="text-gray-500 font-medium">Loading amazing products...</p>
                </div>
            ) : products.length === 0 ? (
                <div className="text-center py-24 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
                    <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 rounded-full mb-4">
                        <Search className="w-10 h-10 text-gray-400" />
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">No products match your criteria</h3>
                    <p className="text-gray-500 mb-6">Try adjusting your filters or searching for something else.</p>
                </div>
            ) : (
                <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                        {products.map((product) => (
                            <div
                                key={product.productId}
                                onClick={() => setSelectedProduct(product)}
                                className="group bg-white rounded-2xl shadow-sm hover:shadow-xl border border-gray-100 transition-all duration-300 overflow-hidden flex flex-col cursor-pointer"
                            >
                                <div className="relative h-56 bg-gradient-to-br from-gray-50 to-gray-100 overflow-hidden">
                                    {product.imageUrl ? (
                                        <img
                                            src={product.imageUrl}
                                            alt={product.productName}
                                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center text-gray-300">
                                            <Package className="w-16 h-16" />
                                        </div>
                                    )}
                                    <div className="absolute top-3 left-3">
                                        <span className="px-3 py-1 bg-white/90 backdrop-blur-sm shadow-sm rounded-full text-[10px] font-bold text-gray-600 uppercase tracking-wider">
                                            {product.categoryName || 'General'}
                                        </span>
                                    </div>
                                </div>

                                <div className="p-5 flex-1 flex flex-col">
                                    <h3 className="font-bold text-gray-900 mb-2 truncate group-hover:text-primary-600 transition-colors">
                                        {product.productName}
                                    </h3>
                                    <p className="text-sm text-gray-500 mb-4 line-clamp-2 leading-relaxed">
                                        {product.description}
                                    </p>

                                    <div className="mt-auto pt-4 border-t border-gray-50 flex items-center justify-between">
                                        <div className="flex flex-col">
                                            <span className="text-xs text-gray-400 font-medium uppercase tracking-tighter">Price</span>
                                            <span className="text-2xl font-black text-gray-900">${product.price.toFixed(2)}</span>
                                        </div>
                                        <button
                                            onClick={(e) => handleAddToCart(e, product.productId)}
                                            disabled={product.stockQuantity === 0}
                                            className={`p-3 rounded-xl shadow-lg transition-all duration-300 ${product.stockQuantity === 0
                                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                                : 'bg-primary-600 text-black hover:bg-primary-700 hover:-translate-y-1 active:scale-95 shadow-primary-200'
                                                }`}
                                        >
                                            <ShoppingCart className="w-5 h-5" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Pagination Controls */}
                    {totalPages > 1 && (
                        <div className="mt-12 flex items-center justify-center gap-4">
                            <button
                                onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                                disabled={currentPage === 0}
                                className="p-2 rounded-xl bg-white border border-gray-100 shadow-sm disabled:opacity-50 hover:bg-gray-50 transition-colors"
                            >
                                <ChevronLeft className="w-6 h-6" />
                            </button>
                            <div className="flex gap-2">
                                {[...Array(totalPages)].map((_, i) => (
                                    <button
                                        key={i}
                                        onClick={() => setCurrentPage(i)}
                                        className={`w-10 h-10 rounded-xl font-bold transition-all ${currentPage === i
                                            ? 'bg-primary-600 text-black shadow-lg shadow-primary-200'
                                            : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-100'
                                            }`}
                                    >
                                        {i + 1}
                                    </button>
                                ))}
                            </div>
                            <button
                                onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={currentPage === totalPages - 1}
                                className="p-2 rounded-xl bg-white border border-gray-100 shadow-sm disabled:opacity-50 hover:bg-gray-50 transition-colors"
                            >
                                <ChevronRight className="w-6 h-6" />
                            </button>
                        </div>
                    )}
                </>
            )}

            {selectedProduct && (
                <ProductDetailModal
                    product={selectedProduct}
                    onClose={() => setSelectedProduct(null)}
                />
            )}
        </div>
    );
};

export default ProductBrowse;
