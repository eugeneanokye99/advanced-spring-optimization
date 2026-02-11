import { useState, useEffect } from 'react';
import { getCartItems, updateCartItemQuantity, removeFromCart, getCartTotal, clearCart } from '../../services/cartService';
import { createOrder } from '../../services/orderService';
import { useAuth } from '../../context/AuthContext';
import { Trash2, Plus, Minus, ShoppingBag, X, CreditCard, MapPin } from 'lucide-react';
import { showErrorAlert, showSuccessToast, showWarningToast, isInsufficientStockError } from '../../utils/errorHandler';

const Cart = () => {
    const [cartItems, setCartItems] = useState([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [showCheckoutModal, setShowCheckoutModal] = useState(false);
    const [checkoutForm, setCheckoutForm] = useState({
        shippingAddress: '',
        paymentMethod: 'CASH', // Default to CASH
        notes: ''
    });
    const [isProcessing, setIsProcessing] = useState(false);
    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            loadCart();
        }
    }, [user]);

    const loadCart = async () => {
        if (!user) return;
        try {
            const [itemsResponse, totalResponse] = await Promise.all([
                getCartItems(user.userId),
                getCartTotal(user.userId),
            ]);
            setCartItems(itemsResponse.data || []);
            setTotal(totalResponse.data || 0);
        } catch (error) {
            console.error('Error loading cart:', error);            
            if (isInsufficientStockError(error)) {
                showErrorAlert(error, 'Not enough stock available for this item');
            } else {
                showErrorAlert(error, 'Failed to update cart item');
            }            showErrorAlert(error, 'Failed to load cart items');
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateQuantity = async (cartItemId, newQuantity) => {
        if (newQuantity < 1) return;
        try {
            await updateCartItemQuantity(cartItemId, newQuantity);
            loadCart();
        } catch (error) {
            showErrorAlert(error, 'Failed to update quantity');
        }
    };

    const handleRemove = async (cartItemId) => {
        try {
            await removeFromCart(cartItemId);
            showSuccessToast('Item removed from cart');
            loadCart();
        } catch (error) {
            showErrorAlert(error, 'Failed to remove item');
        }
    };

    const handleCheckout = async () => {
        if (cartItems.length === 0) {
            showWarningToast('Your cart is empty');
            return;
        }

        try {
            const orderItems = cartItems.map((item) => ({
                productId: item.productId,
                quantity: item.quantity,
                price: item.productPrice,
            }));

            await createOrder({
                userId: user.userId,
                orderItems,
                shippingAddress: "123 Main St, New York, NY 10001", // Placeholder
                totalAmount: total
            });

            await clearCart(user.userId);
            showSuccessToast('Order placed successfully!');
            loadCart();
        } catch (error) {
            showErrorAlert(error, 'Failed to place order');
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center py-12">
                <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-6">Shopping Cart</h1>

            {cartItems.length === 0 ? (
                <div className="card p-12 text-center">
                    <ShoppingBag className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">Your cart is empty</h3>
                    <p className="text-gray-600">Start shopping to add items to your cart</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Cart Items */}
                    <div className="lg:col-span-2 space-y-4">
                        {cartItems.map((item) => (
                            <div key={item.cartItemId} className="card p-4">
                                <div className="flex items-center gap-4">
                                    <div className="w-20 h-20 bg-gray-200 rounded-lg flex-shrink-0"></div>
                                    <div className="flex-1">
                                        <h3 className="font-semibold text-gray-900">{item.productName}</h3>
                                        <p className="text-sm text-gray-600">${item.productPrice.toFixed(2)} each</p>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity - 1)}
                                            className="p-2 hover:bg-gray-100 rounded-lg"
                                        >
                                            <Minus className="w-4 h-4" />
                                        </button>
                                        <span className="w-12 text-center font-medium">{item.quantity}</span>
                                        <button
                                            onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity + 1)}
                                            className="p-2 hover:bg-gray-100 rounded-lg"
                                        >
                                            <Plus className="w-4 h-4" />
                                        </button>
                                    </div>
                                    <div className="text-right">
                                        <p className="font-bold text-gray-900">${(item.productPrice * item.quantity).toFixed(2)}</p>
                                        <button
                                            onClick={() => handleRemove(item.cartItemId)}
                                            className="text-red-600 hover:text-red-800 mt-2"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Order Summary */}
                    <div className="lg:col-span-1">
                        <div className="card p-6 sticky top-6">
                            <h2 className="text-xl font-bold text-gray-900 mb-4">Order Summary</h2>
                            <div className="space-y-3 mb-6">
                                <div className="flex justify-between text-gray-600">
                                    <span>Subtotal</span>
                                    <span>${total.toFixed(2)}</span>
                                </div>
                                <div className="flex justify-between text-gray-600">
                                    <span>Shipping</span>
                                    <span>Free</span>
                                </div>
                                <div className="border-t pt-3 flex justify-between font-bold text-lg">
                                    <span>Total</span>
                                    <span className="text-primary-600">${total.toFixed(2)}</span>
                                </div>
                            </div>
                            <button onClick={handleCheckout} className="w-full btn-primary py-3 text-lg">
                                Proceed to Checkout
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Cart;
