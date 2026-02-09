import { useState, useEffect } from 'react';
import { getOrdersByUser, cancelOrder, deleteOrder, updateOrder } from '../../services/orderService';
import { useAuth } from '../../context/AuthContext';
import { Package, Clock, Truck, CheckCircle, XCircle, Trash2, X, Edit, Plus, Minus } from 'lucide-react';

const OrderHistory = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingOrder, setEditingOrder] = useState(null);
    const [editForm, setEditForm] = useState({});
    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            loadOrders();
        }
    }, [user]);

    const loadOrders = async () => {
        if (!user) return;
        try {
            const response = await getOrdersByUser(user.userId);
            setOrders(response.data || []);
            console.log('Orders loaded:', response.data);
        } catch (error) {
            console.error('Error loading orders:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCancelOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to cancel this order?')) return;
        try {
            await cancelOrder(orderId);
            loadOrders();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to cancel order');
        }
    };

    const handleDeleteOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to delete this order?')) return;
        try {
            await deleteOrder(orderId);
            loadOrders();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to delete order');
        }
    };

    const handleEditOrder = (order) => {
        console.log('Editing order:', order);
        setEditingOrder(order.orderId);
        setEditForm({
            shippingAddress: order.shippingAddress || '',
            paymentMethod: order.paymentMethod || '',
            notes: order.notes || '',
            orderItems: (order.orderItems || []).map(item => {
                const unitPrice = item.price || (item.subtotal / item.quantity) || 0;
                return {
                    orderItemId: item.orderItemId,
                    productId: item.productId,
                    productName: item.productName || 'Unknown Product',
                    quantity: item.quantity || 1,
                    price: unitPrice
                };
            })
        });
    };

const handleUpdateOrder = async (orderId) => {
    try {
        const updateData = {
            shippingAddress: editForm.shippingAddress,
            paymentMethod: editForm.paymentMethod,
            notes: editForm.notes,
            orderItems: editForm.orderItems.map(item => ({
                orderItemId: item.orderItemId,
                productId: item.productId,
                quantity: item.quantity,
                price: item.price
            }))
        };

        console.log('Updating order with data:', updateData);
        await updateOrder(orderId, updateData);
        setEditingOrder(null);
        loadOrders();
    } catch (error) {
        console.error('Update error:', error);
        alert(error.response?.data?.message || 'Failed to update order');
    }
};


    const updateItemQuantity = (index, newQuantity) => {
        if (newQuantity < 1) return;
        const updatedItems = [...editForm.orderItems];
        updatedItems[index].quantity = newQuantity;
        setEditForm({ ...editForm, orderItems: updatedItems });
    };

    const removeItem = (index) => {
        if (editForm.orderItems.length === 1) {
            alert('Order must have at least one item');
            return;
        }
        const updatedItems = editForm.orderItems.filter((_, i) => i !== index);
        setEditForm({ ...editForm, orderItems: updatedItems });
    };

    const calculateTotal = () => {
        if (!editForm.orderItems) return 0;
        return editForm.orderItems.reduce((sum, item) => {
            const price = parseFloat(item.price) || 0;
            const quantity = parseInt(item.quantity) || 0;
            return sum + (price * quantity);
        }, 0);
    };

    const getStatusBadge = (status) => {
        const badges = {
            PENDING: { color: 'bg-yellow-100 text-yellow-800', icon: Clock },
            PROCESSING: { color: 'bg-blue-100 text-blue-800', icon: CheckCircle },
            SHIPPED: { color: 'bg-purple-100 text-purple-800', icon: Truck },
            DELIVERED: { color: 'bg-green-100 text-green-800', icon: Package },
            CANCELLED: { color: 'bg-red-100 text-red-800', icon: XCircle },
        };
        const badge = badges[status] || badges.PENDING;
        const Icon = badge.icon;
        return (
            <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${badge.color}`}>
                <Icon className="w-3 h-3" /> {status}
            </span>
        );
    };

    const getPaymentStatusBadge = (status) => {
        const badges = {
            PAID: 'bg-green-100 text-green-800',
            UNPAID: 'bg-red-100 text-red-800',
            PENDING: 'bg-yellow-100 text-yellow-800',
        };
        return (
            <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${badges[status] || badges.UNPAID}`}>
                {status}
            </span>
        );
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
            <h1 className="text-3xl font-bold text-gray-900 mb-6">Order History</h1>

            {orders.length === 0 ? (
                <div className="card p-12 text-center">
                    <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">No orders yet</h3>
                    <p className="text-gray-600">Your order history will appear here</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {orders.map((order) => (
                        <div key={order.orderId} className="card p-6">
                            {editingOrder === order.orderId ? (
                                <div className="space-y-4">
                                    <h3 className="text-lg font-semibold text-gray-900">Edit Order #{order.orderId}</h3>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                                Shipping Address <span className="text-red-500">*</span>
                                            </label>
                                            <input
                                                type="text"
                                                value={editForm.shippingAddress}
                                                onChange={(e) => setEditForm({...editForm, shippingAddress: e.target.value})}
                                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                                                placeholder="Enter shipping address"
                                            />
                                        </div>

                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                                Payment Method
                                            </label>
                                            <select
                                                value={editForm.paymentMethod}
                                                onChange={(e) => setEditForm({...editForm, paymentMethod: e.target.value})}
                                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                                            >
                                                <option value="">Select payment method</option>
                                                <option value="CREDIT_CARD">Credit Card</option>
                                                <option value="DEBIT_CARD">Debit Card</option>
                                                <option value="PAYPAL">PayPal</option>
                                                <option value="BANK_TRANSFER">Bank Transfer</option>
                                                <option value="CASH_ON_DELIVERY">Cash on Delivery</option>
                                                <option value="MOBILE_MONEY">Mobile Money</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Order Items</label>
                                        <div className="space-y-3">
                                            {editForm.orderItems && editForm.orderItems.map((item, index) => {
                                                const itemPrice = parseFloat(item.price) || 0;
                                                const itemQuantity = parseInt(item.quantity) || 0;
                                                const itemTotal = itemPrice * itemQuantity;

                                                return (
                                                    <div key={index} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                                                        <div className="flex-1">
                                                            <p className="font-medium text-gray-900">{item.productName}</p>
                                                            <p className="text-sm text-gray-600">${itemPrice.toFixed(2)} each</p>
                                                        </div>
                                                        <div className="flex items-center gap-2">
                                                            <button
                                                                onClick={() => updateItemQuantity(index, item.quantity - 1)}
                                                                className="p-1 text-gray-600 hover:bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed"
                                                                disabled={item.quantity <= 1}
                                                            >
                                                                <Minus className="w-4 h-4" />
                                                            </button>
                                                            <span className="w-12 text-center font-medium">{item.quantity}</span>
                                                            <button
                                                                onClick={() => updateItemQuantity(index, item.quantity + 1)}
                                                                className="p-1 text-gray-600 hover:bg-gray-200 rounded"
                                                            >
                                                                <Plus className="w-4 h-4" />
                                                            </button>
                                                        </div>
                                                        <div className="w-24 text-right font-medium">
                                                            ${itemTotal.toFixed(2)}
                                                        </div>
                                                        <button
                                                            onClick={() => removeItem(index)}
                                                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
                                                            title="Remove Item"
                                                            disabled={editForm.orderItems.length === 1}
                                                        >
                                                            <Trash2 className="w-4 h-4" />
                                                        </button>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                        <div className="mt-3 pt-3 border-t flex justify-between items-center">
                                            <span className="font-semibold text-gray-900">Total:</span>
                                            <span className="text-xl font-bold text-gray-900">
                                                ${calculateTotal().toFixed(2)}
                                            </span>
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                                        <textarea
                                            value={editForm.notes}
                                            onChange={(e) => setEditForm({...editForm, notes: e.target.value})}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                                            rows="3"
                                            placeholder="Add any special instructions or notes"
                                        />
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleUpdateOrder(order.orderId)}
                                            className="btn-primary"
                                        >
                                            Save Changes
                                        </button>
                                        <button
                                            onClick={() => setEditingOrder(null)}
                                            className="btn-secondary"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <div className="flex items-center justify-between mb-4">
                                        <div>
                                            <h3 className="text-lg font-semibold text-gray-900">Order #{order.orderId}</h3>
                                            <p className="text-sm text-gray-600">
                                                {new Date(order.orderDate).toLocaleDateString('en-US', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric',
                                                    hour: '2-digit',
                                                    minute: '2-digit'
                                                })}
                                            </p>
                                        </div>
                                        <div className="flex items-center gap-3">
                                            <div className="text-right">
                                                <div className="flex gap-2 mb-2">
                                                    {getStatusBadge(order.status)}
                                                    {getPaymentStatusBadge(order.paymentStatus)}
                                                </div>
                                                <p className="text-lg font-bold text-gray-900">
                                                    ${(parseFloat(order.totalAmount) || 0).toFixed(2)}
                                                </p>
                                            </div>
                                            {order.status === 'PENDING' && (
                                                <div className="flex gap-2">
                                                    <button
                                                        onClick={() => handleEditOrder(order)}
                                                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                                                        title="Edit Order"
                                                    >
                                                        <Edit className="w-5 h-5" />
                                                    </button>
                                                    <button
                                                        onClick={() => handleCancelOrder(order.orderId)}
                                                        className="p-2 text-orange-600 hover:bg-orange-50 rounded-lg transition"
                                                        title="Cancel Order"
                                                    >
                                                        <X className="w-5 h-5" />
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteOrder(order.orderId)}
                                                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                                                        title="Delete Order"
                                                    >
                                                        <Trash2 className="w-5 h-5" />
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4 text-sm">
                                        <div>
                                            <span className="text-gray-600">Shipping Address:</span>
                                            <p className="font-medium text-gray-900">{order.shippingAddress || 'Not provided'}</p>
                                        </div>
                                        <div>
                                            <span className="text-gray-600">Payment Method:</span>
                                            <p className="font-medium text-gray-900">
                                                {order.paymentMethod ? order.paymentMethod.replace(/_/g, ' ') : 'Not specified'}
                                            </p>
                                        </div>
                                    </div>

                                    {order.notes && (
                                        <div className="mb-4 text-sm">
                                            <span className="text-gray-600">Notes:</span>
                                            <p className="font-medium text-gray-900">{order.notes}</p>
                                        </div>
                                    )}

                                    {order.orderItems && order.orderItems.length > 0 && (
                                        <div className="border-t pt-4">
                                            <h4 className="text-sm font-medium text-gray-700 mb-2">Items:</h4>
                                            <div className="space-y-2">
                                                {order.orderItems.map((item, index) => (
                                                    <div key={index} className="flex justify-between text-sm">
                                                        <span className="text-gray-600">
                                                            {item.productName} x {item.quantity}
                                                        </span>
                                                        <span className="text-gray-900 font-medium">
                                                            ${(parseFloat(item.subtotal) || 0).toFixed(2)}
                                                        </span>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default OrderHistory;
