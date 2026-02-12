import React, { useEffect, useState } from 'react';
import { 
    Zap, 
    Database, 
    Gauge, 
    Clock, 
    ArrowDownRight, 
    CheckCircle2,
    ShieldCheck,
    Cpu,
    RefreshCw
} from 'lucide-react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    LineChart, Line, Legend, AreaChart, Area, PieChart, Pie, Cell
} from 'recharts';
import { getPerformanceMetrics, getCacheStats } from '../../services/analyticsService';
import toast from 'react-hot-toast';

const OptimizationMetrics = () => {
    const [performanceData, setPerformanceData] = useState([]);
    const [cacheData, setCacheData] = useState(null);
    const [loading, setLoading] = useState(true);

    const fetchData = async () => {
        setLoading(true);
        try {
            const [metricsRes, cacheRes] = await Promise.all([
                getPerformanceMetrics(),
                getCacheStats()
            ]);
            
            // Format metrics for charts
            // metricsRes is the ApiResponse object because of the axios interceptor
            const metrics = metricsRes.data;
            const chartData = Object.entries(metrics)
                .map(([key, stats]) => {
                    // key format: "category:ClassName.methodName" or "database:$Proxy.method"
                    const [category, fullPath] = key.split(':');
                    const parts = fullPath.split('.');
                    const methodName = parts.pop();
                    const rawClassName = parts.pop() || category;
                    const className = rawClassName.startsWith('$') ? category : rawClassName;
                    
                    return {
                        method: `${className}.${methodName}`,
                        avg: stats.average,
                        p95: stats.p95,
                        calls: stats.callCount
                    };
                })
                .sort((a, b) => b.avg - a.avg)
                .slice(0, 8); // Show top 8 slowest
            
            setPerformanceData(chartData);
            setCacheData(cacheRes.data);
        } catch (error) {
            console.error('Error fetching performance data:', error);
            toast.error('Failed to load real-time performance metrics');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const COLORS = ['#10b981', '#f59e0b', '#ef4444', '#3b82f6'];

    const cachePieData = cacheData ? [
        { name: 'Hits', value: parseInt(cacheData.totalHits) },
        { name: 'Misses', value: parseInt(cacheData.totalMisses) }
    ] : [];

    const optimizationFeatures = [
        {
            title: 'N+1 Query Resolution',
            description: 'Implemented Hibernate @BatchSize and GraphQL @BatchMapping to reduce SQL round-trips.',
            status: 'Active',
            impact: '85% Fewer Queries',
            icon: Database,
            color: 'text-blue-600',
            bgColor: 'bg-blue-50'
        },
        {
            title: 'Application Caching',
            description: 'Spring Cache integrated with automated eviction logic for frequently accessed resources.',
            status: 'Active',
            impact: '98% Latency Reduction',
            icon: Zap,
            color: 'text-amber-600',
            bgColor: 'bg-amber-50'
        },
        {
            title: 'Database Indexing',
            description: 'Strategic indexes on high-traffic columns validated through JPA specification analysis.',
            status: 'Optimized',
            impact: 'O(log n) Search',
            icon: Gauge,
            color: 'text-emerald-600',
            bgColor: 'bg-emerald-50'
        },
        {
            title: 'AOP Monitoring',
            description: 'Centralized transaction and execution tracking for performance auditing.',
            status: 'Active',
            impact: 'Real-time Metrics',
            icon: Cpu,
            color: 'text-purple-600',
            bgColor: 'bg-purple-50'
        }
    ];

    return (
        <div className="space-y-8 pb-12">
            <div>
                <div className="flex justify-between items-end">
                    <div>
                        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">System Reliability & Performance</h1>
                        <p className="text-gray-500 mt-2">Real-time metrics from Backend AOP Collectors and Caching Layer.</p>
                    </div>
                    <button 
                        onClick={fetchData}
                        disabled={loading}
                        className="flex items-center space-x-2 bg-white border border-gray-200 px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors shadow-sm disabled:opacity-50"
                    >
                        <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                        <span>Refresh Data</span>
                    </button>
                </div>
            </div>

            {/* Feature Status Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {optimizationFeatures.map((feature, index) => (
                    <div key={index} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                        <div className="flex items-center justify-between mb-4">
                            <div className={`${feature.bgColor} p-3 rounded-2xl`}>
                                <feature.icon className={`w-6 h-6 ${feature.color}`} />
                            </div>
                            <span className="px-3 py-1 bg-green-100 text-green-700 text-xs font-bold rounded-full uppercase">
                                {feature.status}
                            </span>
                        </div>
                        <h3 className="text-lg font-bold text-gray-900">{feature.title}</h3>
                        <p className="text-sm text-gray-500 mt-1 leading-relaxed">{feature.description}</p>
                        <div className="mt-4 pt-4 border-t border-gray-50 flex items-center text-primary-600 font-bold">
                            <CheckCircle2 className="w-4 h-4 mr-2" />
                            {feature.title === 'Application Caching' && cacheData ? `${cacheData.hitRate} Hit Rate` : feature.impact}
                        </div>
                    </div>
                ))}
            </div>

            {/* Performance Charts */}
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                {/* Method Latency Chart */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h2 className="text-xl font-bold text-gray-900">Top Service Latency</h2>
                            <p className="text-sm text-gray-500">Average vs P95 execution time (milliseconds)</p>
                        </div>
                        <Clock className="w-6 h-6 text-gray-400" />
                    </div>
                    <div className="h-80 w-full">
                        {loading ? (
                            <div className="h-full flex items-center justify-center text-gray-400 font-medium">
                                <RefreshCw className="w-5 h-5 animate-spin mr-2" /> 
                                Analyzing metrics...
                            </div>
                        ) : performanceData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart 
                                    data={performanceData} 
                                    layout="vertical"
                                    margin={{ left: 40, right: 30 }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f3f4f6" />
                                    <XAxis type="number" stroke="#9ca3af" fontSize={11} tickFormatter={(val) => `${val}ms`} />
                                    <YAxis 
                                        dataKey="method" 
                                        type="category" 
                                        stroke="#4b5563" 
                                        fontSize={10} 
                                        width={160}
                                        tick={{fontWeight: 500}}
                                    />
                                    <Tooltip 
                                        cursor={{fill: '#f9fafb'}}
                                        contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                                    />
                                    <Legend wrapperStyle={{paddingTop: '20px'}} />
                                    <Bar dataKey="avg" name="Avg Latency" fill="#3b82f6" radius={[0, 4, 4, 0]} barSize={20} />
                                    <Bar dataKey="p95" name="P95 Latency" fill="#fbbf24" radius={[0, 4, 4, 0]} barSize={20} />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <div className="h-full flex flex-col items-center justify-center text-gray-400">
                                <Gauge className="w-12 h-12 mb-2 opacity-20" />
                                <p>No performance data recorded yet</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* Cache Statistics Chart */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h2 className="text-xl font-bold text-gray-900">Cache Efficiency</h2>
                            <p className="text-sm text-gray-500">Distribution of cache hits and misses</p>
                        </div>
                        <Zap className="w-6 h-6 text-gray-400" />
                    </div>
                    <div className="h-80 w-full flex items-center">
                        {loading ? (
                            <div className="h-full w-full flex items-center justify-center text-gray-400 font-medium">
                                <RefreshCw className="w-5 h-5 animate-spin mr-2" />
                                Evaluating cache...
                            </div>
                        ) : cacheData && (cacheData.totalRequests > 0) ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 w-full h-full items-center">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={cachePieData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={100}
                                            paddingAngle={5}
                                            dataKey="value"
                                        >
                                            {cachePieData.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip />
                                    </PieChart>
                                </ResponsiveContainer>
                                <div className="space-y-4 px-4">
                                    <div className="p-4 bg-emerald-50 rounded-2xl">
                                        <p className="text-sm text-emerald-600 font-bold uppercase tracking-wider text-[10px]">Total Hits</p>
                                        <p className="text-2xl font-black text-emerald-700">{cacheData?.totalHits || 0}</p>
                                    </div>
                                    <div className="p-4 bg-amber-50 rounded-2xl">
                                        <p className="text-sm text-amber-600 font-bold uppercase tracking-wider text-[10px]">Total Misses</p>
                                        <p className="text-2xl font-black text-amber-700">{cacheData?.totalMisses || 0}</p>
                                    </div>
                                    <div className="p-4 bg-blue-50 rounded-2xl">
                                        <p className="text-sm text-blue-600 font-bold uppercase tracking-wider text-[10px]">Efficiency</p>
                                        <p className="text-2xl font-black text-blue-700">{cacheData?.hitRate || '0%'}</p>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="h-full w-full flex flex-col items-center justify-center text-gray-400">
                                <Zap className="w-12 h-12 mb-2 opacity-20" />
                                <p>No cache activity monitored yet</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>

        </div>
    );
};

export default OptimizationMetrics;
