package com.shopjoy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine.
 * Caching Strategy:
 * - SHORT TTL (2 min): Frequently changing data (inventory, cart, stock levels)
 * - MEDIUM TTL (10 min): Moderately changing data (orders, reviews)
 * - LONG TTL (30 min): Relatively stable data (products, categories, users, addresses)
 * Cache eviction is handled via @CacheEvict annotations on write operations.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary cache manager for long-lived data (Products, Categories, Users).
     * TTL: 30 minutes, Max size: 1000 entries.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "products", "product", "activeProducts", "productsByCategory", "productsCount", "filteredProducts",
            "productSearch", "productsByPriceRange",
            "categories", "category",
            "users", "userProfile", "userProfileEmail", "userProfileUsername", "usersByIds",
            "userDetails"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats());
        return cacheManager;
    }

    /**
     * Cache manager for medium-lived data (Orders, Reviews).
     * TTL: 10 minutes, Max size: 500 entries.
     */
    @Bean
    public CacheManager mediumCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "orders", "order", "ordersByUser", "ordersByStatus", "pendingOrders",
            "reviews", "review", "reviewsByProduct", "reviewsByUser", "productRating",
            "addresses", "address", "addressesByUser", "defaultAddress"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats());
        return cacheManager;
    }

    /**
     * Cache manager for short-lived data (Inventory, Cart).
     * TTL: 2 minutes, Max size: 500 entries.
     * Short TTL because stock levels change frequently.
     */
    @Bean
    public CacheManager shortCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager( 
            "inventory", "inventoryByProduct", "lowStock", "outOfStock",
            "cart", "cartItems", "cartTotal", "cartCount"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats());
        return cacheManager;
    }
}
