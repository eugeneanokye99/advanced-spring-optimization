import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { ShoppingBag, ShoppingCart, Package, LogOut, Menu, X, Home, MapPin, Lock } from 'lucide-react';
import ProductBrowse from '../../components/customer/ProductBrowse';
import Cart from '../../components/customer/Cart';
import OrderHistory from '../../components/customer/OrderHistory';
import CustomerDashboardHome from '../../components/customer/CustomerDashboardHome';
import AddressManagement from '../../components/customer/AddressManagement';
import ChangePassword from '../../components/customer/ChangePassword';

const CustomerDashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('home');
    const [sidebarOpen, setSidebarOpen] = useState(true);

    useEffect(() => {
        const role = (user?.userType || '').toUpperCase();
        if (role !== 'CUSTOMER') {
            navigate('/login');
        }
    }, [user, navigate]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const menuItems = [
        { id: 'home', label: 'Home', icon: Home },
        { id: 'products', label: 'Browse Products', icon: ShoppingBag },
        { id: 'cart', label: 'My Cart', icon: ShoppingCart },
        { id: 'orders', label: 'Order History', icon: Package },
        { id: 'addresses', label: 'My Addresses', icon: MapPin },
        { id: 'password', label: 'Change Password', icon: Lock },
    ];

    const renderContent = () => {
        switch (activeTab) {
            case 'home':
                return <CustomerDashboardHome />;
            case 'products':
                return <ProductBrowse />;
            case 'cart':
                return <Cart />;
            case 'orders':
                return <OrderHistory />;
            case 'addresses':
                return <AddressManagement />;
            case 'password':
                return <ChangePassword />;
            default:
                return <CustomerDashboardHome />;
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex">
            {/* Sidebar */}
            <aside className={`${sidebarOpen ? 'w-64' : 'w-20'} bg-white border-r border-gray-200 transition-all duration-300 flex flex-col`}>
                <div className="h-16 flex items-center justify-between px-4 border-b border-gray-200">
                    {sidebarOpen && <h1 className="text-xl font-bold text-primary-600">ShopJoy</h1>}
                    <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2 hover:bg-gray-100 rounded-lg">
                        {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
                    </button>
                </div>

                <nav className="flex-1 p-4 space-y-2">
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        return (
                            <button key={item.id} onClick={() => setActiveTab(item.id)} className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${activeTab === item.id ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-700 hover:bg-gray-100'}`}>
                                <Icon className="w-5 h-5 flex-shrink-0" />
                                {sidebarOpen && <span>{item.label}</span>}
                            </button>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-gray-200">
                    {sidebarOpen && (
                        <div className="mb-3 px-2">
                            <p className="text-sm font-medium text-gray-900">{user?.username}</p>
                            <p className="text-xs text-gray-500">{user?.email}</p>
                        </div>
                    )}
                    <button onClick={handleLogout} className="w-full flex items-center gap-3 px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                        <LogOut className="w-5 h-5 flex-shrink-0" />
                        {sidebarOpen && <span>Logout</span>}
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-auto">
                <div className="p-8">{renderContent()}</div>
            </main>
        </div>
    );
};

export default CustomerDashboard;
