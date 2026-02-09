import api from './api';

// POST /api/v1/orders - Create order
export const createOrder = (orderData) => api.post('/orders', orderData);

// GET /api/v1/orders/{id} - Get order by ID
export const getOrderById = (id) => api.get(`/orders/${id}`);

// GET /api/v1/orders/user/{userId} - Get orders by user
export const getOrdersByUser = (userId) => api.get(`/orders/user/${userId}`);

// GET /api/v1/orders/status/{status} - Get orders by status
export const getOrdersByStatus = (status) => api.get(`/orders/status/${status}`);

// GET /api/v1/orders/date-range - Get orders by date range
export const getOrdersByDateRange = (startDate, endDate) =>
    api.get('/orders/date-range', { params: { startDate, endDate } });

// PATCH /api/v1/orders/{id}/status?status={status} - Update order status
export const updateOrderStatus = (id, status) =>
    api.patch(`/orders/${id}/status`, null, { params: { status } });

// PATCH /api/v1/orders/{id}/confirm - Confirm order
export const confirmOrder = (id) => api.patch(`/orders/${id}/confirm`);

// PATCH /api/v1/orders/{id}/ship - Ship order
export const shipOrder = (id) => api.patch(`/orders/${id}/ship`);

// PATCH /api/v1/orders/{id}/complete - Complete order
export const completeOrder = (id) => api.patch(`/orders/${id}/complete`);

// PATCH /api/v1/orders/{id}/cancel - Cancel order
export const cancelOrder = (id) => api.patch(`/orders/${id}/cancel`);

// GET /api/v1/orders/pending - Get pending orders
export const getPendingOrders = () => api.get('/orders/pending');

// GET /api/v1/orders - Get all orders
export const getAllOrders = () => api.get('/orders');

// PUT /api/v1/orders/{id} - Update order (PENDING only)
export const updateOrder = (id, orderData) => api.put(`/orders/${id}`, orderData);

// DELETE /api/v1/orders/{id} - Delete order (PENDING only)
export const deleteOrder = (id) => api.delete(`/orders/${id}`);
