import { gql } from '@apollo/client';

// Dashboard Queries
export const GET_DASHBOARD_ANALYTICS = gql`
  query GetDashboardAnalytics {
    users {
      users {
        id
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
        id
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
        id
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
          id
          productId
          productName
          categoryName
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
      id
      stockQuantity
      reorderLevel
      product {
        id
        productName
      }
    }
  }
`;

export const GET_USER_ANALYTICS = gql`
  query GetUserAnalytics($userId: ID) {
    orders(userId: $userId) {
      orders {
        id
        totalAmount
        status
        orderDate
        shippingAddress
        paymentMethod
        orderItems {
          id
          productId
          productName
          categoryName
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
      id
      quantity
      product {
        id
        productName
        price
      }
      addedAt
    }
    reviews(userId: $userId) {
      reviews {
        id
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
  query GetAllOrders($filter: OrderFilterInput, $page: Int, $size: Int, $sortBy: String, $sortDirection: String) {
    orders(filter: $filter, page: $page, size: $size, sortBy: $sortBy, sortDirection: $sortDirection) {
      orders {
        id
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
          id
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
      id
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
        id
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
  query GetUserOrders($userId: ID, $filter: OrderFilterInput, $page: Int, $size: Int, $sortBy: String, $sortDirection: String) {
    orders(userId: $userId, filter: $filter, page: $page, size: $size, sortBy: $sortBy, sortDirection: $sortDirection) {
      orders {
        id
        totalAmount
        status
        paymentStatus
        orderDate
        shippingAddress
        paymentMethod
        notes
        orderItems {
          id
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
        id
        productName
        description
        price
        category {
          id
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
      id
      categoryName
      description
      products {
        id
        productName
      }
    }
  }
`;

// Inventory Queries
export const GET_LOW_STOCK_PRODUCTS = gql`
  query GetLowStockProducts {
    lowStockProducts {
      id
      stockQuantity
      reorderLevel
      product {
        id
        productName
        price
      }
    }
  }
`;