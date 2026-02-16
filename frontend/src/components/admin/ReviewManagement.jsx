import { useState, useEffect } from 'react';
import { Trash2, MessageSquare, Star, User, Package, Calendar } from 'lucide-react';
import { getAllReviews, deleteReview } from '../../services/reviewService';
import { showErrorAlert } from '../../utils/errorHandler';

const ReviewManagement = () => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        loadReviews();
    }, []);

    const loadReviews = async () => {
        try {
            setLoading(true);
            const response = await getAllReviews();
            setReviews(response.data || []);
        } catch (error) {
            console.error('Error loading reviews:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this review?')) {
            try {
                await deleteReview(id);
                loadReviews();
            } catch (error) {
                showErrorAlert(error, 'Failed to delete review');
            }
        }
    };

    const filteredReviews = reviews.filter(review =>
        (review.productName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (review.userName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (review.comment || '').toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Review Management</h1>
                    <p className="text-gray-600 mt-1">Monitor and moderate customer feedback</p>
                </div>
            </div>

            <div className="card p-4 mb-6">
                <div className="relative">
                    <MessageSquare className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                        type="text"
                        placeholder="Search by product, user, or comment..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="input-field pl-10"
                    />
                </div>
            </div>

            {loading ? (
                <div className="flex justify-center py-12">
                    <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
            ) : (
                <div className="grid grid-cols-1 gap-6">
                    {filteredReviews.length === 0 ? (
                        <div className="card p-12 text-center text-gray-500">
                            No reviews found
                        </div>
                    ) : (
                        filteredReviews.map((review) => (
                            <div key={review.id} className="card p-6 hover:shadow-md transition-shadow">
                                <div className="flex justify-between items-start">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-4 mb-3">
                                            <div className="flex">
                                                {[...Array(5)].map((_, i) => (
                                                    <Star
                                                        key={i}
                                                        className={`w-4 h-4 ${i < review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
                                                    />
                                                ))}
                                            </div>
                                            <span className="text-sm font-semibold text-gray-900">{review.title}</span>
                                        </div>

                                        <p className="text-gray-700 mb-4 italic">"{review.comment}"</p>

                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-500">
                                            <div className="flex items-center gap-2">
                                                <User className="w-4 h-4" />
                                                <span>{review.userName || `User ID: ${review.userId}`}</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Package className="w-4 h-4" />
                                                <span>{review.productName || `Product ID: ${review.productId}`}</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <Calendar className="w-4 h-4" />
                                                <span>{new Date(review.createdAt).toLocaleDateString()}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <button
                                        onClick={() => handleDelete(review.id)}
                                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                        title="Delete Review"
                                    >
                                        <Trash2 className="w-5 h-5" />
                                    </button>
                                </div>
                                {review.helpfulCount > 0 && (
                                    <div className="mt-4 pt-4 border-t border-gray-50">
                                        <span className="text-xs bg-blue-50 text-blue-700 px-2 py-1 rounded-full">
                                            {review.helpfulCount} people found this helpful
                                        </span>
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
};

export default ReviewManagement;
