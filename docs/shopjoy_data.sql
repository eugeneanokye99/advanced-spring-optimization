-- ============================================
-- SHOPJOY TEST DATA GENERATION SCRIPT
-- Run this after creating all tables
-- ============================================

-- Clear existing data (in correct order to avoid foreign key conflicts)
DELETE FROM reviews;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM addresses;
DELETE FROM inventory;
DELETE FROM products;

-- ============================================
-- INSERT CART ITEMS
-- ============================================
INSERT INTO cart_items (user_id, product_id, quantity, added_at) VALUES
(2, 1, 1, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 11, 2, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
(3, 5, 1, CURRENT_TIMESTAMP - INTERVAL '2 hours');
DELETE FROM categories;
DELETE FROM users;

-- Reset sequences
ALTER SEQUENCE users_user_id_seq RESTART WITH 1;
ALTER SEQUENCE categories_category_id_seq RESTART WITH 1;
ALTER SEQUENCE products_product_id_seq RESTART WITH 1;
ALTER SEQUENCE inventory_inventory_id_seq RESTART WITH 1;
ALTER SEQUENCE orders_order_id_seq RESTART WITH 1;
ALTER SEQUENCE order_items_order_item_id_seq RESTART WITH 1;
ALTER SEQUENCE reviews_review_id_seq RESTART WITH 1;
ALTER SEQUENCE addresses_address_id_seq RESTART WITH 1;

-- ============================================
-- INSERT USERS (1 admin + 20 customers)
-- ============================================
-- Password for all users: "password123" (hashed with BCrypt)
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, user_type, created_at, updated_at) VALUES
('admin', 'admin@shopjoy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', '555-0000', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('john_doe', 'john.doe@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '555-0101', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP),
('jane_smith', 'jane.smith@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '555-0102', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '5 months', CURRENT_TIMESTAMP),
('bob_wilson', 'bob.wilson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Wilson', '555-0103', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '4 months', CURRENT_TIMESTAMP),
('alice_brown', 'alice.brown@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Brown', '555-0104', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 months', CURRENT_TIMESTAMP),
('charlie_davis', 'charlie.davis@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie', 'Davis', '555-0105', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 months', CURRENT_TIMESTAMP),
('emma_johnson', 'emma.johnson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Emma', 'Johnson', '555-0106', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP),
('david_lee', 'david.lee@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'David', 'Lee', '555-0107', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP),
('sophia_martinez', 'sophia.martinez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sophia', 'Martinez', '555-0108', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 month', CURRENT_TIMESTAMP),
('michael_garcia', 'michael.garcia@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Michael', 'Garcia', '555-0109', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 month', CURRENT_TIMESTAMP),
('olivia_rodriguez', 'olivia.rodriguez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Olivia', 'Rodriguez', '555-0110', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 weeks', CURRENT_TIMESTAMP),
('james_hernandez', 'james.hernandez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'James', 'Hernandez', '555-0111', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 weeks', CURRENT_TIMESTAMP),
('ava_lopez', 'ava.lopez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ava', 'Lopez', '555-0112', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 weeks', CURRENT_TIMESTAMP),
('william_gonzalez', 'william.gonzalez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'William', 'Gonzalez', '555-0113', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 weeks', CURRENT_TIMESTAMP),
('isabella_wilson', 'isabella.wilson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Isabella', 'Wilson', '555-0114', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 week', CURRENT_TIMESTAMP),
('ethan_anderson', 'ethan.anderson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ethan', 'Anderson', '555-0115', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 week', CURRENT_TIMESTAMP),
('mia_thomas', 'mia.thomas@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mia', 'Thomas', '555-0116', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
('alexander_taylor', 'alexander.taylor@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alexander', 'Taylor', '555-0117', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
('charlotte_moore', 'charlotte.moore@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlotte', 'Moore', '555-0118', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
('daniel_jackson', 'daniel.jackson@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Daniel', 'Jackson', '555-0119', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP),
('amelia_white', 'amelia.white@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amelia', 'White', '555-0120', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP);

-- ============================================
-- INSERT CATEGORIES (Hierarchical)
-- ============================================
-- Top-level categories
INSERT INTO categories (category_name, description, parent_category_id, created_at) VALUES
('Electronics', 'Electronic devices and accessories', NULL, CURRENT_TIMESTAMP),
('Home & Kitchen', 'Home appliances and kitchen essentials', NULL, CURRENT_TIMESTAMP),
('Books', 'Physical and digital books', NULL, CURRENT_TIMESTAMP),
('Clothing & Fashion', 'Apparel and fashion accessories', NULL, CURRENT_TIMESTAMP),
('Sports & Outdoors', 'Sports equipment and outdoor gear', NULL, CURRENT_TIMESTAMP),
('Toys & Games', 'Toys and games for all ages', NULL, CURRENT_TIMESTAMP),
('Beauty & Personal Care', 'Cosmetics and personal care products', NULL, CURRENT_TIMESTAMP),
('Automotive', 'Car parts and accessories', NULL, CURRENT_TIMESTAMP),
('Health & Wellness', 'Health supplements and wellness products', NULL, CURRENT_TIMESTAMP),
('Office Supplies', 'Office equipment and stationery', NULL, CURRENT_TIMESTAMP);

-- Subcategories (Electronics)
INSERT INTO categories (category_name, description, parent_category_id, created_at) VALUES
('Laptops', 'Portable computers', 1, CURRENT_TIMESTAMP),
('Desktops', 'Desktop computers and workstations', 1, CURRENT_TIMESTAMP),
('Smartphones', 'Mobile phones and accessories', 1, CURRENT_TIMESTAMP),
('Tablets', 'Tablet computers', 1, CURRENT_TIMESTAMP),
('Headphones & Audio', 'Audio equipment and accessories', 1, CURRENT_TIMESTAMP),
('Cameras', 'Digital cameras and photography equipment', 1, CURRENT_TIMESTAMP),
('Smart Home', 'Smart home devices and IoT', 1, CURRENT_TIMESTAMP);

-- Subcategories (Home & Kitchen)
INSERT INTO categories (category_name, description, parent_category_id, created_at) VALUES
('Kitchen Appliances', 'Small and large kitchen appliances', 2, CURRENT_TIMESTAMP),
('Cookware', 'Pots, pans, and cooking utensils', 2, CURRENT_TIMESTAMP),
('Home Decor', 'Decorative items for home', 2, CURRENT_TIMESTAMP);

-- Subcategories (Clothing)
INSERT INTO categories (category_name, description, parent_category_id, created_at) VALUES
('Men''s Clothing', 'Clothing for men', 4, CURRENT_TIMESTAMP),
('Women''s Clothing', 'Clothing for women', 4, CURRENT_TIMESTAMP),
('Footwear', 'Shoes and sneakers', 4, CURRENT_TIMESTAMP);

-- ============================================
-- INSERT PRODUCTS (100+ products)
-- ============================================

-- Electronics - Laptops
INSERT INTO products (product_name, description, category_id, price, cost_price, sku, brand, image_url, is_active, created_at, updated_at) VALUES
('Dell XPS 13 Laptop', '13.3" FHD, Intel Core i7-1165G7, 16GB RAM, 512GB SSD', 11, 1299.99, 950.00, 'DELL-XPS13-001', 'Dell', 'https://via.placeholder.com/300x300?text=Dell+XPS+13', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MacBook Pro 14"', 'Apple M3 chip, 16GB RAM, 512GB SSD', 11, 1999.99, 1600.00, 'APPLE-MBP14-001', 'Apple', 'https://via.placeholder.com/300x300?text=MacBook+Pro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HP Spectre x360', '14" OLED touchscreen, Intel i7, 16GB, 1TB SSD', 11, 1549.99, 1200.00, 'HP-SPECT-001', 'HP', 'https://via.placeholder.com/300x300?text=HP+Spectre', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lenovo ThinkPad X1', 'Business laptop, 14" FHD, Intel i5, 8GB, 256GB', 11, 1099.99, 850.00, 'LEN-X1-001', 'Lenovo', 'https://via.placeholder.com/300x300?text=ThinkPad+X1', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ASUS ROG Gaming Laptop', '15.6" 144Hz, RTX 3060, Ryzen 7, 16GB, 1TB', 11, 1399.99, 1050.00, 'ASUS-ROG-001', 'ASUS', 'https://via.placeholder.com/300x300?text=ASUS+ROG', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Desktops
('HP Pavilion Desktop', 'AMD Ryzen 5, 8GB RAM, 512GB SSD, Windows 11', 12, 699.99, 500.00, 'HP-PAV-DT-001', 'HP', 'https://via.placeholder.com/300x300?text=HP+Desktop', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dell OptiPlex Tower', 'Intel i7, 16GB RAM, 1TB HDD, Business PC', 12, 899.99, 650.00, 'DELL-OPTI-001', 'Dell', 'https://via.placeholder.com/300x300?text=Dell+OptiPlex', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iMac 24"', 'Apple M3, 8GB RAM, 256GB SSD, 4.5K Display', 12, 1499.99, 1200.00, 'APPLE-IMAC24-001', 'Apple', 'https://via.placeholder.com/300x300?text=iMac+24', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Smartphones
('Samsung Galaxy S24', '6.2" AMOLED, 128GB, 5G, Phantom Black', 13, 799.99, 600.00, 'SAM-S24-001', 'Samsung', 'https://via.placeholder.com/300x300?text=Galaxy+S24', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iPhone 15 Pro', '6.1" Super Retina XDR, 256GB, A17 Pro chip', 13, 1099.99, 850.00, 'APPL-IP15P-001', 'Apple', 'https://via.placeholder.com/300x300?text=iPhone+15+Pro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Google Pixel 8', '6.2" OLED, 128GB, Google Tensor G3', 13, 699.99, 500.00, 'GOOG-PIX8-001', 'Google', 'https://via.placeholder.com/300x300?text=Pixel+8', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('OnePlus 11', '6.7" AMOLED, 256GB, Snapdragon 8 Gen 2', 13, 649.99, 450.00, 'ONEPLUS-11-001', 'OnePlus', 'https://via.placeholder.com/300x300?text=OnePlus+11', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Tablets
('iPad Air 10.9"', 'Apple M1 chip, 64GB, Wi-Fi', 14, 599.99, 450.00, 'APPL-IPAD-AIR', 'Apple', 'https://via.placeholder.com/300x300?text=iPad+Air', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Samsung Galaxy Tab S9', '11" Display, 128GB, Android', 14, 749.99, 550.00, 'SAM-TABS9-001', 'Samsung', 'https://via.placeholder.com/300x300?text=Galaxy+Tab', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Microsoft Surface Pro 9', '13" PixelSense, Intel i5, 8GB, 256GB', 14, 999.99, 750.00, 'MS-SURF-PRO9', 'Microsoft', 'https://via.placeholder.com/300x300?text=Surface+Pro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Headphones
('Sony WH-1000XM5', 'Premium noise-cancelling wireless headphones', 15, 399.99, 280.00, 'SONY-WH1000-M5', 'Sony', 'https://via.placeholder.com/300x300?text=Sony+WH1000XM5', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bose QuietComfort 45', 'Wireless noise-cancelling headphones', 15, 329.99, 230.00, 'BOSE-QC45-001', 'Bose', 'https://via.placeholder.com/300x300?text=Bose+QC45', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Apple AirPods Pro 2', 'Active noise cancellation, MagSafe', 15, 249.99, 180.00, 'APPL-AIRPODS-P2', 'Apple', 'https://via.placeholder.com/300x300?text=AirPods+Pro', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JBL Tune 760NC', 'Wireless over-ear headphones with ANC', 15, 129.99, 80.00, 'JBL-TUNE760-001', 'JBL', 'https://via.placeholder.com/300x300?text=JBL+Tune', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Cameras
('Canon EOS R50', 'Mirrorless camera, 24.2MP, 4K video', 16, 899.99, 650.00, 'CANON-R50-001', 'Canon', 'https://via.placeholder.com/300x300?text=Canon+R50', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sony A7 IV', 'Full-frame mirrorless, 33MP, 4K 60fps', 16, 2499.99, 1900.00, 'SONY-A7IV-001', 'Sony', 'https://via.placeholder.com/300x300?text=Sony+A7+IV', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GoPro HERO12 Black', 'Action camera, 5.3K video, waterproof', 16, 399.99, 280.00, 'GOPRO-H12-001', 'GoPro', 'https://via.placeholder.com/300x300?text=GoPro+12', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics - Smart Home
('Amazon Echo Dot 5th Gen', 'Smart speaker with Alexa', 17, 49.99, 30.00, 'AMZN-ECHO-DOT5', 'Amazon', 'https://via.placeholder.com/300x300?text=Echo+Dot', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Google Nest Hub 2nd Gen', 'Smart display with Google Assistant', 17, 99.99, 70.00, 'GOOG-NEST-HUB2', 'Google', 'https://via.placeholder.com/300x300?text=Nest+Hub', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ring Video Doorbell', 'HD video doorbell with motion detection', 17, 99.99, 65.00, 'RING-VD-001', 'Ring', 'https://via.placeholder.com/300x300?text=Ring+Doorbell', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Philips Hue Starter Kit', 'Smart LED bulbs with bridge, 3-pack', 17, 149.99, 100.00, 'PHILIPS-HUE-KIT', 'Philips', 'https://via.placeholder.com/300x300?text=Philips+Hue', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Home & Kitchen - Kitchen Appliances
('Instant Pot Duo 7-in-1', '6 Qt electric pressure cooker', 18, 89.99, 60.00, 'IP-DUO7-6QT', 'Instant Pot', 'https://via.placeholder.com/300x300?text=Instant+Pot', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ninja Air Fryer', '4-quart air fryer with reheat & dehydrate', 18, 119.99, 75.00, 'NINJA-AF4-001', 'Ninja', 'https://via.placeholder.com/300x300?text=Ninja+Air+Fryer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Keurig K-Elite Coffee Maker', 'Single serve K-Cup pod coffee maker', 18, 169.99, 110.00, 'KEUR-KE-001', 'Keurig', 'https://via.placeholder.com/300x300?text=Keurig', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('KitchenAid Stand Mixer', 'Artisan 5-Qt tilt-head stand mixer', 18, 349.99, 250.00, 'KITAID-MIXER-001', 'KitchenAid', 'https://via.placeholder.com/300x300?text=KitchenAid+Mixer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Breville Espresso Machine', 'Barista Express espresso machine', 18, 699.99, 500.00, 'BREV-ESP-001', 'Breville', 'https://via.placeholder.com/300x300?text=Breville+Espresso', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vitamix E310 Blender', 'Professional-grade blender, 48oz', 18, 349.99, 250.00, 'VITA-E310-001', 'Vitamix', 'https://via.placeholder.com/300x300?text=Vitamix', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Home & Kitchen - Cookware
('T-fal Nonstick Cookware Set', '12-piece cookware set', 19, 129.99, 80.00, 'TFAL-COOK12-001', 'T-fal', 'https://via.placeholder.com/300x300?text=T-fal+Cookware', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lodge Cast Iron Skillet', '12-inch pre-seasoned cast iron', 19, 34.99, 20.00, 'LODGE-CI12-001', 'Lodge', 'https://via.placeholder.com/300x300?text=Cast+Iron', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cuisinart Knife Block Set', '15-piece stainless steel knife set', 19, 99.99, 60.00, 'CUISIN-KNIFE15', 'Cuisinart', 'https://via.placeholder.com/300x300?text=Knife+Set', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Books
('Atomic Habits', 'James Clear - Build good habits, break bad ones', 3, 16.99, 10.00, 'BOOK-ATOMIC-001', 'Penguin', 'https://via.placeholder.com/300x300?text=Atomic+Habits', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The 48 Laws of Power', 'Robert Greene - Power dynamics and strategy', 3, 18.99, 12.00, 'BOOK-48LAWS-001', 'Penguin', 'https://via.placeholder.com/300x300?text=48+Laws', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sapiens', 'Yuval Noah Harari - A brief history of humankind', 3, 18.99, 11.00, 'BOOK-SAP-001', 'Harper', 'https://via.placeholder.com/300x300?text=Sapiens', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Educated', 'Tara Westover - A memoir', 3, 15.99, 9.00, 'BOOK-EDU-001', 'Random House', 'https://via.placeholder.com/300x300?text=Educated', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Great Gatsby', 'F. Scott Fitzgerald - Classic American novel', 3, 14.99, 8.00, 'BOOK-TGG-001', 'Scribner', 'https://via.placeholder.com/300x300?text=Gatsby', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1984', 'George Orwell - Dystopian fiction classic', 3, 13.99, 7.50, 'BOOK-1984-001', 'Signet', 'https://via.placeholder.com/300x300?text=1984', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thinking, Fast and Slow', 'Daniel Kahneman - Behavioral economics', 3, 19.99, 12.00, 'BOOK-THINK-001', 'Farrar', 'https://via.placeholder.com/300x300?text=Thinking', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Clothing - Men's
('Nike Air Max 90', 'Classic sneakers, multiple colors', 21, 129.99, 80.00, 'NIKE-AM90-001', 'Nike', 'https://via.placeholder.com/300x300?text=Air+Max+90', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Levi''s 501 Original Jeans', 'Classic fit jeans, various sizes', 21, 69.99, 40.00, 'LEVI-501-001', 'Levi''s', 'https://via.placeholder.com/300x300?text=Levis+501', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ralph Lauren Polo Shirt', 'Classic fit polo, cotton', 21, 89.99, 50.00, 'RL-POLO-001', 'Ralph Lauren', 'https://via.placeholder.com/300x300?text=Polo+Shirt', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Patagonia Fleece Jacket', 'Men''s Better Sweater fleece', 21, 139.99, 90.00, 'PATA-FLEECE-M', 'Patagonia', 'https://via.placeholder.com/300x300?text=Patagonia+Fleece', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Clothing - Women's
('Adidas Ultraboost', 'Women''s running shoes', 22, 179.99, 110.00, 'ADIDAS-UB-W', 'Adidas', 'https://via.placeholder.com/300x300?text=Ultraboost', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lululemon Align Leggings', 'High-rise yoga pants, 25"', 22, 98.00, 60.00, 'LULU-ALIGN-001', 'Lululemon', 'https://via.placeholder.com/300x300?text=Lululemon', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The North Face Jacket', 'Women''s waterproof rain jacket', 22, 199.99, 130.00, 'NF-RAIN-W', 'The North Face', 'https://via.placeholder.com/300x300?text=North+Face', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Footwear
('Converse Chuck Taylor', 'Classic canvas sneakers, unisex', 23, 55.00, 30.00, 'CONV-CHUCK-001', 'Converse', 'https://via.placeholder.com/300x300?text=Chuck+Taylor', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Timberland Boots', 'Premium 6-inch waterproof boots', 23, 189.99, 120.00, 'TIMB-BOOT6-001', 'Timberland', 'https://via.placeholder.com/300x300?text=Timberland', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Crocs Classic Clogs', 'Comfortable slip-on clogs', 23, 49.99, 25.00, 'CROCS-CLAS-001', 'Crocs', 'https://via.placeholder.com/300x300?text=Crocs', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Sports & Outdoors
('Yeti Rambler 30oz', 'Stainless steel tumbler with lid', 5, 39.99, 22.00, 'YETI-RAM30-001', 'Yeti', 'https://via.placeholder.com/300x300?text=Yeti+Rambler', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Coleman 6-Person Tent', 'Dome tent with WeatherTec system', 5, 149.99, 95.00, 'COLE-TENT6-001', 'Coleman', 'https://via.placeholder.com/300x300?text=Coleman+Tent', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Wilson Basketball', 'Official size basketball', 5, 29.99, 15.00, 'WILS-BB-001', 'Wilson', 'https://via.placeholder.com/300x300?text=Basketball', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fitbit Charge 6', 'Fitness and health tracker', 5, 179.99, 120.00, 'FITBIT-C6-001', 'Fitbit', 'https://via.placeholder.com/300x300?text=Fitbit', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Manduka PRO Yoga Mat', 'Premium yoga mat, 6mm thick', 5, 119.99, 75.00, 'MAND-YOGA-001', 'Manduka', 'https://via.placeholder.com/300x300?text=Yoga+Mat', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Toys & Games
('LEGO Star Wars Set', 'Millennium Falcon building kit, 1351 pieces', 6, 169.99, 110.00, 'LEGO-SW-MF', 'LEGO', 'https://via.placeholder.com/300x300?text=LEGO+Star+Wars', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nintendo Switch OLED', 'Gaming console with 7" OLED screen', 6, 349.99, 270.00, 'NINT-SW-OLED', 'Nintendo', 'https://via.placeholder.com/300x300?text=Switch+OLED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PlayStation 5', 'Gaming console, disc edition', 6, 499.99, 400.00, 'SONY-PS5-DISC', 'Sony', 'https://via.placeholder.com/300x300?text=PS5', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Monopoly Board Game', 'Classic family board game', 6, 19.99, 10.00, 'MONO-CLAS-001', 'Hasbro', 'https://via.placeholder.com/300x300?text=Monopoly', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hot Wheels 20-Car Pack', 'Die-cast vehicles variety pack', 6, 24.99, 12.00, 'HW-20PACK-001', 'Hot Wheels', 'https://via.placeholder.com/300x300?text=Hot+Wheels', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Beauty & Personal Care
('Dyson Airwrap', 'Multi-styler for hair styling', 7, 599.99, 450.00, 'DYSON-AW-001', 'Dyson', 'https://via.placeholder.com/300x300?text=Dyson+Airwrap', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Oral-B iO Electric Toothbrush', 'Rechargeable electric toothbrush', 7, 199.99, 130.00, 'ORALB-IO-001', 'Oral-B', 'https://via.placeholder.com/300x300?text=Oral-B', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Neutrogena Hydro Boost', 'Water gel moisturizer, 1.7oz', 7, 19.99, 10.00, 'NEUT-HYDRO-001', 'Neutrogena', 'https://via.placeholder.com/300x300?text=Neutrogena', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CeraVe Moisturizing Cream', 'Daily face and body moisturizer, 16oz', 7, 15.99, 8.00, 'CERAV-MOIST-001', 'CeraVe', 'https://via.placeholder.com/300x300?text=CeraVe', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Revlon One-Step Hair Dryer', 'Volumizer hot air brush', 7, 59.99, 35.00, 'REVLON-1STEP-001', 'Revlon', 'https://via.placeholder.com/300x300?text=Revlon+Dryer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Automotive
('Michelin Defender Tires', 'All-season tire, single', 8, 149.99, 100.00, 'MICH-DEF-TIRE', 'Michelin', 'https://via.placeholder.com/300x300?text=Michelin+Tire', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Garmin Dash Cam Mini 2', 'Compact HD dashboard camera', 8, 129.99, 85.00, 'GARM-DC-MINI2', 'Garmin', 'https://via.placeholder.com/300x300?text=Dash+Cam', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Armor All Car Care Kit', 'Complete car cleaning kit', 8, 29.99, 15.00, 'ARMOR-KIT-001', 'Armor All', 'https://via.placeholder.com/300x300?text=Armor+All', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('WeatherTech Floor Mats', 'Custom fit all-weather floor liners', 8, 149.99, 95.00, 'WEATH-MAT-001', 'WeatherTech', 'https://via.placeholder.com/300x300?text=WeatherTech', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Health & Wellness
('Optimum Nutrition Whey Protein', 'Gold Standard 100% Whey, 5lb', 9, 64.99, 45.00, 'ON-WHEY-5LB', 'Optimum Nutrition', 'https://via.placeholder.com/300x300?text=ON+Whey', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nature Made Multivitamin', 'Daily multivitamin, 100 tablets', 9, 14.99, 8.00, 'NM-MULTI-100', 'Nature Made', 'https://via.placeholder.com/300x300?text=Multivitamin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Theragun Mini', 'Portable massage gun', 9, 199.99, 140.00, 'THERA-MINI-001', 'Therabody', 'https://via.placeholder.com/300x300?text=Theragun', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Office Supplies
('HP OfficeJet Pro Printer', 'All-in-one wireless color printer', 10, 179.99, 120.00, 'HP-OJ-PRO-001', 'HP', 'https://via.placeholder.com/300x300?text=HP+Printer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Logitech MX Master 3S', 'Wireless performance mouse', 10, 99.99, 65.00, 'LOGI-MXM3S-001', 'Logitech', 'https://via.placeholder.com/300x300?text=MX+Master', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Staples Copy Paper', '10-ream case, 8.5 x 11', 10, 49.99, 30.00, 'STAPLES-PAPER10', 'Staples', 'https://via.placeholder.com/300x300?text=Copy+Paper', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Swingline Stapler', 'Heavy-duty desk stapler', 10, 24.99, 12.00, 'SWING-STAPLER', 'Swingline', 'https://via.placeholder.com/300x300?text=Stapler', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Post-it Notes Super Sticky', '24-pack assorted colors', 10, 29.99, 15.00, 'POSTIT-24PACK', '3M', 'https://via.placeholder.com/300x300?text=Post-it', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Additional varied stock products
('Samsung 55" 4K TV', 'QLED Smart TV with HDR', 1, 799.99, 550.00, 'SAM-TV55-4K', 'Samsung', 'https://via.placeholder.com/300x300?text=Samsung+TV', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dyson V15 Vacuum', 'Cordless stick vacuum with laser detection', 2, 749.99, 520.00, 'DYSON-V15-001', 'Dyson', 'https://via.placeholder.com/300x300?text=Dyson+V15', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iRobot Roomba j7+', 'Robot vacuum with auto-empty base', 2, 799.99, 550.00, 'IROBOT-J7-001', 'iRobot', 'https://via.placeholder.com/300x300?text=Roomba', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- ============================================
-- INSERT INVENTORY (for all products)
-- ============================================
-- Varied stock levels: some high, some low, some out of stock
INSERT INTO inventory (product_id, quantity_in_stock, reorder_level, warehouse_location, last_restocked, updated_at)
SELECT
product_id,
CASE
WHEN product_id % 15 = 0 THEN 0  -- Out of stock (every 15th product)
WHEN product_id % 7 = 0 THEN FLOOR(RANDOM() * 5 + 3)::INTEGER  -- Low stock (every 7th product)
WHEN product_id % 3 = 0 THEN FLOOR(RANDOM() * 20 + 10)::INTEGER  -- Medium stock
ELSE FLOOR(RANDOM() * 100 + 30)::INTEGER  -- High stock
END,
10,  -- Reorder level
'Warehouse-' || (FLOOR(RANDOM() * 5 + 1))::TEXT,
CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days'),
CURRENT_TIMESTAMP
FROM products;