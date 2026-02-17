import {
    ShoppingBag,
    Package,
    ShoppingCart,
    TrendingUp,
    DollarSign,
    CreditCard,
    Star,
    ChevronRight,
    Activity,
    Clock,
    Target
} from 'lucide-react';
import { useUserAnalytics, transformUserAnalytics } from '../../services/graphqlService';
import { useAuth } from '../../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const CustomerDashboardHome = () => {
    const { user } = useAuth();
    const { data: rawData, loading, error, refetch } = useUserAnalytics(user?.id);

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mb-4"></div>
                <p className="text-gray-500 font-medium">Preparing your personalized dashboard...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <div className="text-red-600 mb-4">Error loading dashboard:</div>
                <p className="text-gray-500 mb-4">{error.message}</p>
                <button 
                    onClick={refetch}
                    className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
                >
                    Try Again
                </button>
            </div>
        );
    }

    const analytics = transformUserAnalytics(rawData);
    if (!analytics) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <p className="text-gray-500 font-medium">No data available</p>
            </div>
        );
    }

    const COLORS = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

    const summaryCards = [
        {
            label: 'Total Spent',
            value: `$${analytics.totalSpent.toFixed(2)}`,
            icon: DollarSign,
            color: 'text-emerald-600',
            bgColor: 'bg-emerald-50'
        },
        {
            label: 'Total Orders',
            value: analytics.totalOrders,
            icon: Package,
            color: 'text-blue-600',
            bgColor: 'bg-blue-50'
        },
        {
            label: 'Items Bought',
            value: analytics.totalItemsPurchased,
            icon: ShoppingBag,
            color: 'text-indigo-600',
            bgColor: 'bg-indigo-50'
        },
        {
            label: 'Average Order',
            value: `$${(analytics.totalSpent / (analytics.totalOrders || 1)).toFixed(2)}`,
            icon: CreditCard,
            color: 'text-purple-600',
            bgColor: 'bg-purple-50'
        }
    ];

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            {/* Greeting Header */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">
                        Welcome back, <span className="text-primary-600">{user.firstName}</span>!
                    </h1>
                    <p className="text-gray-500 mt-2 text-lg">Here's what's happening with your account today.</p>
                </div>
                <div className="flex gap-3">
                    <button className="btn-primary flex items-center gap-2 px-6">
                        <ShoppingCart className="w-5 h-5" />
                        Go Shopping
                    </button>
                </div>
            </div>

            {/* Quick Stats Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                {summaryCards.map((card, idx) => (
                    <div key={idx} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-all duration-300">
                        <div className="flex items-center justify-between mb-4">
                            <div className={`${card.bgColor} p-3 rounded-2xl`}>
                                <card.icon className={`w-6 h-6 ${card.color}`} />
                            </div>
                        </div>
                        <div>
                            <p className="text-sm font-bold text-gray-400 uppercase tracking-widest">{card.label}</p>
                            <p className="text-3xl font-black text-gray-900 mt-1">{card.value}</p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Spending by Category Chart */}
                <div className="lg:col-span-2 bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h3 className="text-xl font-bold text-gray-900">Spending by Category</h3>
                            <p className="text-sm text-gray-500 mt-1">Your favorite product types</p>
                        </div>
                        <Target className="w-6 h-6 text-gray-300" />
                    </div>
                    {analytics.spendingByCategory && analytics.spendingByCategory.length > 0 ? (
                        <div className="h-72 w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={analytics.spendingByCategory}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                                    <XAxis
                                        dataKey="categoryName"
                                        axisLine={false}
                                        tickLine={false}
                                        fontSize={12}
                                        tick={{ fill: '#9ca3af', fontWeight: 500 }}
                                    />
                                    <YAxis
                                        axisLine={false}
                                        tickLine={false}
                                        fontSize={12}
                                        tick={{ fill: '#9ca3af', fontWeight: 500 }}
                                        tickFormatter={(val) => `$${val}`}
                                    />
                                    <Tooltip
                                        cursor={{ fill: '#f9fafb' }}
                                        contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                                    />
                                    <Bar dataKey="amountSpent" radius={[8, 8, 0, 0]}>
                                        {analytics.spendingByCategory.map((_, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    ) : (
                        <div className="h-72 flex flex-col items-center justify-center text-gray-400">
                            <Activity className="w-12 h-12 mb-3 opacity-20" />
                            <p>No spending data yet. Start shopping!</p>
                        </div>
                    )}
                </div>

                {/* Recent Activity Feed */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-8">
                        <h3 className="text-xl font-bold text-gray-900">Recent Activity</h3>
                        <Clock className="w-6 h-6 text-gray-300" />
                    </div>
                    <div className="space-y-6">
                        {analytics.recentActivities && analytics.recentActivities.length > 0 ? (
                            analytics.recentActivities.map((activity, idx) => (
                                <div key={idx} className="flex gap-4 group">
                                    <div className={`mt-1 flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center ${activity.type === 'PURCHASE' ? 'bg-blue-50 text-blue-600' : 'bg-yellow-50 text-yellow-600'
                                        }`}>
                                        {activity.type === 'PURCHASE' ? <ShoppingBag className="w-5 h-5" /> : <Star className="w-5 h-5" />}
                                    </div>
                                    <div className="flex-1">
                                        <p className="text-sm font-bold text-gray-900 group-hover:text-primary-600 transition-colors uppercase tracking-tight">
                                            {activity.description}
                                        </p>
                                        <p className="text-xs text-gray-400 font-medium mt-1">{activity.date}</p>
                                    </div>
                                    <ChevronRight className="w-4 h-4 text-gray-200 self-center group-hover:translate-x-1 transition-transform" />
                                </div>
                            ))
                        ) : (
                            <p className="text-center text-gray-500 py-12">No recent activity detected.</p>
                        )}
                        <button className="w-full py-3 mt-4 text-sm font-bold text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-2xl transition-all">
                            View Full History
                        </button>
                    </div>
                </div>
            </div>

            {/* CTA Section */}
            <div className="bg-gradient-to-r from-primary-600 to-indigo-600 rounded-3xl p-8 text-white relative overflow-hidden group">
                <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
                    <div>
                        <h3 className="text-2xl font-bold mb-2">Ready for your next find?</h3>
                        <p className="text-primary-100 max-w-md">Discover new arrivals in your favorite categories and enjoy exclusive member discounts.</p>
                    </div>
                    <button className="px-8 py-4 bg-white text-primary-700 font-black rounded-2xl shadow-xl hover:scale-105 transition-transform">
                        Explore Shop
                    </button>
                </div>
                {/* Decorative background elements */}
                <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/3 blur-3xl group-hover:scale-150 transition-transform duration-1000"></div>
                <div className="absolute bottom-0 left-0 w-32 h-32 bg-black/10 rounded-full translate-y-1/2 -translate-x-1/2 blur-2xl"></div>
            </div>
        </div>
    );
};

export default CustomerDashboardHome;
