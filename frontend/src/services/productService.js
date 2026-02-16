import api from './api';

// POST /api/v1/products - Create product
export const createProduct = (productData) => api.post('/products', productData);

// GET /api/v1/products/{id} - Get product by ID
export const getProductById = (id) => api.get(`/products/${id}`);

// GET /api/v1/products - Get all products
export const getAllProducts = () => api.get('/products');

// GET /api/v1/products/active - Get active products
export const getActiveProducts = () => api.get('/products/active');

// GET /api/v1/products/category/{categoryId} - Get products by category
export const getProductsByCategory = (categoryId) => api.get(`/products/category/${categoryId}`);

// GET /api/v1/products/search?name={name} - Search products by name
export const searchProductsByName = (name) => api.get('/products/search', { params: { name } });

// GET /api/v1/products/price-range?minPrice={min}&maxPrice={max} - Get products by price range
export const getProductsByPriceRange = (minPrice, maxPrice) =>
    api.get('/products/price-range', { params: { minPrice, maxPrice } });

// PUT /api/v1/products/{id} - Update product
export const updateProduct = (id, productData) => api.put(`/products/${id}`, productData);

// PATCH /api/v1/products/{id}/price?newPrice={price} - Update product price
export const updateProductPrice = (id, newPrice) =>
    api.patch(`/products/${id}/price`, null, { params: { newPrice } });

// PATCH /api/v1/products/{id}/activate - Activate product
export const activateProduct = (id) => api.patch(`/products/${id}/activate`);

// PATCH /api/v1/products/{id}/deactivate - Deactivate product
export const deactivateProduct = (id) => api.patch(`/products/${id}/deactivate`);

// DELETE /api/v1/products/{id} - Delete product
export const deleteProduct = (id) => api.delete(`/products/${id}`);

// GET /api/v1/products/count - Get total product count
export const getTotalProductCount = () => api.get('/products/count');

// GET /api/v1/products/count/category/{categoryId} - Get product count by category
export const getProductCountByCategory = (categoryId) => api.get(`/products/count/category/${categoryId}`);

// GET /api/v1/products/paginated - Get products with pagination
export const getProductsPaginated = (page = 0, size = 10, sortBy = 'id', sortDirection = 'ASC') =>
    api.get('/products/paginated', { params: { page, size, sortBy, sortDirection } });

// GET /api/v1/products/search/paginated - Search products with pagination
export const searchProductsPaginated = (term, page = 0, size = 10) =>
    api.get('/products/search/paginated', { params: { term, page, size } });

// GET /api/v1/products/filter - Get products with filters
export const getProductsWithFilters = (filters) =>
    api.get('/products/filter', { params: filters });

// GET /api/v1/products/new-arrivals - Get recently added products
export const getNewArrivals = (limit = 10) =>
    api.get('/products/new-arrivals', { params: { limit } });
