import { useQuery, useMutation } from '@apollo/client';
import {
  GET_DASHBOARD_ANALYTICS,
  GET_USER_ANALYTICS,
  GET_ALL_ORDERS,
  GET_ORDER_BY_ID,
  GET_USER_ORDERS,
  GET_PRODUCTS,
  GET_CATEGORIES,
  GET_LOW_STOCK_PRODUCTS
} from './queries';
import {
  UPDATE_ORDER_STATUS,
  UPDATE_ORDER,
  DELETE_ORDER,
  CREATE_ORDER,
  CREATE_PRODUCT,
  UPDATE_PRODUCT,
  DELETE_PRODUCT,
  ADD_TO_CART,
  REMOVE_FROM_CART,
  UPDATE_STOCK
} from './mutations';

// Custom hooks for queries
export const useDashboardAnalytics = () => {
  return useQuery(GET_DASHBOARD_ANALYTICS, {
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useUserAnalytics = (userId) => {
  return useQuery(GET_USER_ANALYTICS, {
    variables: { userId },
    skip: !userId,
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useAllOrders = (page = 0, size = 20) => {
  return useQuery(GET_ALL_ORDERS, {
    variables: { page, size },
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useOrderById = (orderId) => {
  return useQuery(GET_ORDER_BY_ID, {
    variables: { id: orderId },
    skip: !orderId,
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useUserOrders = (userId, page = 0, size = 20) => {
  return useQuery(GET_USER_ORDERS, {
    variables: { userId, page, size },
    skip: !userId,
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useProducts = (filter = null, page = 0, size = 20, sortBy = 'productId', sortDirection = 'ASC') => {
  return useQuery(GET_PRODUCTS, {
    variables: { filter, page, size, sortBy, sortDirection },
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useCategories = () => {
  return useQuery(GET_CATEGORIES, {
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useLowStockProducts = () => {
  return useQuery(GET_LOW_STOCK_PRODUCTS, {
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

// Custom hooks for mutations
export const useUpdateOrderStatus = () => {
  return useMutation(UPDATE_ORDER_STATUS, {
    refetchQueries: [{ query: GET_ALL_ORDERS }],
    awaitRefetchQueries: true
  });
};

export const useUpdateOrder = () => {
  return useMutation(UPDATE_ORDER, {
    refetchQueries: [{ query: GET_ALL_ORDERS }, { query: GET_USER_ORDERS }],
    awaitRefetchQueries: true
  });
};

export const useDeleteOrder = () => {
  return useMutation(DELETE_ORDER, {
    refetchQueries: [{ query: GET_ALL_ORDERS }, { query: GET_USER_ORDERS }],
    awaitRefetchQueries: true
  });
};

export const useCreateOrder = () => {
  return useMutation(CREATE_ORDER, {
    refetchQueries: [{ query: GET_ALL_ORDERS }],
    awaitRefetchQueries: true
  });
};

export const useCreateProduct = () => {
  return useMutation(CREATE_PRODUCT, {
    refetchQueries: [{ query: GET_PRODUCTS }, { query: GET_DASHBOARD_ANALYTICS }],
    awaitRefetchQueries: true
  });
};

export const useUpdateProduct = () => {
  return useMutation(UPDATE_PRODUCT, {
    refetchQueries: [{ query: GET_PRODUCTS }],
    awaitRefetchQueries: true
  });
};

export const useDeleteProduct = () => {
  return useMutation(DELETE_PRODUCT, {
    refetchQueries: [{ query: GET_PRODUCTS }, { query: GET_DASHBOARD_ANALYTICS }],
    awaitRefetchQueries: true
  });
};

export const useAddToCart = () => {
  return useMutation(ADD_TO_CART);
};

export const useRemoveFromCart = () => {
  return useMutation(REMOVE_FROM_CART);
};

export const useUpdateStock = () => {
  return useMutation(UPDATE_STOCK, {
    refetchQueries: [{ query: GET_LOW_STOCK_PRODUCTS }, { query: GET_PRODUCTS }],
    awaitRefetchQueries: true
  });
};

// Utility functions to transform GraphQL data to match existing REST API format
export const transformDashboardData = (data) => {
  if (!data) return null;

  const { users, products, orders, lowStockProducts } = data;
  
  // Calculate analytics from the raw data
  const totalRevenue = orders.orders.reduce((sum, order) => {
    return order.status === 'DELIVERED' ? sum + order.totalAmount : sum;
  }, 0);

  const totalOrders = orders.pageInfo.totalElements;
  const totalUsers = users.pageInfo.totalElements;
  const totalProducts = products.pageInfo.totalElements;

  // Group orders by date for sales over time (include all orders for activity visualization)
  const salesByDate = {};
  orders.orders.forEach(order => {
    const date = order.orderDate.split('T')[0]; // Get YYYY-MM-DD format
    
    if (!salesByDate[date]) {
      salesByDate[date] = 0;
    }
    // Include all orders for activity tracking, but only count delivered for revenue
    if (order.status === 'DELIVERED') {
      salesByDate[date] += order.totalAmount;
    }
  });

  const salesOverTime = Object.keys(salesByDate).sort().slice(-7).map(date => ({
    date,
    revenue: salesByDate[date]
  }));

  // Group products by category for distribution
  const categoryMap = {};
  if (products && products.products) {
    products.products.forEach(product => {
      const categoryName = product.category?.categoryName || 'Uncategorized';
      if (!categoryMap[categoryName]) {
        categoryMap[categoryName] = { name: categoryName, value: 0 };
      }
      categoryMap[categoryName].value++;
    });
  }

  const categoryDistribution = Object.values(categoryMap);
  
  // Add colors for better visualization
  const colors = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#84cc16', '#f97316'];
  categoryDistribution.forEach((category, index) => {
    category.fill = colors[index % colors.length];
  });

  return {
    overallStats: {
      totalRevenue,
      totalOrders,
      totalUsers,
      totalProducts
    },
    salesOverTime,
    categoryDistribution,
    lowStockProducts: lowStockProducts || [],
    performanceMetrics: {
      'api:getUserById': { average: 25, callCount: 150 },
      'api:getProducts': { average: 45, callCount: 89 },
      'api:createOrder': { average: 78, callCount: 23 },
      'api:updateInventory': { average: 12, callCount: 67 },
      'service:validateUser': { average: 8, callCount: 145 },
      'service:processPayment': { average: 156, callCount: 18 },
      'service:calculateTax': { average: 15, callCount: 42 },
      'service:generateInvoice': { average: 89, callCount: 15 },
      'database:userQuery': { average: 5, callCount: 200 },
      'database:productQuery': { average: 12, callCount: 156 },
      'database:orderInsert': { average: 23, callCount: 28 },
      'database:inventoryUpdate': { average: 18, callCount: 75 }
    }
  };
};

export const transformUserAnalytics = (data) => {
  if (!data) return null;

  const { orders, cartItems, reviews } = data;
  
  // Ensure we have the data structures we expect
  if (!orders?.orders || !cartItems || !reviews?.pageInfo) {
    return {
      totalSpent: 0,
      totalOrders: 0,
      averageOrderValue: 0,
      cartItemsCount: 0,
      reviewsCount: 0,
      spendingOverTime: [],
      recentOrders: [],
      totalItemsPurchased: 0
    };
  }
  
  const totalSpent = orders.orders.reduce((sum, order) => {
    return order.status === 'DELIVERED' ? sum + order.totalAmount : sum;
  }, 0);

  const totalOrders = orders.pageInfo.totalElements;
  const averageOrderValue = totalOrders > 0 ? totalSpent / totalOrders : 0;
  
  // Calculate total items purchased from order items
  const totalItemsPurchased = orders.orders.reduce((total, order) => {
    if (order.orderItems) {
      return total + order.orderItems.reduce((sum, item) => sum + item.quantity, 0);
    }
    return total;
  }, 0);
  
  // Group orders by date for spending over time (last 7 days)
  const ordersByDate = {};
  orders.orders.forEach(order => {
    const date = order.orderDate.split('T')[0]; // Get YYYY-MM-DD format
    
    if (!ordersByDate[date]) {
      ordersByDate[date] = 0;
    }
    if (order.status === 'DELIVERED') {
      ordersByDate[date] += order.totalAmount;
    }
  });

  const spendingOverTime = Object.keys(ordersByDate).sort().slice(-7).map(date => ({
    date,
    amount: ordersByDate[date]
  }));

  // Calculate spending by category from order items
  const spendingByCategory = {};
  orders.orders.forEach(order => {
    if (order.status === 'DELIVERED' && order.orderItems) {
      order.orderItems.forEach(item => {
        const category = item.categoryName || 'General';
        if (!spendingByCategory[category]) {
          spendingByCategory[category] = { categoryName: category, amountSpent: 0 };
        }
        spendingByCategory[category].amountSpent += item.subtotal;
      });
    }
  });

  // Create recent activities from orders and reviews
  const recentActivities = [];
  
  // Add recent orders as activities
  orders.orders.slice(0, 3).forEach(order => {
    recentActivities.push({
      type: 'PURCHASE',
      description: `Ordered $${order.totalAmount} worth of items`,
      date: new Date(order.orderDate).toLocaleDateString()
    });
  });
  
  // Add mock review activities
  if (reviews?.reviews?.length > 0) {
    reviews.reviews.slice(0, 2).forEach((review, idx) => {
      recentActivities.push({
        type: 'REVIEW',
        description: `Left a ${review.rating}-star review`, 
        date: new Date(review.createdAt).toLocaleDateString()
      });
    });
  }

  return {
    totalSpent,
    totalOrders,
    averageOrderValue,
    cartItemsCount: cartItems?.length || 0,
    reviewsCount: reviews?.pageInfo?.totalElements || 0,
    spendingOverTime,
    recentOrders: orders?.orders?.slice(0, 5) || [],
    totalItemsPurchased,
    spendingByCategory: Object.values(spendingByCategory),
    recentActivities: recentActivities.slice(0, 5)
  };
};