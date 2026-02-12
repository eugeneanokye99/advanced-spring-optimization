import { useState, useEffect } from 'react';
import { useUserOrders, useUpdateOrderStatus, useUpdateOrder, useDeleteOrder } from '../../services/graphqlService';
import { useAuth } from '../../context/AuthContext';
import { Package, Clock, Truck, CheckCircle, XCircle, Trash2, X, Edit, Plus, Minus, ChevronLeft, ChevronRight, Search, Filter, ArrowUpDown } from 'lucide-react';
import { showErrorAlert, showSuccessToast, showWarningToast } from '../../utils/errorHandler';

const OrderHistory = () => {
    const [editingOrder, setEditingOrder] = useState(null);
    const [editForm, setEditForm] = useState({});
    const [currentPage, setCurrentPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [paymentStatusFilter, setPaymentStatusFilter] = useState('ALL');
    const [sortBy, setSortBy] = useState('orderDate');
    const [sortDirection, setSortDirection] = useState('DESC');
    
    const { user } = useAuth();
    const pageSize = 10;

    const filter = {
        searchTerm: searchTerm.trim() || null,
        status: statusFilter === 'ALL' ? null : statusFilter,
        paymentStatus: paymentStatusFilter === 'ALL' ? null : paymentStatusFilter
    };

    const { data, loading, error, refetch } = useUserOrders(
        user?.userId, 
        filter,
        currentPage, 
        pageSize,
        sortBy,
        sortDirection
    );
    
    useEffect(() => {
        if (error) {
            const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to load orders';
            showErrorAlert({ message: errorMessage }, 'Order Sync Failed');
        }
    }, [error]);

    useEffect(() => {
        setCurrentPage(0);
    }, [searchTerm, statusFilter, paymentStatusFilter, sortBy, sortDirection]);
    const [updateOrderStatusMutation] = useUpdateOrderStatus();
    const [updateOrderMutation] = useUpdateOrder();
    const [deleteOrderMutation] = useDeleteOrder();

    const orders = data?.orders?.orders || [];
    const pageInfo = data?.orders?.pageInfo || {};

    const handleCancelOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to cancel this order?')) return;
        try {
            await updateOrderStatusMutation({
                variables: { 
                    id: orderId.toString(), 
                    status: 'CANCELLED' 
                }
            });
            showSuccessToast('Order cancelled successfully');
            // Refetch data to show updated status
            refetch();
        } catch (error) {
            const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to cancel order';
            showErrorAlert({ message: errorMessage }, 'Failed to cancel order');
        }
    };

    const handleDeleteOrder = async (orderId) => {
        if (!window.confirm('Are you sure you want to delete this order?')) return;
        try {
            await deleteOrderMutation({
                variables: { id: orderId.toString() }
            });
            showSuccessToast('Order deleted successfully');
            refetch();
        } catch (error) {
            const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to delete order';
            showErrorAlert({ message: errorMessage }, 'Failed to delete order');
        }
    };

    const handleEditOrder = (order) => {
        console.log('Editing order:', order);
        setEditingOrder(order.orderId);
        setEditForm({
            shippingAddress: order.shippingAddress || '',
            paymentMethod: order.paymentMethod || 'CASH',
            notes: order.notes || '',
            orderItems: (order.orderItems || []).map(item => {
                const unitPrice = item.unitPrice || item.price || (item.subtotal / item.quantity) || 0;
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
        
        await updateOrderMutation({
            variables: {
                id: orderId.toString(),
                input: updateData
            }
        });

        showSuccessToast('Order updated successfully');
        setEditingOrder(null);
        refetch();
    } catch (error) {
        console.error('Update error:', error);
        const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to update order';
        showErrorAlert({ message: errorMessage }, 'Failed to update order');
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
            showWarningToast('Order must have at least one item');
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
        const normalizedStatus = (status || 'PENDING').toUpperCase();
        const badges = {
            PENDING: { color: 'bg-yellow-100 text-yellow-800', icon: Clock },
            PROCESSING: { color: 'bg-blue-100 text-blue-800', icon: CheckCircle },
            SHIPPED: { color: 'bg-purple-100 text-purple-800', icon: Truck },
            DELIVERED: { color: 'bg-green-100 text-green-800', icon: Package },
            CANCELLED: { color: 'bg-red-100 text-red-800', icon: XCircle },
        };
        const badge = badges[normalizedStatus] || badges.PENDING;
        const Icon = badge.icon;
        return (
            <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${badge.color}`}>
                <Icon className="w-3 h-3" /> {normalizedStatus}
            </span>
        );
    };

    const getPaymentStatusBadge = (status) => {
        const normalizedStatus = (status || 'UNPAID').toUpperCase();
        const badges = {
            PAID: 'bg-green-100 text-green-800',
            UNPAID: 'bg-red-100 text-red-800',
            PENDING: 'bg-yellow-100 text-yellow-800',
        };
        return (
            <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${badges[normalizedStatus] || badges.UNPAID}`}>
                {normalizedStatus}
            </span>
        );
    };

    if (loading && !data) {
        return (
            <div className="flex justify-center py-12">
                <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <h1 className="text-3xl font-bold text-gray-900">Order History</h1>
                
                <div className="flex flex-wrap items-center gap-3">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search orders..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 text-sm w-full md:w-64"
                        />
                    </div>
                    
                    <div className="flex items-center gap-2 text-sm">
                        <Filter className="w-4 h-4 text-gray-400" />
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 py-2 pl-2 pr-8 text-sm"
                        >
                            <option value="ALL">All Status</option>
                            <option value="PENDING">Pending</option>
                            <option value="PROCESSING">Processing</option>
                            <option value="SHIPPED">Shipped</option>
                            <option value="DELIVERED">Delivered</option>
                            <option value="CANCELLED">Cancelled</option>
                        </select>

                        <select
                            value={paymentStatusFilter}
                            onChange={(e) => setPaymentStatusFilter(e.target.value)}
                            className="border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 py-2 pl-2 pr-8 text-sm"
                        >
                            <option value="ALL">All Payment</option>
                            <option value="PAID">Paid</option>
                            <option value="UNPAID">Unpaid</option>
                            <option value="PENDING">Pending</option>
                        </select>
                        
                        <select
                            value={sortBy}
                            onChange={(e) => setSortBy(e.target.value)}
                            className="border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 py-2 pl-2 pr-8 text-sm"
                        >
                            <option value="orderDate">Order Date</option>
                            <option value="totalAmount">Total Amount</option>
                            <option value="orderId">Order ID</option>
                        </select>
                        
                        <button
                            onClick={() => setSortDirection(sortDirection === 'ASC' ? 'DESC' : 'ASC')}
                            className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 bg-white"
                            title={sortDirection === 'ASC' ? 'Sort Ascending' : 'Sort Descending'}
                        >
                            <ArrowUpDown className={`w-4 h-4 text-gray-600 transition-transform ${sortDirection === 'ASC' ? 'rotate-180' : ''}`} />
                        </button>
                    </div>
                </div>
            </div>

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
                                                <option value="CASH">Cash</option>
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
                                            {order.status && order.status.toUpperCase() === 'PENDING' && (
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
                                                {order.paymentMethod ? order.paymentMethod.replace(/_/g, ' ') : 'Cash'}
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

            {/* Pagination Controls */}
            {pageInfo.totalPages > 1 && (
                <div className="mt-8 flex items-center justify-center gap-4">
                    <button
                        onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                        disabled={currentPage === 0}
                        className="p-2 rounded-xl bg-white border border-gray-100 shadow-sm disabled:opacity-50 hover:bg-gray-50 transition-colors"
                    >
                        <ChevronLeft className="w-6 h-6" />
                    </button>
                    <div className="flex gap-2">
                        {[...Array(pageInfo.totalPages)].map((_, i) => (
                            <button
                                key={i}
                                onClick={() => setCurrentPage(i)}
                                className={`w-10 h-10 rounded-xl font-bold transition-all ${currentPage === i
                                    ? 'bg-primary-600 text-black shadow-lg shadow-primary-200'
                                    : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-100'
                                    }`}
                            >
                                {i + 1}
                            </button>
                        ))}
                    </div>
                    <button
                        onClick={() => setCurrentPage(p => Math.min(pageInfo.totalPages - 1, p + 1))}
                        disabled={currentPage === pageInfo.totalPages - 1}
                        className="p-2 rounded-xl bg-white border border-gray-100 shadow-sm disabled:opacity-50 hover:bg-gray-50 transition-colors"
                    >
                        <ChevronRight className="w-6 h-6" />
                    </button>
                </div>
            )}
        </div>
    );
};

export default OrderHistory;
