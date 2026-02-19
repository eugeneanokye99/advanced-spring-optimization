package com.shopjoy.util;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to extract native Caffeine cache statistics from Spring's CacheManagers.
 */
@Component
public class CacheMetricsCollector {

    private final Map<String, CacheManager> cacheManagers;

    public CacheMetricsCollector(
            CacheManager cacheManager, // Primary
            CacheManager mediumCacheManager,
            CacheManager shortCacheManager) {
        this.cacheManagers = new LinkedHashMap<>();
        this.cacheManagers.put("Primary (30m)", cacheManager);
        this.cacheManagers.put("Medium (10m)", mediumCacheManager);
        this.cacheManagers.put("Short (2m)", shortCacheManager);
    }

    /**
     * Gets statistics for all caches across all CacheManagers.
     */
    public Map<String, Object> getAllCacheStats() {
        Map<String, Object> allStats = new LinkedHashMap<>();

        for (Map.Entry<String, CacheManager> entry : cacheManagers.entrySet()) {
            String managerName = entry.getKey();
            CacheManager manager = entry.getValue();

            if (manager instanceof CaffeineCacheManager caffeineManager) {
                Map<String, Object> managerStats = new LinkedHashMap<>();
                Collection<String> cacheNames = caffeineManager.getCacheNames();

                for (String cacheName : cacheNames) {
                    CaffeineCache cache = (CaffeineCache) manager.getCache(cacheName);
                    if (cache != null) {
                        managerStats.put(cacheName, formatStats(cache.getNativeCache().stats()));
                    }
                }
                allStats.put(managerName, managerStats);
            }
        }

        return allStats;
    }

    /**
     * Gets a summary of cache performance.
     */
    public Map<String, Object> getCacheSummary() {
        long totalHitCount = 0;
        long totalMissCount = 0;
        long totalRequestCount = 0;
        long totalEvictionCount = 0;

        for (CacheManager manager : cacheManagers.values()) {
            if (manager instanceof CaffeineCacheManager caffeineManager) {
                for (String cacheName : caffeineManager.getCacheNames()) {
                    CaffeineCache cache = (CaffeineCache) manager.getCache(cacheName);
                    if (cache != null) {
                        CacheStats stats = cache.getNativeCache().stats();
                        totalHitCount += stats.hitCount();
                        totalMissCount += stats.missCount();
                        totalRequestCount += stats.requestCount();
                        totalEvictionCount += stats.evictionCount();
                    }
                }
            }
        }

        double hitRate = totalRequestCount > 0 ? (double) totalHitCount / totalRequestCount * 100 : 0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallHitRate", String.format("%.2f%%", hitRate));
        summary.put("totalHits", totalHitCount);
        summary.put("totalMisses", totalMissCount);
        summary.put("totalRequests", totalRequestCount);
        summary.put("totalEvictions", totalEvictionCount);

        return summary;
    }

    private Map<String, Object> formatStats(CacheStats stats) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
        map.put("hits", stats.hitCount());
        map.put("misses", stats.missCount());
        map.put("requests", stats.requestCount());
        map.put("evictions", stats.evictionCount());
        map.put("averageLoadPenaltyMs", String.format("%.2f", stats.averageLoadPenalty() / 1_000_000.0));
        return map;
    }
}
