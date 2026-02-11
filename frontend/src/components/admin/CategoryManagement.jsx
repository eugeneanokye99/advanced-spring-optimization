import { useState, useEffect } from 'react';
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '../../services/categoryService';
import { Plus, Edit, Trash2 } from 'lucide-react';
import { showErrorAlert, showSuccessToast } from '../../utils/errorHandler';

const CategoryManagement = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [formData, setFormData] = useState({ categoryName: '', description: '' });

    useEffect(() => {
        loadCategories();
    }, []);

    const loadCategories = async () => {
        try {
            const response = await getAllCategories();
            setCategories(response.data || []);
        } catch (error) {
            console.error('Error loading categories:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingCategory) {
                await updateCategory(editingCategory.categoryId, formData);
                showSuccessToast('Category updated successfully');
            } else {
                await createCategory(formData);
                showSuccessToast('Category created successfully');
            }
            setShowModal(false);
            setFormData({ categoryName: '', description: '' });
            setEditingCategory(null);
            loadCategories();
        } catch (error) {
            showErrorAlert(error, editingCategory ? 'Failed to update category' : 'Failed to create category');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this category?')) {
            try {
                await deleteCategory(id);
                showSuccessToast('Category deleted successfully');
                loadCategories();
            } catch (error) {
                showErrorAlert(error, 'Failed to delete category');
            }
        }
    };

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-gray-900">Category Management</h1>
                <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
                    <Plus className="w-5 h-5" /> Add Category
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {loading ? (
                    <div className="col-span-full flex justify-center py-12">
                        <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : categories.length === 0 ? (
                    <div className="col-span-full text-center py-12 text-gray-500">No categories found</div>
                ) : (
                    categories.map((category) => (
                        <div key={category.categoryId} className="card p-6">
                            <h3 className="text-lg font-semibold text-gray-900 mb-2">{category.categoryName}</h3>
                            <p className="text-sm text-gray-600 mb-4">{category.description}</p>
                            <div className="flex gap-2">
                                <button onClick={() => { setEditingCategory(category); setFormData({ categoryName: category.categoryName, description: category.description }); setShowModal(true); }} className="text-blue-600 hover:text-blue-900">
                                    <Edit className="w-4 h-4" />
                                </button>
                                <button onClick={() => handleDelete(category.categoryId)} className="text-red-600 hover:text-red-900">
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {showModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl max-w-md w-full">
                        <div className="p-6 border-b">
                            <h2 className="text-2xl font-bold">{editingCategory ? 'Edit Category' : 'Add Category'}</h2>
                        </div>
                        <form onSubmit={handleSubmit} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Name</label>
                                <input type="text" value={formData.categoryName} onChange={(e) => setFormData({ ...formData, categoryName: e.target.value })} className="input-field" required />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
                                <textarea value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} className="input-field" rows="3" required />
                            </div>
                            <div className="flex gap-3">
                                <button type="submit" className="btn-primary flex-1">{editingCategory ? 'Update' : 'Create'}</button>
                                <button type="button" onClick={() => { setShowModal(false); setFormData({ categoryName: '', description: '' }); setEditingCategory(null); }} className="btn-secondary flex-1">Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

        </div>
    );
};

export default CategoryManagement;
