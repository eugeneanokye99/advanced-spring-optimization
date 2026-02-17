import { useState, useEffect } from 'react';
import { X, Star, ThumbsUp, Send, MessageSquare } from 'lucide-react';
import { getReviewsByProduct, createReview, markReviewAsHelpful } from '../../services/reviewService';
import { useAuth } from '../../context/AuthContext';
import { showErrorAlert } from '../../utils/errorHandler';

const ProductDetailModal = ({ product, onClose }) => {
    const { user } = useAuth();
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newReview, setNewReview] = useState({
        rating: 5,
        title: '',
        comment: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (product) {
            loadReviews();
        }
    }, [product]);

    const loadReviews = async () => {
        try {
            setLoading(true);
            const response = await getReviewsByProduct(product.id);
            setReviews(response.data || []);
        } catch (error) {
            console.error('Error loading reviews:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitReview = async (e) => {
        e.preventDefault();
        if (!user) {
            showErrorAlert({ message: 'Please login to leave a review' }, 'Authentication Required');
            return;
        }

        try {
            setSubmitting(true);
            await createReview({
                ...newReview,
                userId: user.id,
                productId: product.id
            });
            setNewReview({ rating: 5, title: '', comment: '' });
            loadReviews();
        } catch (error) {
            showErrorAlert(error, 'Error submitting review');
        } finally {
            setSubmitting(false);
        }
    };

    const handleHelpful = async (reviewId) => {
        try {
            await markReviewAsHelpful(reviewId);
            loadReviews();
        } catch (error) {
            console.error('Error marking helpful:', error);
        }
    };

    if (!product) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col md:flex-row relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 bg-white/80 rounded-full shadow-lg hover:bg-white z-10 transition-colors"
                >
                    <X className="w-6 h-6 text-gray-900" />
                </button>

                {/* Left: Product Info */}
                <div className="md:w-1/2 overflow-y-auto border-r border-gray-100 p-8">
                    <div className="aspect-square bg-gray-50 rounded-2xl mb-6 overflow-hidden">
                        {product.imageUrl ? (
                            <img src={product.imageUrl} alt={product.productName} className="w-full h-full object-cover" />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center text-gray-300">No Image</div>
                        )}
                    </div>
                    <h2 className="text-3xl font-black text-gray-900 mb-2">{product.productName}</h2>
                    <p className="text-primary-600 font-bold text-2xl mb-4">${product.price.toFixed(2)}</p>
                    <div className="prose prose-sm text-gray-600 mb-6">
                        {product.description}
                    </div>
                    <div className="grid grid-cols-2 gap-4 text-xs font-medium uppercase tracking-wider text-gray-500">
                        <div className="bg-gray-50 p-3 rounded-xl">
                            <span className="block text-gray-400 mb-1">Category</span>
                            <span className="text-gray-900 font-bold">{product.categoryName}</span>
                        </div>
                        <div className="bg-gray-50 p-3 rounded-xl">
                            <span className="block text-gray-400 mb-1">Brand</span>
                            <span className="text-gray-900 font-bold">{product.brand || 'N/A'}</span>
                        </div>
                    </div>
                </div>

                {/* Right: Reviews */}
                <div className="md:w-1/2 flex flex-col h-full overflow-hidden bg-gray-50/50">
                    <div className="p-8 border-b border-gray-100 bg-white">
                        <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                            Customer Reviews
                            <span className="text-sm font-normal text-gray-400">({reviews.length})</span>
                        </h3>
                    </div>

                    <div className="flex-1 overflow-y-auto p-8 space-y-6">
                        {/* New Review Form */}
                        {user ? (
                            <form onSubmit={handleSubmitReview} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 mb-8">
                                <h4 className="font-bold text-gray-900 mb-4 text-sm">Leave a Review</h4>
                                <div className="flex gap-2 mb-4">
                                    {[1, 2, 3, 4, 5].map((star) => (
                                        <button
                                            key={star}
                                            type="button"
                                            onClick={() => setNewReview({ ...newReview, rating: star })}
                                            className={`p-1 transition-colors ${star <= newReview.rating ? 'text-yellow-400' : 'text-gray-300'}`}
                                        >
                                            <Star className={`w-6 h-6 ${star <= newReview.rating ? 'fill-current' : ''}`} />
                                        </button>
                                    ))}
                                </div>
                                <input
                                    type="text"
                                    placeholder="Review Title"
                                    required
                                    value={newReview.title}
                                    onChange={(e) => setNewReview({ ...newReview, title: e.target.value })}
                                    className="w-full mb-3 px-4 py-2 bg-gray-50 border-none rounded-xl text-sm focus:ring-2 focus:ring-primary-500"
                                />
                                <textarea
                                    placeholder="Tell us what you think..."
                                    required
                                    rows="3"
                                    value={newReview.comment}
                                    onChange={(e) => setNewReview({ ...newReview, comment: e.target.value })}
                                    className="w-full mb-4 px-4 py-2 bg-gray-50 border-none rounded-xl text-sm focus:ring-2 focus:ring-primary-500 resize-none"
                                ></textarea>
                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="w-full py-3 bg-gray-900 text-white rounded-xl font-bold flex items-center justify-center gap-2 hover:bg-primary-600 hover:text-black transition-all disabled:opacity-50"
                                >
                                    <Send className="w-4 h-4" />
                                    {submitting ? 'Submitting...' : 'Post Review'}
                                </button>
                            </form>
                        ) : (
                            <div className="bg-primary-50 p-4 rounded-xl text-primary-700 text-sm font-medium mb-8 text-center">
                                Please login to join the conversation and leave a review.
                            </div>
                        )}

                        {loading ? (
                            <div className="flex justify-center py-4">
                                <div className="w-8 h-8 border-3 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
                            </div>
                        ) : reviews.length === 0 ? (
                            <div className="text-center py-8">
                                <MessageSquare className="w-12 h-12 text-gray-200 mx-auto mb-3" />
                                <p className="text-gray-400 text-sm">No reviews yet. Be the first to review!</p>
                            </div>
                        ) : (
                            reviews.map((review) => (
                                <div key={review.id} className="space-y-2">
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <div className="flex gap-1 mb-1">
                                                {[...Array(5)].map((_, i) => (
                                                    <Star
                                                        key={i}
                                                        className={`w-3 h-3 ${i < review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
                                                    />
                                                ))}
                                            </div>
                                            <h5 className="font-bold text-gray-900 text-sm">{review.title}</h5>
                                        </div>
                                        <span className="text-[10px] text-gray-400 font-medium uppercase tracking-wider">
                                            {new Date(review.createdAt).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <p className="text-sm text-gray-600 italic">"{review.comment}"</p>
                                    <div className="flex items-center justify-between pt-2">
                                        <span className="text-xs font-bold text-gray-900">{review.userName || 'Anonymous'}</span>
                                        <button
                                            onClick={() => handleHelpful(review.id)}
                                            className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-primary-600 transition-colors"
                                        >
                                            <ThumbsUp className="w-3 h-3" />
                                            Helpful ({review.helpfulCount})
                                        </button>
                                    </div>
                                    <div className="h-px bg-gray-100 mt-4"></div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailModal;
