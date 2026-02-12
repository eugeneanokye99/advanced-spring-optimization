import { Package, ShoppingCart, Users, TrendingUp, AlertTriangle, DollarSign, Activity, RefreshCw } from 'lucide-react';
import { useDashboardAnalytics, transformDashboardData } from '../../services/graphqlService';
import { getPerformanceMetrics } from '../../services/analyticsService';
import { useState, useEffect } from 'react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    LineChart, Line, PieChart, Pie, Cell, Legend
} from 'recharts';

const Dashboard = () => {
    const { data: rawData, loading: gqlLoading, error: gqlError, refetch } = useDashboardAnalytics();
    const [performanceMetrics, setPerformanceMetrics] = useState(null);
    const [perfLoading, setPerfLoading] = useState(true);

    const fetchPerformance = async () => {
        setPerfLoading(true);
        try {
            const res = await getPerformanceMetrics();
            setPerformanceMetrics(res.data);
        } catch (err) {
            console.error("Dashboard performance fetch error:", err);
        } finally {
            setPerfLoading(false);
        }
    };

    useEffect(() => {
        fetchPerformance();
    }, []);
    
    if (gqlLoading || perfLoading) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <div className="w-12 h-12 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mb-4"></div>
                <p className="text-gray-500 font-medium">Crunching your numbers...</p>
            </div>
        );
    }

    if (gqlError) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <div className="text-red-600 mb-4">Error loading dashboard data:</div>
                <p className="text-gray-500 mb-4">{gqlError.message}</p>
                <button 
                    onClick={refetch}
                    className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
                >
                    Try Again
                </button>
            </div>
        );
    }

    const data = transformDashboardData(rawData);
    if (!data) {
        return (
            <div className="flex flex-col items-center justify-center h-96">
                <p className="text-gray-500 font-medium">No data available</p>
            </div>
        );
    }

    // Merge real performance metrics if available
    if (performanceMetrics) {
        data.performanceMetrics = performanceMetrics;
    }

    const { overallStats, salesOverTime, categoryDistribution, lowStockProducts } = data;
    const lowStockCount = lowStockProducts.length;

    const statCards = [
        {
            title: 'Total Revenue',
            value: `$${overallStats.totalRevenue.toLocaleString()}`,
            icon: DollarSign,
            color: 'text-emerald-600',
            bgColor: 'bg-emerald-50',
        },
        {
            title: 'Total Orders',
            value: overallStats.totalOrders,
            icon: ShoppingCart,
            color: 'text-blue-600',
            bgColor: 'bg-blue-50',
        },
        {
            title: 'Total Products',
            value: overallStats.totalProducts,
            icon: Package,
            color: 'text-indigo-600',
            bgColor: 'bg-indigo-50',
        },
        {
            title: 'Total Users',
            value: overallStats.totalUsers,
            icon: Users,
            color: 'text-purple-600',
            bgColor: 'bg-purple-50',
        }
    ];

    const COLORS = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">System Analytics</h1>
                <p className="text-gray-500 mt-2">Comprehensive overview of your e-commerce performance.</p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {statCards.map((stat, index) => (
                    <div key={index} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
                        <div className="flex items-center justify-between">
                            <div className={`${stat.bgColor} p-3 rounded-2xl`}>
                                <stat.icon className={`w-6 h-6 ${stat.color}`} />
                            </div>
                        </div>
                        <div className="mt-4">
                            <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">{stat.title}</p>
                            <p className="text-3xl font-black text-gray-900 mt-1">{stat.value}</p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Sales Chart */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-8">
                        <h3 className="text-xl font-bold text-gray-900">Revenue Performance</h3>
                        <div className="px-3 py-1 bg-gray-100 rounded-lg text-xs font-bold text-gray-500">LAST 7 DAYS</div>
                    </div>
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={[...salesOverTime].reverse()}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                                <XAxis
                                    dataKey="date"
                                    stroke="#9ca3af"
                                    fontSize={12}
                                    tickLine={false}
                                    axisLine={false}
                                    tickFormatter={(val) => {
                                        const date = new Date(val);
                                        return `${date.getMonth() + 1}/${date.getDate()}`;
                                    }}
                                />
                                <YAxis
                                    stroke="#9ca3af"
                                    fontSize={12}
                                    tickLine={false}
                                    axisLine={false}
                                    tickFormatter={(val) => `$${val}`}
                                />
                                <Tooltip
                                    contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                                    formatter={(value) => [`$${value.toFixed(2)}`, 'Revenue']}
                                    labelFormatter={(val) => `Date: ${val}`}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="revenue"
                                    stroke="#4f46e5"
                                    strokeWidth={4}
                                    dot={{ r: 6, fill: '#4f46e5', strokeWidth: 2, stroke: '#fff' }}
                                    activeDot={{ r: 8, strokeWidth: 0 }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Category Pie Chart */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <h3 className="text-xl font-bold text-gray-900 mb-8">Products by Category</h3>
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={categoryDistribution}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={80}
                                    outerRadius={110}
                                    paddingAngle={5}
                                    dataKey="value"
                                    nameKey="name"
                                    fill="#8884d8"
                                >
                                    {categoryDistribution.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.fill || COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                                    formatter={(value, name) => [`${value} products`, name]}
                                />
                                <Legend verticalAlign="bottom" height={36} />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Performance Stats */}
            <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                <div className="flex items-center justify-between mb-8">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-indigo-50 rounded-lg">
                            <Activity className="w-5 h-5 text-indigo-600" />
                        </div>
                        <h3 className="text-xl font-bold text-gray-900">System Performance Metrics</h3>
                    </div>
                    <div className="flex gap-2">
                        {['api', 'service', 'database', 'sorting'].map(cat => (
                            <span key={cat} className="px-3 py-1 bg-gray-50 text-[10px] font-black text-gray-400 uppercase rounded-full border border-gray-100">
                                {cat}
                            </span>
                        ))}
                        <button 
                            onClick={fetchPerformance}
                            className="p-1 hover:bg-gray-100 rounded-full transition-colors"
                            title="Refresh Metrics"
                        >
                            <RefreshCw className="w-4 h-4 text-gray-400" />
                        </button>
                    </div>
                </div>

                <div className="space-y-8">
                    {data.performanceMetrics && ['api', 'service', 'database', 'sorting'].map(category => {
                        const categoryMetrics = Object.entries(data.performanceMetrics)
                            .filter(([key]) => key.startsWith(`${category}:`))
                            .sort((a, b) => b[1].average - a[1].average) // Show slowest first
                            .slice(0, 4);

                        if (categoryMetrics.length === 0) return null;

                        return (
                            <div key={category}>
                                <h4 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-4 flex items-center gap-2">
                                    <span className="w-2 h-2 rounded-full bg-primary-500"></span>
                                    {category} Layer execution
                                </h4>
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                                    {categoryMetrics.map(([key, stats]) => {
                                        const fullLabel = key.split(':').pop();
                                        const methodName = fullLabel.split('.').pop();
                                        return (
                                            <div key={key} className="p-5 bg-gray-50 rounded-3xl border border-transparent hover:border-primary-100 hover:bg-white transition-all group">
                                                <p className="text-[10px] font-bold text-gray-400 uppercase mb-3 truncate group-hover:text-primary-600" title={fullLabel}>
                                                    {methodName}
                                                </p>
                                                <div className="flex items-end justify-between">
                                                    <div>
                                                        <p className="text-2xl font-black text-gray-900">{stats.average}<span className="text-xs font-normal text-gray-400">ms</span></p>
                                                        <p className="text-[10px] text-gray-500 font-medium uppercase mt-1">Avg Time</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="text-sm font-black text-gray-700">{stats.callCount}</p>
                                                        <p className="text-[10px] text-gray-500 font-medium uppercase mt-1">Calls</p>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* Alerts */}
            {lowStockCount > 0 && (
                <div className="bg-red-50 border border-red-100 p-6 rounded-3xl flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-red-100 rounded-2xl">
                            <AlertTriangle className="w-6 h-6 text-red-600" />
                        </div>
                        <div>
                            <p className="text-red-900 font-bold">Low Stock Warning</p>
                            <p className="text-red-700 text-sm">There are {lowStockCount} items currently below their reorder point.</p>
                        </div>
                    </div>
                    <button className="px-6 py-2 bg-red-600 text-white font-bold rounded-xl hover:bg-red-700 transition-colors">
                        Manage Inventory
                    </button>
                </div>
            )}
        </div>
    );
};

export default Dashboard;
