import { useState, useEffect } from 'react';
import { getLowStockProducts, getOutOfStockProducts, getAllInventoryItems, updateStock, addStock } from '../../services/inventoryService';
import { AlertTriangle, Package, Plus, Search, Filter } from 'lucide-react';
import { showErrorAlert, formatErrorMessage } from '../../utils/errorHandler';

const InventoryManagement = () => {
    const [allItems, setAllItems] = useState([]);
    const [filteredItems, setFilteredItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('all');
    const [searchTerm, setSearchTerm] = useState('');
    const [stockFilter, setStockFilter] = useState('all');

    useEffect(() => {
        loadInventory();
    }, []);

    const loadInventory = async () => {
        try {
            setError(null);
            console.log('Loading inventory data...');
            const allInventory = await getAllInventoryItems();
            console.log('Inventory data loaded:', allInventory);
            setAllItems(allInventory.data || []);
            setFilteredItems(allInventory.data || []);
        } catch (error) {
            console.error('Error loading inventory:', error);
            setError(formatErrorMessage(error) || 'Failed to load inventory data');
        } finally {
            setLoading(false);
        }
    };

    const getStockStatus = (item) => {
        if (item.stockQuantity === 0) return 'out';
        if (item.stockQuantity <= item.reorderLevel) return 'low';
        return 'in';
    };

    const filterItems = () => {
        let filtered = allItems;

        // Filter by search term
        if (searchTerm) {
            filtered = filtered.filter(item =>
                item.productName.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Filter by stock status
        if (stockFilter !== 'all') {
            filtered = filtered.filter(item => getStockStatus(item) === stockFilter);
        }

        // Filter by active tab
        if (activeTab !== 'all') {
            filtered = filtered.filter(item => getStockStatus(item) === activeTab);
        }

        setFilteredItems(filtered);
    };

    useEffect(() => {
        filterItems();
    }, [allItems, searchTerm, stockFilter, activeTab]);

    const handleAddStock = async (productId) => {
        const quantity = prompt('Enter quantity to add:');
        if (quantity && !isNaN(quantity)) {
            try {
                await addStock(productId, parseInt(quantity));
                alert(`Successfully added ${quantity} units to inventory!`);
                loadInventory();
            } catch (error) {
                console.error('Error adding stock:', error);
                showErrorAlert(error, 'Failed to add stock');
            }
        }
    };

    const getTabCounts = () => {
        const all = allItems.length;
        const inStock = allItems.filter(item => getStockStatus(item) === 'in').length;
        const lowStock = allItems.filter(item => getStockStatus(item) === 'low').length;
        const outOfStock = allItems.filter(item => getStockStatus(item) === 'out').length;
        return { all, inStock, lowStock, outOfStock };
    };

    const tabCounts = getTabCounts();

    return (
        <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-6">Inventory Management</h1>

            {/* Error Message */}
            {error && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
                    <div className="flex">
                        <AlertTriangle className="h-5 w-5 text-red-400" />
                        <div className="ml-3">
                            <h3 className="text-sm font-medium text-red-800">Error Loading Inventory</h3>
                            <p className="text-sm text-red-700 mt-2">{error}</p>
                            <button 
                                onClick={loadInventory} 
                                className="mt-2 text-sm text-red-800 underline hover:text-red-600"
                            >
                                Try Again
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Search and Filter Section */}
            <div className="bg-white p-4 rounded-lg shadow mb-6">
                <div className="flex flex-col sm:flex-row gap-4">
                    {/* Search */}
                    <div className="flex-1">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                            <input
                                type="text"
                                placeholder="Search products..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                            />
                        </div>
                    </div>
                    {/* Stock Filter */}
                    <div className="flex items-center gap-2">
                        <Filter className="text-gray-400 w-4 h-4" />
                        <select
                            value={stockFilter}
                            onChange={(e) => setStockFilter(e.target.value)}
                            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        >
                            <option value="all">All Stock Status</option>
                            <option value="in">In Stock</option>
                            <option value="low">Low Stock</option>
                            <option value="out">Out of Stock</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Tabs */}
            <div className="flex gap-4 mb-6 overflow-x-auto">
                <button onClick={() => setActiveTab('all')} className={`px-6 py-3 rounded-lg font-medium whitespace-nowrap ${activeTab === 'all' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-600'}`}>
                    All Items ({tabCounts.all})
                </button>
                <button onClick={() => setActiveTab('in')} className={`px-6 py-3 rounded-lg font-medium whitespace-nowrap ${activeTab === 'in' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'}`}>
                    In Stock ({tabCounts.inStock})
                </button>
                <button onClick={() => setActiveTab('low')} className={`px-6 py-3 rounded-lg font-medium whitespace-nowrap ${activeTab === 'low' ? 'bg-yellow-100 text-yellow-800' : 'bg-gray-100 text-gray-600'}`}>
                    Low Stock ({tabCounts.lowStock})
                </button>
                <button onClick={() => setActiveTab('out')} className={`px-6 py-3 rounded-lg font-medium whitespace-nowrap ${activeTab === 'out' ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-600'}`}>
                    Out of Stock ({tabCounts.outOfStock})
                </button>
            </div>

            <div className="card overflow-hidden">
                <table className="w-full">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Current Stock</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Reorder Level</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Reserved</th>
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {loading ? (
                            <tr><td colSpan="6" className="px-6 py-12 text-center"><div className="w-8 h-8 border-4 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto"></div></td></tr>
                        ) : filteredItems.length === 0 ? (
                            <tr><td colSpan="6" className="px-6 py-12 text-center text-gray-500">No items found</td></tr>
                        ) : (
                            filteredItems.map((item) => {
                                const status = getStockStatus(item);
                                return (
                                    <tr key={item.productId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center">
                                                <Package className="w-5 h-5 text-gray-400 mr-3" />
                                                <div>
                                                    <span className="text-sm font-medium text-gray-900 block">{item.productName}</span>
                                                    <span className="text-xs text-gray-500">ID: {item.productId}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                                status === 'out' ? 'bg-red-100 text-red-800' :
                                                status === 'low' ? 'bg-yellow-100 text-yellow-800' :
                                                'bg-green-100 text-green-800'
                                            }`}>
                                                {status === 'out' ? 'Out of Stock' : status === 'low' ? 'Low Stock' : 'In Stock'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`text-sm font-medium ${
                                                status === 'out' ? 'text-red-600' :
                                                status === 'low' ? 'text-yellow-600' :
                                                'text-green-600'
                                            }`}>
                                                {item.stockQuantity}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-gray-900">{item.reorderLevel}</td>
                                        <td className="px-6 py-4 text-sm text-gray-900">{item.reservedQuantity || 0}</td>
                                        <td className="px-6 py-4 text-right">
                                            <button onClick={() => handleAddStock(item.productId)} className="btn-primary text-xs px-3 py-1 flex items-center gap-1 ml-auto">
                                                <Plus className="w-3 h-3" /> Add Stock
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default InventoryManagement;
