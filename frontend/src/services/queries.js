import { gql } from '@apollo/client';

// Dashboard Queries
export const GET_DASHBOARD_ANALYTICS = gql`
  query GetDashboardAnalytics {
    users {
      users {
        userId
        username
        userType
        createdAt
      }
      pageInfo {
        totalElements
      }
    }
    products {
      products {
        productId
        productName
        price
        category {
          categoryName
        }
        createdAt
      }
      pageInfo {
        totalElements
      }
    }
    orders {
      orders {
        orderId
        totalAmount
        status
        paymentStatus
        orderDate
        shippingAddress
        paymentMethod
        notes
        user {
          firstName
          lastName
        }
        orderItems {
          orderItemId
          productId
          productName
          quantity
          unitPrice
          subtotal
        }
      }
      pageInfo {
        totalElements
      }
    }
    lowStockProducts {
      inventoryId
      stockQuantity
      reorderLevel
      product {
        productId
        productName
      }
    }
  }
`;

export const GET_USER_ANALYTICS = gql`
  query GetUserAnalytics($userId: ID) {
    orders(userId: $userId) {
      orders {
        orderId
        totalAmount
        status
        orderDate
        shippingAddress
        paymentMethod
        orderItems {
          orderItemId
          productId
          productName
          quantity
          unitPrice
          subtotal
        }
      }
      pageInfo {
        totalElements
      }
    }
    cartItems(userId: $userId) {
      cartItemId
      quantity
      product {
        productId
        productName
        price
      }
      addedAt
    }
    reviews(userId: $userId) {
      reviews {
        reviewId
        rating
        comment
        createdAt
        product {
          productName
        }
      }
      pageInfo {
        totalElements
      }
    }
  }
`;

// Order Queries
export const GET_ALL_ORDERS = gql`
  query GetAllOrders($page: Int, $size: Int) {
    orders(page: $page, size: $size) {
      orders {
        orderId
        userId
        totalAmount
        status
        paymentStatus
        orderDate
        shippingAddress
        paymentMethod
        notes
        user {
          firstName
          lastName
          email
        }
        orderItems {
          orderItemId
          productId
          productName
          quantity
          unitPrice
          subtotal
        }
      }
      pageInfo {
        page
        size
        totalElements
        totalPages
      }
    }
  }
`;

export const GET_ORDER_BY_ID = gql`
  query GetOrderById($id: ID!) {
    order(id: $id) {
      orderId
      userId
      totalAmount
      status
      paymentStatus
      orderDate
      shippingAddress
      paymentMethod
      notes
      user {
        firstName
        lastName
        email
        phone
      }
      orderItems {
        orderItemId
        productId
        productName
        quantity
        unitPrice
        subtotal
      }
    }
  }
`;

export const GET_USER_ORDERS = gql`
  query GetUserOrders($userId: ID, $page: Int, $size: Int) {
    orders(userId: $userId, page: $page, size: $size) {
      orders {
        orderId
        totalAmount
        status
        paymentStatus
        orderDate
        shippingAddress
        paymentMethod
        notes
        orderItems {
          orderItemId
          productId
          productName
          quantity
          unitPrice
          subtotal
        }
      }
      pageInfo {
        page
        size
        totalElements
        totalPages
      }
    }
  }
`;

// Product Queries
export const GET_PRODUCTS = gql`
  query GetProducts(
    $filter: ProductFilterInput
    $page: Int
    $size: Int
    $sortBy: String
    $sortDirection: String
  ) {
    products(
      filter: $filter
      page: $page
      size: $size
      sortBy: $sortBy
      sortDirection: $sortDirection
    ) {
      products {
        productId
        productName
        description
        price
        category {
          categoryId
          categoryName
        }
        createdAt
      }
      pageInfo {
        page
        size
        totalElements
        totalPages
      }
    }
  }
`;

export const GET_CATEGORIES = gql`
  query GetCategories {
    categories {
      categoryId
      categoryName
      description
      products {
        productId
        productName
      }
    }
  }
`;

// Inventory Queries
export const GET_LOW_STOCK_PRODUCTS = gql`
  query GetLowStockProducts {
    lowStockProducts {
      inventoryId
      stockQuantity
      reorderLevel
      product {
        productId
        productName
        price
      }
    }
  }
`;