import { useState, useEffect } from 'react';
import { useAllOrders, useUpdateOrderStatus } from '../../services/graphqlService';
import { Package, Clock, Truck, CheckCircle, XCircle, Search, Eye, Filter, MapPin, CreditCard, User, ShoppingBag, ChevronLeft, ChevronRight, DollarSign, ArrowUpDown } from 'lucide-react';
import { showErrorAlert } from '../../utils/errorHandler';
import { processPayment } from '../../services/orderService';
import { toast } from 'react-hot-toast';

const OrderManagement = () => {
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [paymentStatusFilter, setPaymentStatusFilter] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');
    const [expandedOrder, setExpandedOrder] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [sortBy, setSortBy] = useState('orderDate');
    const [sortDirection, setSortDirection] = useState('DESC');
    const pageSize = 20;

    const filter = {
        status: statusFilter === 'ALL' ? null : statusFilter,
        paymentStatus: paymentStatusFilter === 'ALL' ? null : paymentStatusFilter,
        searchTerm: searchTerm.trim() || null
    };

    const { data, loading, error, refetch } = useAllOrders(filter, currentPage, pageSize, sortBy, sortDirection);
    const [updateOrderStatusMutation] = useUpdateOrderStatus();

    const orders = data?.orders?.orders || [];
    const pageInfo = data?.orders?.pageInfo || {};

    useEffect(() => {
        if (error) {
            const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to sync orders';
            showErrorAlert({ message: errorMessage }, 'Order Sync Error');
        }
    }, [error]);

    useEffect(() => {
        setCurrentPage(0);
    }, [statusFilter, paymentStatusFilter, searchTerm, sortBy, sortDirection]);

    const handleStatusChange = async (orderId, newStatus) => {
        try {
            await updateOrderStatusMutation({
                variables: {
                    id: orderId.toString(),
                    status: newStatus 
                },
                // Optimistically update the cache
                optimisticResponse: {
                    updateOrderStatus: {
                        __typename: 'Order',
                        id: orderId.toString(),
                        status: newStatus,
                        orderDate: new Date().toISOString(),
                        totalAmount: 0
                    }
                }
            });

            toast.success(`Order status updated to ${newStatus}`);

            // Refetch to ensure we have the latest data with all fields
            await refetch();
        } catch (error) {
            console.error('Error updating order status:', error);
            const errorMessage = error?.graphQLErrors?.[0]?.message || error.message || 'Failed to update order status';
            showErrorAlert(error, errorMessage);
            // Refetch to restore correct state if error occurred
            await refetch();
        }
    };

    const handleProcessPayment = async (orderId) => {
        const transactionId = `TXN-${Math.random().toString(36).substr(2, 9).toUpperCase()}`;
        try {
            toast.loading('Processing payment...', { id: 'payment' });
            await processPayment(orderId, transactionId);
            toast.success('Payment processed. Order is now PROCESSING', { id: 'payment' });
            refetch();
        } catch (error) {
            console.error('Error processing payment:', error);
            toast.error(error.response?.data?.message || 'Failed to process payment', { id: 'payment' });
        }
    };

    const getStatusBadge = (status) => {
        const normalizedStatus = (status || 'PENDING').toUpperCase();
        const badges = {
            PENDING: { color: 'bg-yellow-100 text-yellow-800' },
            PROCESSING: { color: 'bg-blue-100 text-blue-800' },
            SHIPPED: { color: 'bg-indigo-100 text-indigo-800' },
            DELIVERED: { color: 'bg-emerald-100 text-emerald-800' },
            CANCELLED: { color: 'bg-rose-100 text-rose-800' },
        };
        const Icons = {
            PENDING: Clock,
            PROCESSING: CheckCircle,
            SHIPPED: Truck,
            DELIVERED: Package,
            CANCELLED: XCircle,
        };
        const badge = badges[normalizedStatus] || badges.PENDING;
        const Icon = Icons[normalizedStatus] || Icons.PENDING;
        return (
            <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${badge.color}`}>
                <Icon className="w-3 h-3" /> {normalizedStatus}
            </span>
        );
    };
    return (
        <div className="space-y-8">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">Order Operations</h1>
                    <p className="text-gray-500 mt-2">Manage customer orders and workflow transitions.</p>
                </div>

                <div className="flex flex-wrap items-center gap-4 bg-white p-2 rounded-2xl shadow-sm border border-gray-100">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search orders..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10 pr-4 py-2 bg-gray-50 border-none rounded-xl text-xs font-bold focus:ring-2 focus:ring-primary-500 transition-all w-48"
                        />
                    </div>

                    <div className="flex items-center gap-2 px-3 text-gray-400 border-l border-gray-100">
                        <Filter className="w-4 h-4" />
                        <span className="text-xs font-black uppercase">Status</span>
                    </div>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="bg-gray-50 border-none rounded-xl text-xs font-bold py-2 px-4 focus:ring-2 focus:ring-primary-500 transition-all"
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
                        className="bg-gray-50 border-none rounded-xl text-xs font-bold py-2 px-4 focus:ring-2 focus:ring-primary-500 transition-all"
                    >
                        <option value="ALL">All Payment</option>
                        <option value="PAID">Paid</option>
                        <option value="UNPAID">Unpaid</option>
                        <option value="PENDING">Pending</option>
                    </select>

                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="bg-gray-50 border-none rounded-xl text-xs font-bold py-2 px-4 focus:ring-2 focus:ring-primary-500 transition-all"
                    >
                        <option value="orderDate">Date</option>
                        <option value="totalAmount">Amount</option>
                        <option value="id">ID</option>
                    </select>

                    <button
                        onClick={() => setSortDirection(sortDirection === 'ASC' ? 'DESC' : 'ASC')}
                        className="p-2 bg-gray-50 rounded-xl hover:bg-gray-100 transition-all"
                    >
                        <ArrowUpDown className={`w-4 h-4 text-gray-600 ${sortDirection === 'ASC' ? 'rotate-180' : ''}`} />
                    </button>
                </div>
            </div>

            <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-x-auto">
                <table className="w-full text-left border-collapse min-w-[1000px]">
                    <thead className="bg-gray-50/50 border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest">Order Details</th>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest">Customer & Items</th>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest">Shipping & Payment</th>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest text-right">Amount</th>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest text-center">Status</th>
                            <th className="px-6 py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                        {loading ? (
                            <tr>
                                <td colSpan="6" className="px-6 py-24 text-center">
                                    <div className="w-10 h-10 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
                                    <p className="mt-4 text-sm font-medium text-gray-500 uppercase tracking-widest">Syncing Orders...</p>
                                </td>
                            </tr>
                        ) : orders.length === 0 ? (
                            <tr>
                                <td colSpan="6" className="px-6 py-24 text-center">
                                    <div className="w-16 h-16 bg-gray-50 text-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
                                        <Search className="w-8 h-8" />
                                    </div>
                                    <p className="text-gray-400 font-bold">No orders match your criteria</p>
                                </td>
                            </tr>
                        ) : (
                            orders.map((order) => (
                                <>
                                    <tr key={order.id} className="hover:bg-gray-50/50 transition-colors group border-b border-gray-100">
                                        <td className="px-6 py-5">
                                            <div className="flex flex-col">
                                                <div className="flex items-center gap-2">
                                                    <span className="text-sm font-black text-gray-900">#{order.id}</span>
                                                    <button
                                                        onClick={() => setExpandedOrder(expandedOrder === order.id ? null : order.id)}
                                                        className="p-1 text-gray-400 hover:text-gray-600 transition-colors"
                                                        title="View Details"
                                                    >
                                                        <Eye className="w-4 h-4" />
                                                    </button>
                                                </div>
                                                <span className="text-[10px] text-gray-400 font-medium mt-1">
                                                    {new Date(order.orderDate).toLocaleDateString('en-US', {
                                                        year: 'numeric',
                                                        month: 'short',
                                                        day: 'numeric',
                                                        hour: '2-digit',
                                                        minute: '2-digit'
                                                    })}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-5">
                                            <div className="flex items-start gap-3">
                                                <div className="w-8 h-8 bg-primary-50 rounded-full flex items-center justify-center text-[10px] font-black text-primary-700 flex-shrink-0">
                                                    <User className="w-4 h-4" />
                                                </div>
                                                <div className="flex flex-col min-w-0">
                                                    <span className="text-sm font-bold text-gray-700 truncate">{order.user.firstName} {order.user.lastName || 'Unknown Customer'}</span>
                                                    <div className="text-[10px] text-gray-400 font-medium space-y-1">
                                                        <div className="flex items-center gap-1">
                                                            <ShoppingBag className="w-3 h-3" />
                                                            <span>{order.orderItems ? order.orderItems.length : 0} items</span>
                                                        </div>
                                                        {order.orderItems && order.orderItems.length > 0 && (
                                                            <div className="max-w-xs">
                                                                <span className="text-gray-600 truncate block">
                                                                    {order.orderItems.slice(0, 2).map(item => item.productName).join(', ')}
                                                                    {order.orderItems.length > 2 && ` +${order.orderItems.length - 2} more`}
                                                                </span>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-5">
                                            <div className="flex flex-col space-y-2 text-[10px]">
                                                <div className="flex items-start gap-1">
                                                    <MapPin className="w-3 h-3 text-gray-400 mt-0.5 flex-shrink-0" />
                                                    <span className="text-gray-600 line-clamp-2 max-w-xs">
                                                        {order.shippingAddress || 'No address provided'}
                                                    </span>
                                                </div>
                                                <div className="flex items-center gap-1">
                                                    <CreditCard className="w-3 h-3 text-gray-400 flex-shrink-0" />
                                                    <span className="text-gray-600">
                                                        {order.paymentMethod ? order.paymentMethod.replace(/_/g, ' ') : 'Cash'}
                                                    </span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-5 text-right">
                                            <span className="text-sm font-black text-gray-900">${(order.totalAmount || 0).toFixed(2)}</span>
                                        </td>
                                        <td className="px-6 py-5 text-center">
                                            {getStatusBadge(order.status)}
                                        </td>
                                        <td className="px-6 py-5 text-right">
                                            <div className="flex justify-end gap-2">
                                                {order.status && order.status.toUpperCase() === 'PENDING' && (
                                                    <>
                                                        <button
                                                            onClick={() => handleProcessPayment(order.id)}
                                                            className="px-3 py-1.5 bg-emerald-600 text-white text-[10px] font-black uppercase tracking-wider rounded-lg hover:bg-emerald-700 transition-all shadow-lg shadow-emerald-100 flex items-center gap-1.5 whitespace-nowrap"
                                                        >
                                                            <DollarSign className="w-3.5 h-3.5" /> Pay
                                                        </button>
                                                        <button
                                                            onClick={() => handleStatusChange(order.id, 'PROCESSING')}
                                                            className="px-3 py-1.5 bg-blue-600 text-white text-[10px] font-black uppercase tracking-wider rounded-lg hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 whitespace-nowrap"
                                                        >
                                                            Accept
                                                        </button>
                                                    </>
                                                )}
                                                {order.status && order.status.toUpperCase() === 'PROCESSING' && (
                                                    <button
                                                        onClick={() => handleStatusChange(order.id, 'SHIPPED')}
                                                        className="px-3 py-1.5 bg-indigo-600 text-white text-[10px] font-black uppercase tracking-wider rounded-lg hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-100 whitespace-nowrap"
                                                    >
                                                        Ship
                                                    </button>
                                                )}
                                                {order.status && order.status.toUpperCase() === 'SHIPPED' && (
                                                    <button
                                                        onClick={() => handleStatusChange(order.id, 'DELIVERED')}
                                                        className="px-3 py-1.5 bg-emerald-600 text-white text-[10px] font-black uppercase tracking-wider rounded-lg hover:bg-emerald-700 transition-all shadow-lg shadow-emerald-100 whitespace-nowrap"
                                                    >
                                                        Deliver
                                                    </button>
                                                )}
                                                {order.status && ['PENDING', 'PROCESSING'].includes(order.status.toUpperCase()) && (
                                                    <button
                                                        onClick={() => handleStatusChange(order.id, 'CANCELLED')}
                                                        className="px-3 py-1.5 bg-white text-rose-600 border border-rose-100 text-[10px] font-black uppercase tracking-wider rounded-lg hover:bg-rose-50 transition-all whitespace-nowrap"
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                    {expandedOrder === order.id && (
                                        <tr className="bg-gray-50/30">
                                            <td colSpan="6" className="px-6 py-4">
                                                <div className="bg-white rounded-xl p-4 border border-gray-100">
                                                    <h4 className="text-sm font-bold text-gray-900 mb-3 flex items-center gap-2">
                                                        <Package className="w-4 h-4" />
                                                        Order Details - #{order.id}
                                                    </h4>
                                                    
                                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                                        {/* Customer Information */}
                                                        <div className="space-y-3">
                                                            <h5 className="text-xs font-bold text-gray-700 uppercase tracking-wider">Customer Information</h5>
                                                            <div className="space-y-2 text-sm">
                                                                <div>
                                                                    <span className="text-gray-600">Name:</span>
                                                                    <p className="font-medium text-gray-900">{order.userName || 'Unknown Customer'}</p>
                                                                </div>
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
                                                        </div>
                                                        
                                                        {/* Order Items */}
                                                        <div className="md:col-span-2 space-y-3">
                                                            <h5 className="text-xs font-bold text-gray-700 uppercase tracking-wider">Order Items</h5>
                                                            {order.orderItems && order.orderItems.length > 0 ? (
                                                                <div className="space-y-2">
                                                                    {order.orderItems.map((item, index) => (
                                                                        <div key={index} className="flex justify-between items-center p-3 bg-gray-50/50 rounded-lg">
                                                                            <div className="flex-1">
                                                                                <div className="font-medium text-gray-900">{item.productName || 'Unknown Product'}</div>
                                                                                <div className="text-sm text-gray-600">
                                                                                    ${((item.subtotal || 0) / (item.quantity || 1)).toFixed(2)} each × {item.quantity || 1}
                                                                                </div>
                                                                            </div>
                                                                            <div className="text-right">
                                                                                <div className="font-bold text-gray-900">${(item.subtotal || 0).toFixed(2)}</div>
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                    <div className="pt-2 mt-3 border-t border-gray-200">
                                                                        <div className="flex justify-between items-center text-sm font-bold">
                                                                            <span className="text-gray-700">Total Amount:</span>
                                                                            <span className="text-lg text-gray-900">${(order.totalAmount || 0).toFixed(2)}</span>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            ) : (
                                                                <p className="text-gray-500 italic">No items found</p>
                                                            )}
                                                            
                                                            {order.notes && (
                                                                <div className="mt-4">
                                                                    <span className="text-gray-600 text-xs font-bold uppercase tracking-wider">Customer Notes:</span>
                                                                    <p className="mt-1 text-sm font-medium text-gray-900 bg-blue-50 p-3 rounded-lg">{order.notes}</p>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Pagination Controls */}
            {pageInfo.totalPages > 1 && (
                <div className="flex items-center justify-between bg-white px-6 py-4 rounded-2xl border border-gray-100 shadow-sm mt-4">
                    <div className="text-sm text-gray-500 font-medium">
                        Showing <span className="font-bold text-gray-900">{orders.length}</span> of <span className="font-bold text-gray-900">{pageInfo.totalElements}</span> orders
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                            disabled={currentPage === 0}
                            className="p-2 rounded-xl hover:bg-gray-50 border border-gray-100 disabled:opacity-30 transition-all"
                        >
                            <ChevronLeft className="w-5 h-5" />
                        </button>
                        <span className="text-sm font-black px-4 py-2 bg-primary-50 text-primary-900 rounded-xl">
                            PAGE {currentPage + 1} OF {pageInfo.totalPages}
                        </span>
                        <button
                            onClick={() => setCurrentPage(p => Math.min(pageInfo.totalPages - 1, p + 1))}
                            disabled={currentPage >= pageInfo.totalPages - 1}
                            className="p-2 rounded-xl hover:bg-gray-50 border border-gray-100 disabled:opacity-30 transition-all"
                        >
                            <ChevronRight className="w-5 h-5" />
                        </button>
                    </div>
                </div>
            )}

            {/* Order Summary Statistics */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-8">
                <div className="bg-blue-50/50 p-6 rounded-3xl border border-blue-50">
                    <div className="flex items-center gap-3">
                        <Clock className="w-8 h-8 text-blue-600" />
                        <div>
                            <h4 className="text-xs font-black text-blue-900 uppercase tracking-widest">Pending Orders</h4>
                            <p className="text-2xl font-black text-blue-700">{orders.filter(o => (o.status || '').toUpperCase() === 'PENDING').length}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-indigo-50/50 p-6 rounded-3xl border border-indigo-50">
                    <div className="flex items-center gap-3">
                        <CheckCircle className="w-8 h-8 text-indigo-600" />
                        <div>
                            <h4 className="text-xs font-black text-indigo-900 uppercase tracking-widest">Processing</h4>
                            <p className="text-2xl font-black text-indigo-700">{orders.filter(o => (o.status || '').toUpperCase() === 'PROCESSING').length}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-emerald-50/50 p-6 rounded-3xl border border-emerald-50">
                    <div className="flex items-center gap-3">
                        <Truck className="w-8 h-8 text-emerald-600" />
                        <div>
                            <h4 className="text-xs font-black text-emerald-900 uppercase tracking-widest">Shipped</h4>
                            <p className="text-2xl font-black text-emerald-700">{orders.filter(o => (o.status || '').toUpperCase() === 'SHIPPED').length}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-gray-50/50 p-6 rounded-3xl border border-gray-50">
                    <div className="flex items-center gap-3">
                        <Package className="w-8 h-8 text-gray-600" />
                        <div>
                            <h4 className="text-xs font-black text-gray-700 uppercase tracking-widest">Total Orders</h4>
                            <p className="text-2xl font-black text-gray-900">{orders.length}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Workflow Guide */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-blue-50/50 p-6 rounded-3xl border border-blue-50">
                    <h4 className="text-xs font-black text-blue-900 uppercase tracking-widest mb-2">Process Order</h4>
                    <p className="text-xs text-blue-700 leading-relaxed">Accepting an order moves it to processing, notifying the warehouse to begin fulfillment.</p>
                </div>
                <div className="bg-indigo-50/50 p-6 rounded-3xl border border-indigo-50">
                    <h4 className="text-xs font-black text-indigo-900 uppercase tracking-widest mb-2">Shipping Logic</h4>
                    <p className="text-xs text-indigo-700 leading-relaxed">Once items are boxed, mark as shipped to provide the customer with tracking updates.</p>
                </div>
                <div className="bg-emerald-50/50 p-6 rounded-3xl border border-emerald-50">
                    <h4 className="text-xs font-black text-emerald-900 uppercase tracking-widest mb-2">Final Delivery</h4>
                    <p className="text-xs text-emerald-700 leading-relaxed">Completing an order records the final transaction and completes the customer journey.</p>
                </div>
            </div>
        </div>
    );
};

export default OrderManagement;
