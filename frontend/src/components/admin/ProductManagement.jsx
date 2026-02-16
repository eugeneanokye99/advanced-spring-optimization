import { useState, useEffect } from 'react';
import {
    Plus,
    Edit,
    Trash2,
    Search,
    Filter,
    ChevronLeft,
    ChevronRight,
    ChevronDown,
    CheckCircle,
    XCircle,
} from 'lucide-react';
import {
    getProductsPaginated,
    getProductsWithFilters,
    createProduct,
    updateProduct,
    deleteProduct,
    activateProduct,
    deactivateProduct,
} from '../../services/productService';
import { getAllCategories } from '../../services/categoryService';
import { showErrorAlert, formatErrorMessage, extractFieldErrors, isValidationError, showSuccessToast } from '../../utils/errorHandler';

const ProductManagement = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [priceRange, setPriceRange] = useState({ min: '', max: '' });
    const [sortBy, setSortBy] = useState('id');
    const [sortDirection, setSortDirection] = useState('ASC');
    const [statusFilter, setStatusFilter] = useState('all');
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [formErrors, setFormErrors] = useState({});
    const [formData, setFormData] = useState({
        productName: '',
        description: '',
        price: '',
        costPrice: '',
        categoryId: '',
        sku: '',
        brand: '',
        initialStock: '',
        imageUrl: '',
    });

    useEffect(() => {
        loadCategories();
    }, []);

    useEffect(() => {
        loadProducts();
    }, [currentPage, searchTerm, selectedCategory, priceRange, sortBy, sortDirection, statusFilter]);

    const loadProducts = async () => {
        try {
            setLoading(true);
            const filters = {
                page: currentPage,
                size: 10,
                searchTerm: searchTerm || undefined,
                categoryId: selectedCategory === 'all' ? undefined : parseInt(selectedCategory),
                minPrice: priceRange.min || undefined,
                maxPrice: priceRange.max || undefined,
                sortBy: sortBy,
                sortDirection: sortDirection,
                isActive: statusFilter === 'all' ? undefined : statusFilter === 'active'
            };
            const response = await getProductsWithFilters(filters);
            console.log(response.data);
            setProducts(response.data?.content || []);
            setTotalPages(response.data?.totalPages || 0);
            setTotalElements(response.data?.totalElements || 0);
        } catch (error) {
            console.error('Error loading products:', error);
            // Only show error to user if it's not just an empty result
            if (error.message && !error.message.includes('No products found')) {
                showErrorAlert(error, 'Failed to load products');
            }
        } finally {
            setLoading(false);
        }
    };

    const loadCategories = async () => {
        try {
            const response = await getAllCategories();
            setCategories(response.data || []);
            
        } catch (error) {
            console.error('Error loading categories:', error);
            showErrorAlert(error, 'Failed to load categories');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setFormErrors({});
        
        try {
            const productData = {
                ...formData,
                price: parseFloat(formData.price),
                costPrice: formData.costPrice ? parseFloat(formData.costPrice) : 0,
                categoryId: parseInt(formData.categoryId),
                initialStock: parseInt(formData.initialStock || '0'),
            };

            if (editingProduct) {
                await updateProduct(editingProduct.id, productData);
                showSuccessToast('Product updated successfully!');
            } else {
                await createProduct(productData);
                showSuccessToast('Product created successfully!');
            }

            setShowModal(false);
            resetForm();
            loadProducts();
        } catch (error) {
            console.error('Error saving product:', error);
            
            // Handle validation errors
            if (isValidationError(error)) {
                const fieldErrors = extractFieldErrors(error);
                setFormErrors(fieldErrors);
                
                if (Object.keys(fieldErrors).length === 0) {
                    showErrorAlert(error, 'Please check the form for errors');
                }
            } else {
                showErrorAlert(error, editingProduct ? 'Failed to update product' : 'Failed to create product');
            }
        }
    };

    const handleEdit = (product) => {
        setEditingProduct(product);
        setFormData({
            productName: product.productName,
            description: product.description,
            price: product.price,
            costPrice: product.costPrice || '',
            categoryId: product.categoryId,
            sku: product.sku,
            brand: product.brand || '',
            initialStock: '', // Stock is managed in inventory, not directly editable here for safety
            imageUrl: product.imageUrl || '',
        });
        setShowModal(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this product?')) {
            try {
                await deleteProduct(id);
                showSuccessToast('Product deleted successfully!');
                loadProducts();
            } catch (error) {
                console.error('Error deleting product:', error);
                showErrorAlert(error, 'Failed to delete product');
            }
        }
    };

    const handleToggleActive = async (product) => {
        try {
            if (product.active) {
                await deactivateProduct(product.id);
                showSuccessToast('Product deactivated successfully!');
            } else {
                await activateProduct(product.id);
                showSuccessToast('Product activated successfully!');
            }
            loadProducts();
        } catch (error) {
            console.error('Error toggling product status:', error);
            showErrorAlert(error, `Failed to ${product.active ? 'deactivate' : 'activate'} product`);
        }
    };

    const resetForm = () => {
        setFormData({
            productName: '',
            description: '',
            price: '',
            costPrice: '',
            categoryId: '',
            sku: '',
            brand: '',
            initialStock: '',
            imageUrl: '',
        });
        setFormErrors({});
        setEditingProduct(null);
    };

    const handleSortChange = (value) => {
        const [field, direction] = value.split('-');
        setSortBy(field);
        setSortDirection(direction.toUpperCase());
        setCurrentPage(0);
    };

    const handleClearFilters = () => {
        setSearchTerm('');
        setSelectedCategory('all');
        setPriceRange({ min: '', max: '' });
        setSortBy('id');
        setSortDirection('ASC');
        setStatusFilter('all');
        setCurrentPage(0);
    };

    const displayedProducts = products;


    return (
        <div>
            {/* Header */}
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Product Management</h1>
                    <p className="text-gray-600 mt-1">Manage your product catalog - {totalElements} products</p>
                </div>
                <button
                    onClick={() => {
                        resetForm();
                        setShowModal(true);
                    }}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus className="w-5 h-5" />
                    Add Product
                </button>
            </div>

            {/* Search and Filter */}
            <div className="card p-6 mb-6">
                <div className="flex flex-col lg:flex-row gap-4 mb-4">
                    {/* Search */}
                    <div className="flex-1 relative">
                        <label className="text-sm font-semibold text-gray-700 ml-1 block mb-1">Search Products</label>
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                        <input
                            type="text"
                            placeholder="Search products..."
                            value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
                            className="w-full pl-10 pr-4 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                        />
                    </div>
                    
                    {/* Category Filter */}
                    <div className="min-w-[200px]">
                        <label className="text-sm font-semibold text-gray-700 ml-1 block mb-1">Category</label>
                        <div className="relative">
                            <select
                                value={selectedCategory}
                                onChange={(e) => { setSelectedCategory(e.target.value); setCurrentPage(0); }}
                                className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                            >
                                <option value="all">All Categories</option>
                                {categories.map(cat => (
                                    <option key={cat.categoryId} value={cat.categoryId}>{cat.categoryName}</option>
                                ))}
                            </select>
                            <Filter className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4 pointer-events-none" />
                        </div>
                    </div>
                    
                    {/* Status Filter */}
                    <div className="min-w-[150px]">
                        <label className="text-sm font-semibold text-gray-700 ml-1 block mb-1">Status</label>
                        <select
                            value={statusFilter}
                            onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(0); }}
                            className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                        >
                            <option value="all">All Status</option>
                            <option value="active">Active</option>
                            <option value="inactive">Inactive</option>
                        </select>
                    </div>
                </div>
                
                <div className="flex flex-col lg:flex-row gap-4">
                    {/* Price Range */}
                    <div className="flex-1">
                        <label className="text-sm font-semibold text-gray-700 ml-1 block mb-1">Price Range</label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                placeholder="Min Price"
                                value={priceRange.min}
                                onChange={(e) => { setPriceRange({ ...priceRange, min: e.target.value }); setCurrentPage(0); }}
                                className="w-full px-3 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                            />
                            <span className="text-gray-400">-</span>
                            <input
                                type="number"
                                placeholder="Max Price"
                                value={priceRange.max}
                                onChange={(e) => { setPriceRange({ ...priceRange, max: e.target.value }); setCurrentPage(0); }}
                                className="w-full px-3 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                            />
                        </div>
                    </div>
                    
                    {/* Sort By */}
                    <div className="min-w-[200px]">
                        <label className="text-sm font-semibold text-gray-700 ml-1 block mb-1">Sort By</label>
                        <select
                            onChange={(e) => handleSortChange(e.target.value)}
                            value={`${sortBy}-${sortDirection.toLowerCase()}`}
                            className="w-full appearance-none pl-4 pr-10 py-2 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
                        >
                            <option value="id-asc">ID: Low to High</option>
                            <option value="id-desc">ID: High to Low</option>
                            <option value="productName-asc">Name: A to Z</option>
                            <option value="productName-desc">Name: Z to A</option>
                            <option value="price-asc">Price: Low to High</option>
                            <option value="price-desc">Price: High to Low</option>
                            <option value="category.id-desc">Category: Z to A</option>
                        </select>
                    </div>
                    
                    {/* Clear Filters */}
                    <div className="flex items-end">
                        <button
                            onClick={handleClearFilters}
                            className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                        >
                            Clear Filters
                        </button>
                    </div>
                </div>
            </div>

            {/* Products Table */}
            <div className="card overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Product
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Category
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Price
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Stock
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Status
                                </th>
                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center">
                                        <div className="flex justify-center">
                                            <div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
                                        </div>
                                    </td>
                                </tr>
                            ) : displayedProducts.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                        No products found
                                    </td>
                                </tr>
                            ) : (
                                displayedProducts.map((product) => (
                                    <tr key={product.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center">
                                                <img
                                                    src={product.imageUrl || 'https://via.placeholder.com/40'}
                                                    alt={product.productName}
                                                    className="h-10 w-10 flex-shrink-0 rounded-lg mr-3 object-cover bg-gray-200"
                                                    onError={(e) => { e.target.src = 'https://via.placeholder.com/40'; }}
                                                />
                                                <div>
                                                    <div className="text-sm font-medium text-gray-900">{product.productName}</div>
                                                    <div className="text-sm text-gray-500 truncate max-w-xs">
                                                        {product.description}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-gray-900">{product.categoryName}</td>
                                        <td className="px-6 py-4 text-sm text-gray-900">${product.price.toFixed(2)}</td>
                                        <td className="px-6 py-4 text-sm text-gray-900">{product.stockQuantity}</td>
                                        <td className="px-6 py-4">
                                            <button
                                                onClick={() => handleToggleActive(product)}
                                                className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${product.active
                                                    ? 'bg-green-100 text-green-800'
                                                    : 'bg-gray-100 text-gray-800'
                                                    }`}
                                            >
                                                {product.active ? (
                                                    <>
                                                        <CheckCircle className="w-3 h-3" /> Active
                                                    </>
                                                ) : (
                                                    <>
                                                        <XCircle className="w-3 h-3" /> Inactive
                                                    </>
                                                )}
                                            </button>
                                        </td>
                                        <td className="px-6 py-4 text-right text-sm font-medium">
                                            <button
                                                onClick={() => handleEdit(product)}
                                                className="text-primary-600 hover:text-primary-900 mr-3"
                                            >
                                                <Edit className="w-4 h-4" />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(product.id)}
                                                className="text-red-600 hover:text-red-900"
                                            >
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                <div className="bg-gray-50 px-6 py-4 flex items-center justify-between border-t border-gray-200">
                    <div className="text-sm text-gray-700">
                        Page {currentPage + 1} of {totalPages}
                    </div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                            disabled={currentPage === 0}
                            className="btn-secondary disabled:opacity-50"
                        >
                            <ChevronLeft className="w-4 h-4" />
                        </button>
                        <button
                            onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                            disabled={currentPage >= totalPages - 1}
                            className="btn-secondary disabled:opacity-50"
                        >
                            <ChevronRight className="w-4 h-4" />
                        </button>
                    </div>
                </div>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-gray-200">
                            <h2 className="text-2xl font-bold text-gray-900">
                                {editingProduct ? 'Edit Product' : 'Add New Product'}
                            </h2>
                        </div>
                        <form onSubmit={handleSubmit} className="p-6 space-y-4">
                            {/* Helper function to render field errors */}
                            {(() => {
                                const renderFieldError = (fieldName) => {
                                    return formErrors[fieldName] ? (
                                        <p className="text-red-600 text-sm mt-1">{formErrors[fieldName]}</p>
                                    ) : null;
                                };
                                
                                return (
                                    <>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Product Name</label>
                                                <input
                                                    type="text"
                                                    value={formData.productName}
                                                    onChange={(e) => setFormData({ ...formData, productName: e.target.value })}
                                                    className={`input-field ${formErrors.productName ? 'border-red-500' : ''}`}
                                                    required
                                                />
                                                {renderFieldError('productName')}
                                            </div>
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">SKU</label>
                                                <input
                                                    type="text"
                                                    value={formData.sku}
                                                    onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                                                    className={`input-field ${formErrors.sku ? 'border-red-500' : ''}`}
                                                    placeholder="LAPTOP-001"
                                                    required
                                                />
                                                {renderFieldError('sku')}
                                            </div>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
                                            <textarea
                                                value={formData.description}
                                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                                className={`input-field ${formErrors.description ? 'border-red-500' : ''}`}
                                                rows="3"
                                                required
                                            />
                                            {renderFieldError('description')}
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Selling Price ($)</label>
                                                <input
                                                    type="number"
                                                    step="0.01"
                                                    value={formData.price}
                                                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                                    className={`input-field ${formErrors.price ? 'border-red-500' : ''}`}
                                                    required
                                                />
                                                {renderFieldError('price')}
                                            </div>
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Cost Price ($)</label>
                                                <input
                                                    type="number"
                                                    step="0.01"
                                                    value={formData.costPrice}
                                                    onChange={(e) => setFormData({ ...formData, costPrice: e.target.value })}
                                                    className={`input-field ${formErrors.costPrice ? 'border-red-500' : ''}`}
                                                />
                                                {renderFieldError('costPrice')}
                                            </div>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
                                                <select
                                                    value={formData.categoryId}
                                                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                                                    className={`input-field ${formErrors.categoryId ? 'border-red-500' : ''}`}
                                                    required
                                                >
                                                    <option value="">Select a category</option>
                                                    {categories.map((cat) => (
                                                        <option key={cat.categoryId} value={cat.categoryId}>
                                                            {cat.categoryName || cat.name}
                                                        </option>
                                                    ))}
                                                </select>
                                                {renderFieldError('categoryId')}
                                            </div>
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Brand</label>
                                                <input
                                                    type="text"
                                                    value={formData.brand}
                                                    onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                                                    className={`input-field ${formErrors.brand ? 'border-red-500' : ''}`}
                                                />
                                                {renderFieldError('brand')}
                                            </div>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Initial Stock</label>
                                                <input
                                                    type="number"
                                                    value={formData.initialStock}
                                                    onChange={(e) => setFormData({ ...formData, initialStock: e.target.value })}
                                                    className={`input-field ${formErrors.initialStock ? 'border-red-500' : ''}`}
                                                    disabled={editingProduct}
                                                    placeholder={editingProduct ? "Manage via Inventory" : "0"}
                                                />
                                                {renderFieldError('initialStock')}
                                            </div>
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-2">Image URL</label>
                                                <input
                                                    type="text"
                                                    value={formData.imageUrl}
                                                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                                                    className={`input-field ${formErrors.imageUrl ? 'border-red-500' : ''}`}
                                                />
                                                {renderFieldError('imageUrl')}
                                            </div>
                                        </div>
                                    </>
                                );
                            })()}

                            <div className="flex gap-3 pt-4">
                                <button type="submit" className="btn-primary flex-1">
                                    {editingProduct ? 'Update Product' : 'Create Product'}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowModal(false);
                                        resetForm();
                                    }}
                                    className="btn-secondary flex-1"
                                >
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProductManagement;
