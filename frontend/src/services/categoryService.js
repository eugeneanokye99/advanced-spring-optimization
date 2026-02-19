import api from './api';

// POST /api/v1/categories - Create category
export const createCategory = (categoryData) => api.post('/categories', categoryData);

// GET /api/v1/categories/{id} - Get category by ID
export const getCategoryById = (id) => api.get(`/categories/${id}`);

// GET /api/v1/categories - Get all categories
export const getAllCategories = () => api.get('/categories');

// PUT /api/v1/categories/{id} - Update category
export const updateCategory = (id, categoryData) => api.put(`/categories/${id}`, categoryData);

// DELETE /api/v1/categories/{id} - Delete category
export const deleteCategory = (id) => api.delete(`/categories/${id}`);
