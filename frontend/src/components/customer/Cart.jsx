import { useState, useEffect } from 'react';
import { getCartItems, updateCartItemQuantity, removeFromCart, getCartTotal, clearCart } from '../../services/cartService';
import { createOrder } from '../../services/orderService';
import { getAddressesByUser } from '../../services/addressService';
import { useAuth } from '../../context/AuthContext';
import { Trash2, Plus, Minus, ShoppingBag, CreditCard, MapPin, AlertCircle } from 'lucide-react';
import { showErrorAlert, showSuccessToast, showWarningToast } from '../../utils/errorHandler';

const Cart = () => {
    const [cartItems, setCartItems] = useState([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [addresses, setAddresses] = useState([]);
    const [checkoutForm, setCheckoutForm] = useState({
        shippingAddress: '',
        paymentMethod: 'CASH',
        notes: ''
    });
    const [isProcessing, setIsProcessing] = useState(false);
    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            loadAll();
        }
    }, [user]);

    const loadAll = async () => {
        if (!user) return;
        try {
            const [itemsResponse, totalResponse, addressResponse] = await Promise.all([
                getCartItems(user.id),
                getCartTotal(user.id),
                getAddressesByUser(user.id).catch(() => ({ data: [] })),
            ]);

            const items = itemsResponse.data || [];
            setCartItems(items);
            setTotal(totalResponse.data || 0);

            const userAddresses = addressResponse.data || [];
            setAddresses(userAddresses);

            // Pre-select the default address, or first available
            const defaultAddr = userAddresses.find(a => a.isDefault) || userAddresses[0];
            if (defaultAddr) {
                const formatted = formatAddress(defaultAddr);
                setCheckoutForm(f => ({ ...f, shippingAddress: formatted }));
            }
        } catch (error) {
            console.error('Error loading cart:', error);
            showErrorAlert(error, 'Failed to load cart');
        } finally {
            setLoading(false);
        }
    };

    const formatAddress = (addr) =>
        [addr.streetAddress, addr.city, addr.state, addr.postalCode, addr.country]
            .filter(Boolean)
            .join(', ');

    const loadCart = async () => {
        if (!user) return;
        try {
            const [itemsResponse, totalResponse] = await Promise.all([
                getCartItems(user.id),
                getCartTotal(user.id),
            ]);
            setCartItems(itemsResponse.data || []);
            setTotal(totalResponse.data || 0);
        } catch (error) {
            showErrorAlert(error, 'Failed to reload cart');
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

    const handleCheckout = async (e) => {
        e.preventDefault();
        if (cartItems.length === 0) {
            showWarningToast('Your cart is empty');
            return;
        }
        if (!checkoutForm.shippingAddress.trim()) {
            showWarningToast('Please enter a shipping address');
            return;
        }

        setIsProcessing(true);
        try {
            const orderItems = cartItems.map((item) => ({
                productId: item.productId,
                quantity: item.quantity,
                price: item.productPrice,
            }));

            const result = await createOrder({
                userId: user.id,
                orderItems,
                shippingAddress: checkoutForm.shippingAddress,
                paymentMethod: checkoutForm.paymentMethod,
                notes: checkoutForm.notes,
                totalAmount: total
            });

            // api.js returns the ApiResponse object — check success flag
            if (result && result.success === false) {
                throw new Error(result.message || 'Order creation failed');
            }

            await clearCart(user.id);
            setCheckoutForm(f => ({ ...f, notes: '' }));
            showSuccessToast('Order placed successfully! 🎉');
            loadCart();
        } catch (error) {
            const msg = error.message || '';
            if (msg.includes('no longer available')) {
                showWarningToast(msg);
                await loadCart();
            } else {
                showErrorAlert(error, 'Failed to place order');
            }
        } finally {
            setIsProcessing(false);
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
                            <div key={item.id} className="card p-4">
                                <div className="flex items-center gap-4">
                                    <div className="w-20 h-20 bg-gray-200 rounded-lg shrink-0"></div>
                                    <div className="flex-1">
                                        <h3 className="font-semibold text-gray-900">{item.productName}</h3>
                                        <p className="text-sm text-gray-600">${item.productPrice.toFixed(2)} each</p>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <button onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)} className="p-2 hover:bg-gray-100 rounded-lg">
                                            <Minus className="w-4 h-4" />
                                        </button>
                                        <span className="w-12 text-center font-medium">{item.quantity}</span>
                                        <button onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)} className="p-2 hover:bg-gray-100 rounded-lg">
                                            <Plus className="w-4 h-4" />
                                        </button>
                                    </div>
                                    <div className="text-right">
                                        <p className="font-bold text-gray-900">${(item.productPrice * item.quantity).toFixed(2)}</p>
                                        <button onClick={() => handleRemove(item.id)} className="text-red-600 hover:text-red-800 mt-2">
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Order Summary + Checkout Form */}
                    <div className="lg:col-span-1">
                        <form onSubmit={handleCheckout} className="card p-6 sticky top-6 space-y-4">
                            <h2 className="text-xl font-bold text-gray-900">Order Summary</h2>

                            {/* Totals */}
                            <div className="space-y-2">
                                <div className="flex justify-between text-gray-600">
                                    <span>Subtotal</span>
                                    <span>${total.toFixed(2)}</span>
                                </div>
                                <div className="flex justify-between text-gray-600">
                                    <span>Shipping</span>
                                    <span>Free</span>
                                </div>
                                <div className="border-t pt-2 flex justify-between font-bold text-lg">
                                    <span>Total</span>
                                    <span className="text-primary-600">${total.toFixed(2)}</span>
                                </div>
                            </div>

                            {/* Shipping Address */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    <MapPin className="w-4 h-4 inline mr-1" />Shipping Address *
                                </label>
                                {/* Saved address selector */}
                                {addresses.length > 0 && (
                                    <select
                                        className="w-full border border-gray-300 rounded-lg px-3 py-2 mb-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                                        onChange={(e) => setCheckoutForm(f => ({ ...f, shippingAddress: e.target.value }))}
                                        value={checkoutForm.shippingAddress}
                                    >
                                        {addresses.map(addr => {
                                            const formatted = formatAddress(addr);
                                            return (
                                                <option key={addr.id} value={formatted}>
                                                    {addr.isDefault ? '⭐ ' : ''}{addr.addressType} — {formatted}
                                                </option>
                                            );
                                        })}
                                        <option value="">Enter a different address…</option>
                                    </select>
                                )}
                                {(!checkoutForm.shippingAddress || addresses.length === 0) && (
                                    <textarea
                                        required
                                        rows={2}
                                        value={checkoutForm.shippingAddress}
                                        onChange={(e) => setCheckoutForm(f => ({ ...f, shippingAddress: e.target.value }))}
                                        placeholder="Enter your full shipping address"
                                        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                                    />
                                )}
                                {addresses.length === 0 && (
                                    <p className="text-xs text-gray-500 mt-1 flex items-center gap-1">
                                        <AlertCircle className="w-3 h-3" /> Save addresses in My Addresses for faster checkout
                                    </p>
                                )}
                            </div>

                            {/* Payment Method */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    <CreditCard className="w-4 h-4 inline mr-1" />Payment Method
                                </label>
                                <select
                                    value={checkoutForm.paymentMethod}
                                    onChange={(e) => setCheckoutForm(f => ({ ...f, paymentMethod: e.target.value }))}
                                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                                >
                                    <option value="CASH">Cash on Delivery</option>
                                    <option value="CARD">Credit / Debit Card</option>
                                    <option value="MOBILE_MONEY">Mobile Money</option>
                                </select>
                            </div>

                            {/* Notes */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Order Notes (optional)</label>
                                <input
                                    type="text"
                                    value={checkoutForm.notes}
                                    onChange={(e) => setCheckoutForm(f => ({ ...f, notes: e.target.value }))}
                                    placeholder="Special instructions?"
                                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={isProcessing}
                                className="w-full btn-primary py-3 text-lg disabled:opacity-50"
                            >
                                {isProcessing ? 'Placing Order...' : 'Place Order'}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Cart;
