# GraphQL Queries and Mutations - Testing Guide

## QUERIES

### User Queries

#### Get Single User
```graphql
query GetUser {
  user(id: 1) {
    userId
    username
    email
    firstName
    lastName
    phone
    userType
    createdAt
  }
}
```

#### Get All Users with Pagination
```graphql
query GetUsers {
  users(page: 0, size: 10) {
    users {
      userId
      username
      email
      firstName
      lastName
    }
    pageInfo {
      page
      size
      totalElements
      totalPages
    }
  }
}
```

### Product Queries

#### Get Single Product with Category
```graphql
query GetProduct {
  product(id: 1) {
    id
    name
    description
    price
    stockQuantity
    category {
      id
      name
      description
    }
    createdAt
    updatedAt
  }
}
```

#### Get Products with Filtering and Pagination
```graphql
query GetProducts {
  products(
    filter: {
      minPrice: 10.0
      maxPrice: 1000.0
      categoryId: 1
      inStock: true
      searchTerm: "laptop"
    }
    page: 0
    size: 20
    sortBy: "price"
    sortDirection: "ASC"
  ) {
    products {
      id
      name
      price
      stockQuantity
      category {
        name
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
```

#### Get Products by Category
```graphql
query GetProductsByCategory {
  products(filter: { categoryId: 1 }, page: 0, size: 10) {
    products {
      id
      name
      price
    }
    pageInfo {
      totalElements
    }
  }
}
```

### Category Queries

#### Get Single Category
```graphql
query GetCategory {
  category(id: 1) {
    id
    name
    description
    products {
      id
      name
      price
    }
  }
}
```

#### Get All Categories
```graphql
query GetCategories {
  categories {
    id
    name
    description
  }
}
```

### Order Queries

#### Get Single Order
```graphql
query GetOrder {
  order(id: 1) {
    id
    totalAmount
    status
    paymentStatus
    orderDate
    user {
      id
      username
      email
    }
    orderItems {
      id
      quantity
      price
      product {
        id
        name
      }
    }
  }
}
```

#### Get Orders by User with Pagination
```graphql
query GetOrdersByUser {
  orders(userId: 1, page: 0, size: 10) {
    orders {
      id
      totalAmount
      status
      orderDate
    }
    pageInfo {
      totalElements
      totalPages
    }
  }
}
```

#### Get All Orders
```graphql
query GetAllOrders {
  orders(page: 0, size: 20) {
    orders {
      id
      totalAmount
      status
      user {
        username
      }
    }
    pageInfo {
      page
      size
      totalElements
    }
  }
}
```

### Review Queries

#### Get Review
```graphql
query GetReview {
  review(id: 1) {
    id
    rating
    comment
    helpfulCount
    createdAt
    product {
      name
    }
    user {
      username
    }
  }
}
```

#### Get Reviews by Product
```graphql
query GetReviewsByProduct {
  reviews(productId: 1, page: 0, size: 10) {
    reviews {
      id
      rating
      comment
      createdAt
      user {
        username
      }
    }
    pageInfo {
      totalElements
    }
  }
}
```

#### Get Reviews by User
```graphql
query GetReviewsByUser {
  reviews(userId: 1, page: 0, size: 10) {
    reviews {
      id
      rating
      comment
      product {
        name
      }
    }
    pageInfo {
      totalElements
    }
  }
}
```

### Cart Queries

#### Get Cart Items
```graphql
query GetCartItems {
  cartItems(userId: 1) {
    id
    quantity
    addedAt
    product {
      id
      name
      price
    }
  }
}
```

### Address Queries

#### Get Address
```graphql
query GetAddress {
  address(id: 1) {
    id
    addressType
    street
    city
    state
    postalCode
    country
    isDefault
  }
}
```

#### Get User Addresses
```graphql
query GetUserAddresses {
  addresses(userId: 1) {
    id
    addressType
    street
    city
    state
    isDefault
  }
}
```

### Inventory Queries

#### Get Product Inventory
```graphql
query GetInventory {
  inventory(productId: 1) {
    id
    quantity
    reservedQuantity
    reorderLevel
    lastRestocked
    product {
      name
    }
  }
}
```

#### Get Low Stock Products
```graphql
query GetLowStockProducts {
  lowStockProducts {
    id
    quantity
    reorderLevel
    product {
      name
      price
    }
  }
}
```

## MUTATIONS

### User Mutations

#### Create User
```graphql
mutation CreateUser {
  createUser(input: {
    username: "johndoe"
    email: "john.doe@example.com"
    password: "SecurePass123!"
    firstName: "John"
    lastName: "Doe"
    phone: "+1234567890"
    userType: "CUSTOMER"
  }) {
    id
    username
    email
    firstName
    lastName
    createdAt
  }
}
```

#### Update User
```graphql
mutation UpdateUser {
  updateUser(id: 1, input: {
    firstName: "Jane"
    lastName: "Smith"
    phone: "+0987654321"
  }) {
    id
    username
    firstName
    lastName
    phone
  }
}
```

#### Delete User
```graphql
mutation DeleteUser {
  deleteUser(id: 1)
}
```

### Product Mutations

#### Create Product
```graphql
mutation CreateProduct {
  createProduct(input: {
    name: "Gaming Laptop"
    description: "High-performance gaming laptop with RTX 4080"
    price: 1499.99
    stockQuantity: 50
    categoryId: 1
  }) {
    id
    name
    description
    price
    stockQuantity
    category {
      name
    }
    createdAt
  }
}
```

#### Update Product
```graphql
mutation UpdateProduct {
  updateProduct(id: 1, input: {
    name: "Updated Gaming Laptop"
    price: 1399.99
    stockQuantity: 45
  }) {
    id
    name
    price
    stockQuantity
    updatedAt
  }
}
```

#### Delete Product
```graphql
mutation DeleteProduct {
  deleteProduct(id: 1)
}
```

### Category Mutations

#### Create Category
```graphql
mutation CreateCategory {
  createCategory(input: {
    name: "Electronics"
    description: "Electronic devices and accessories"
  }) {
    id
    name
    description
    createdAt
  }
}
```

#### Create Category
```graphql
mutation CreateCategory {
  createCategory(input: {
    name: "Laptops"
    description: "Laptop computers"
  }) {
    id
    name
    description
  }
}
```

#### Update Category
```graphql
mutation UpdateCategory {
  updateCategory(id: 1, input: {
    name: "Consumer Electronics"
    description: "All types of consumer electronic devices"
  }) {
    id
    name
    description
    updatedAt
  }
}
```

#### Delete Category
```graphql
mutation DeleteCategory {
  deleteCategory(id: 1)
}
```

### Order Mutations

#### Create Order
```graphql
mutation CreateOrder {
  createOrder(input: {
    userId: 1
    orderItems: [
      {
        productId: 1
        quantity: 2
        price: 1499.99
      },
      {
        productId: 2
        quantity: 1
        price: 299.99
      }
    ]
  }) {
    id
    totalAmount
    status
    paymentStatus
    orderDate
    orderItems {
      id
      quantity
      price
      product {
        name
      }
    }
  }
}
```

#### Update Order Status
```graphql
mutation UpdateOrderStatus {
  updateOrderStatus(id: 1, status: "CONFIRMED") {
    id
    status
    updatedAt
  }
}
```

#### Ship Order
```graphql
mutation ShipOrder {
  updateOrderStatus(id: 1, status: "SHIPPED") {
    id
    status
    updatedAt
  }
}
```

#### Complete Order
```graphql
mutation CompleteOrder {
  updateOrderStatus(id: 1, status: "COMPLETED") {
    id
    status
    updatedAt
  }
}
```

#### Cancel Order
```graphql
mutation CancelOrder {
  updateOrderStatus(id: 1, status: "CANCELLED") {
    id
    status
    updatedAt
  }
}
```

### Review Mutations

#### Create Review
```graphql
mutation CreateReview {
  createReview(input: {
    productId: 1
    userId: 1
    rating: 5
    comment: "Excellent product! Highly recommended."
  }) {
    id
    rating
    comment
    createdAt
    product {
      name
    }
    user {
      username
    }
  }
}
```

#### Update Review
```graphql
mutation UpdateReview {
  updateReview(id: 1, input: {
    rating: 4
    comment: "Good product, but could be better."
  }) {
    id
    rating
    comment
    updatedAt
  }
}
```

#### Delete Review
```graphql
mutation DeleteReview {
  deleteReview(id: 1)
}
```

### Cart Mutations

#### Add to Cart
```graphql
mutation AddToCart {
  addToCart(userId: 1, productId: 1, quantity: 2) {
    id
    quantity
    addedAt
    product {
      name
      price
    }
  }
}
```

#### Remove from Cart
```graphql
mutation RemoveFromCart {
  removeFromCart(cartItemId: 1)
}
```

### Address Mutations

#### Create Address
```graphql
mutation CreateAddress {
  createAddress(input: {
    userId: 1
    addressType: "SHIPPING"
    street: "123 Main Street"
    city: "New York"
    state: "NY"
    postalCode: "10001"
    country: "USA"
    isDefault: true
  }) {
    id
    addressType
    street
    city
    state
    postalCode
    country
    isDefault
    createdAt
  }
}
```

#### Update Address
```graphql
mutation UpdateAddress {
  updateAddress(id: 1, input: {
    street: "456 Oak Avenue"
    city: "Los Angeles"
    state: "CA"
    postalCode: "90001"
  }) {
    id
    street
    city
    state
    postalCode
    updatedAt
  }
}
```

#### Set Default Address
```graphql
mutation SetDefaultAddress {
  setDefaultAddress(id: 1) {
    id
    isDefault
  }
}
```

#### Delete Address
```graphql
mutation DeleteAddress {
  deleteAddress(id: 1)
}
```

### Inventory Mutations

#### Update Stock
```graphql
mutation UpdateStock {
  updateStock(productId: 1, quantity: 100) {
    id
    quantity
    reservedQuantity
    product {
      name
    }
  }
}
```

#### Reserve Stock
```graphql
mutation ReserveStock {
  reserveStock(productId: 1, quantity: 5) {
    id
    quantity
    reservedQuantity
  }
}
```

#### Release Stock
```graphql
mutation ReleaseStock {
  releaseStock(productId: 1, quantity: 5) {
    id
    quantity
    reservedQuantity
  }
}
```

## COMPLEX QUERIES

### Get Product with Full Details
```graphql
query GetProductFullDetails {
  product(id: 1) {
    id
    name
    description
    price
    stockQuantity
    category {
      id
      name
      description
    }
    createdAt
    updatedAt
  }
}
```

### Get Order with Complete Information
```graphql
query GetOrderComplete {
  order(id: 1) {
    id
    totalAmount
    status
    paymentStatus
    orderDate
    user {
      id
      username
      email
      firstName
      lastName
    }
    orderItems {
      id
      quantity
      price
      product {
        id
        name
        description
        price
        category {
          name
        }
      }
    }
  }
}
```

### Get Category with Products and Reviews
```graphql
query GetCategoryWithProducts {
  category(id: 1) {
    id
    name
    description
    products {
      id
      name
      price
      stockQuantity
    }
  }
}
```

## TESTING WORKFLOW

### 1. Setup Test Data
```graphql
mutation Setup {
  category: createCategory(input: {
    name: "Electronics"
    description: "Electronic devices"
  }) {
    id
  }
  
  user: createUser(input: {
    username: "testuser"
    email: "test@example.com"
    password: "Password123!"
    userType: "CUSTOMER"
  }) {
    id
  }
}
```

### 2. Create Product
```graphql
mutation CreateTestProduct {
  createProduct(input: {
    name: "Test Laptop"
    price: 999.99
    stockQuantity: 10
    categoryId: 1
  }) {
    id
    name
    price
  }
}
```

### 3. Add to Cart
```graphql
mutation AddProductToCart {
  addToCart(userId: 1, productId: 1, quantity: 2) {
    id
    quantity
  }
}
```

### 4. Create Order
```graphql
mutation CreateTestOrder {
  createOrder(input: {
    userId: 1
    orderItems: [{
      productId: 1
      quantity: 2
      price: 999.99
    }]
  }) {
    id
    totalAmount
    status
  }
}
```

### 5. Add Review
```graphql
mutation AddProductReview {
  createReview(input: {
    productId: 1
    userId: 1
    rating: 5
    comment: "Great product!"
  }) {
    id
    rating
  }
}
```

## GRAPHIQL ACCESS

Once the application is running, access GraphiQL at:
```
http://localhost:8080/graphiql
```

GraphQL endpoint:
```
http://localhost:8080/graphql
```
