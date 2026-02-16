import { gql } from '@apollo/client';

// Order Mutations
export const UPDATE_ORDER_STATUS = gql`
  mutation UpdateOrderStatus($id: ID!, $status: String!) {
    updateOrderStatus(id: $id, status: $status) {
      id
      status
      orderDate
      totalAmount
    }
  }
`;

export const UPDATE_ORDER = gql`
  mutation UpdateOrder($id: ID!, $input: UpdateOrderInput!) {
    updateOrder(id: $id, input: $input) {
      id
      totalAmount
      status
      paymentStatus
      shippingAddress
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
  }
`;

export const DELETE_ORDER = gql`
  mutation DeleteOrder($id: ID!) {
    deleteOrder(id: $id)
  }
`;

export const CREATE_ORDER = gql`
  mutation CreateOrder($input: CreateOrderInput!) {
    createOrder(input: $input) {
      id
      userId
      totalAmount
      status
      paymentStatus
      orderDate
    }
  }
`;

// Product Mutations
export const CREATE_PRODUCT = gql`
  mutation CreateProduct($input: CreateProductInput!) {
    createProduct(input: $input) {
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
  }
`;

export const UPDATE_PRODUCT = gql`
  mutation UpdateProduct($id: ID!, $input: UpdateProductInput!) {
    updateProduct(id: $id, input: $input) {
      id
      productName
      description
      price
      category {
        id
        categoryName
      }
    }
  }
`;

export const DELETE_PRODUCT = gql`
  mutation DeleteProduct($id: ID!) {
    deleteProduct(id: $id)
  }
`;

// Category Mutations
export const CREATE_CATEGORY = gql`
  mutation CreateCategory($input: CreateCategoryInput!) {
    createCategory(input: $input) {
      id
      categoryName
      description
    }
  }
`;

export const UPDATE_CATEGORY = gql`
  mutation UpdateCategory($id: ID!, $input: UpdateCategoryInput!) {
    updateCategory(id: $id, input: $input) {
      id
      categoryName
      description
    }
  }
`;

export const DELETE_CATEGORY = gql`
  mutation DeleteCategory($id: ID!) {
    deleteCategory(id: $id)
  }
`;

// Cart Mutations
export const ADD_TO_CART = gql`
  mutation AddToCart($userId: ID!, $productId: ID!, $quantity: Int!) {
    addToCart(userId: $userId, productId: $productId, quantity: $quantity) {
      id
      quantity
      product {
        id
        productName
        price
      }
      addedAt
    }
  }
`;

export const REMOVE_FROM_CART = gql`
  mutation RemoveFromCart($cartItemId: ID!) {
    removeFromCart(cartItemId: $cartItemId)
  }
`;

// Inventory Mutations
export const UPDATE_STOCK = gql`
  mutation UpdateStock($productId: ID!, $quantity: Int!) {
    updateStock(productId: $productId, quantity: $quantity) {
      id
      stockQuantity
      product {
        id
        productName
      }
    }
  }
`;

export const RESERVE_STOCK = gql`
  mutation ReserveStock($productId: ID!, $quantity: Int!) {
    reserveStock(productId: $productId, quantity: $quantity) {
      id
      stockQuantity
      reservedQuantity
    }
  }
`;

export const RELEASE_STOCK = gql`
  mutation ReleaseStock($productId: ID!, $quantity: Int!) {
    releaseStock(productId: $productId, quantity: $quantity) {
      id
      stockQuantity
      reservedQuantity
    }
  }
`;