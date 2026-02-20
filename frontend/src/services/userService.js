import api from './api';

export const registerUser = (userData) => api.post('/auth/register', userData);

export const getUserById = (id) => api.get(`/users/${id}`);

export const getAllUsers = () => api.get('/users');

export const updateUserProfile = (id, userData) => api.put(`/users/${id}`, userData);

export const deleteUser = (id) => api.delete(`/users/${id}`);

export const getUserByEmail = (email) => api.get(`/users/email/${email}`);

export const authenticateUser = (credentials) => api.post('/auth/login', credentials);

export const changePassword = (id, passwordData) => api.put(`/users/${id}/password`, passwordData);
