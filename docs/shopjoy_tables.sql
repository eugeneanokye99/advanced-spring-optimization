-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    user_type VARCHAR(20) DEFAULT 'customer' CHECK (user_type IN ('customer', 'admin')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- CATEGORIES TABLE
-- ============================================
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_category_id INTEGER REFERENCES categories(category_id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- PRODUCTS TABLE
-- ============================================
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INTEGER NOT NULL REFERENCES categories(category_id) ON DELETE CASCADE,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    cost_price DECIMAL(10, 2) CHECK (cost_price >= 0),
    sku VARCHAR(50) UNIQUE NOT NULL,
    brand VARCHAR(100),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INVENTORY TABLE
-- ============================================
CREATE TABLE inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INTEGER UNIQUE NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    quantity_in_stock INTEGER DEFAULT 0 CHECK (quantity_in_stock >= 0),
    reorder_level INTEGER DEFAULT 10,
    warehouse_location VARCHAR(100),
    last_restocked TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ORDERS TABLE
-- ============================================
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')),
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'unpaid' CHECK (payment_status IN ('unpaid', 'paid', 'refunded')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ORDER_ITEMS TABLE
-- ============================================
CREATE TABLE order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- REVIEWS TABLE
-- ============================================
CREATE TABLE reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, user_id)
);

-- ============================================
-- ADDRESSES TABLE (for multiple shipping addresses)
-- ============================================
CREATE TABLE addresses (
    address_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    address_type VARCHAR(20) CHECK (address_type IN ('shipping', 'billing', 'home', 'work', 'other')),
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ============================================
-- USERS TABLE INDEXES
-- ============================================
-- username and email already have unique indexes from UNIQUE constraint
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ============================================
-- CATEGORIES TABLE INDEXES
-- ============================================
-- category_name already has unique index from UNIQUE constraint
CREATE INDEX idx_categories_parent_id ON categories(parent_category_id);

-- ============================================
-- PRODUCTS TABLE INDEXES
-- ============================================
-- sku already has unique index from UNIQUE constraint
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created_at ON products(created_at);

-- Composite index for common product listing queries
CREATE INDEX idx_products_active_category ON products(is_active, category_id);

-- Full-text search on product name and description (PostgreSQL specific)
CREATE INDEX idx_products_search ON products USING GIN(to_tsvector('english', product_name || ' ' || COALESCE(description, '')));

-- ============================================
-- INVENTORY TABLE INDEXES
-- ============================================
-- product_id already has unique index from UNIQUE constraint
CREATE INDEX idx_inventory_quantity ON inventory(quantity_in_stock);
CREATE INDEX idx_inventory_low_stock ON inventory(quantity_in_stock, reorder_level) 
    WHERE quantity_in_stock <= reorder_level;

-- ============================================
-- ORDERS TABLE INDEXES
-- ============================================
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Composite indexes for common order queries
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date DESC);

-- ============================================
-- ORDER_ITEMS TABLE INDEXES
-- ============================================
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Composite for order details retrieval
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

-- ============================================
-- REVIEWS TABLE INDEXES
-- ============================================
-- (product_id, user_id) already has unique index from UNIQUE constraint
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- For displaying reviews sorted by helpfulness
CREATE INDEX idx_reviews_product_helpful ON reviews(product_id, helpful_count DESC);

-- For verified purchase filtering
CREATE INDEX idx_reviews_verified ON reviews(product_id, is_verified_purchase) 
    WHERE is_verified_purchase = TRUE;

-- ============================================
-- ADDRESSES TABLE INDEXES
-- ============================================
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_type ON addresses(address_type);

-- For finding default addresses quickly
CREATE INDEX idx_addresses_user_default ON addresses(user_id, is_default) 
    WHERE is_default = TRUE;
