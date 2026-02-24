import { useQuery, useMutation } from '@apollo/client';
import {
  GET_DASHBOARD_ANALYTICS,
  GET_USER_ANALYTICS,
  GET_ALL_ORDERS,
  GET_USER_ORDERS
} from './queries';
import {
  UPDATE_ORDER_STATUS,
  UPDATE_ORDER,
  DELETE_ORDER,
  CANCEL_ORDER
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

export const useAllOrders = (filter = null, page = 0, size = 20, sortBy = 'orderDate', sortDirection = 'DESC') => {
  return useQuery(GET_ALL_ORDERS, {
    variables: { filter, page, size, sortBy, sortDirection },
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

export const useUserOrders = (userId, filter = null, page = 0, size = 20, sortBy = 'orderDate', sortDirection = 'DESC') => {
  return useQuery(GET_USER_ORDERS, {
    variables: { userId, filter, page, size, sortBy, sortDirection },
    skip: !userId,
    fetchPolicy: 'cache-and-network',
    errorPolicy: 'all'
  });
};

// Custom hooks for mutations
export const useUpdateOrderStatus = () => {
  return useMutation(UPDATE_ORDER_STATUS, {
    // Don't use refetchQueries here since it doesn't preserve filter/pagination params
    // The component will handle refetch with proper variables
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

export const useCancelOrder = () => {
  return useMutation(CANCEL_ORDER, {
    refetchQueries: [{ query: GET_ALL_ORDERS }, { query: GET_USER_ORDERS }],
    awaitRefetchQueries: true
  });
};

// Utility functions to transform GraphQL data to match existing REST API format
export const transformDashboardData = (data) => {
  if (!data) return null;

  const { users, products, orders, lowStockProducts } = data;
  
  // Calculate analytics from the raw data
  const totalRevenue = orders.orders.reduce((sum, order) => {
    return (order.status || '').toUpperCase() === 'DELIVERED' ? sum + order.totalAmount : sum;
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
    if ((order.status || '').toUpperCase() === 'DELIVERED') {
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
    performanceMetrics: {}
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
    return (order.status || '').toUpperCase() === 'DELIVERED' ? sum + order.totalAmount : sum;
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
    if ((order.status || '').toUpperCase() === 'DELIVERED') {
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
    if ((order.status || '').toUpperCase() === 'DELIVERED' && order.orderItems) {
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