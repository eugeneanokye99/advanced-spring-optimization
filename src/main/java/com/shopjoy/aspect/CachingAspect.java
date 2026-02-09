package com.shopjoy.aspect;

import com.shopjoy.util.AspectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class CachingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(CachingAspect.class);
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, Integer> cacheMisses = new ConcurrentHashMap<>();
    
    private static final long CACHE_TTL = 300000;
    
    @Around("execution(* com.shopjoy.service.*.findById(..)) || execution(* com.shopjoy.service.*.getById(..))")
    public Object cacheResult(ProceedingJoinPoint pjp) throws Throwable {
        String cacheKey = generateCacheKey(pjp);
        
        if (isCacheValid(cacheKey)) {
            incrementCacheHit(cacheKey);
            Object cachedResult = cache.get(cacheKey);
            logger.debug("CACHE HIT: {} - returning cached result", cacheKey);
            return cachedResult;
        }
        
        incrementCacheMiss(cacheKey);
        logger.debug("CACHE MISS: {} - executing method", cacheKey);
        
        Object result = pjp.proceed();
        
        if (result != null) {
            cache.put(cacheKey, result);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            logger.debug("CACHE STORED: {}", cacheKey);
        }
        
        return result;
    }
    
    @After("execution(* com.shopjoy.service.*.update*(..)) || execution(* com.shopjoy.service.*.delete*(..))")
    public void invalidateCache(JoinPoint jp) {
        String className = AspectUtils.extractClassName(jp);
        String methodName = AspectUtils.extractMethodName(jp);
        
        int cleared = 0;
        for (String key : cache.keySet()) {
            if (key.startsWith(className)) {
                cache.remove(key);
                cacheTimestamps.remove(key);
                cleared++;
            }
        }
        
        if (cleared > 0) {
            logger.info("CACHE INVALIDATED: {} entries cleared for {}.{}", 
                cleared, className, methodName);
        }
    }
    
    @Around("execution(* com.shopjoy.service.*.findAll*(..))")
    public Object cacheListResult(ProceedingJoinPoint pjp) throws Throwable {
        String cacheKey = generateCacheKey(pjp);
        
        if (isCacheValid(cacheKey)) {
            incrementCacheHit(cacheKey);
            logger.debug("CACHE HIT (List): {} - returning cached list", cacheKey);
            return cache.get(cacheKey);
        }
        
        incrementCacheMiss(cacheKey);
        Object result = pjp.proceed();
        
        if (result != null) {
            cache.put(cacheKey, result);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            logger.debug("CACHE STORED (List): {}", cacheKey);
        }
        
        return result;
    }
    
    private String generateCacheKey(JoinPoint joinPoint) {
        return AspectUtils.generateCacheKey(joinPoint);
    }
    
    private boolean isCacheValid(String cacheKey) {
        if (!cache.containsKey(cacheKey)) {
            return false;
        }
        
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) {
            return false;
        }
        
        long age = System.currentTimeMillis() - timestamp;
        if (age > CACHE_TTL) {
            cache.remove(cacheKey);
            cacheTimestamps.remove(cacheKey);
            logger.debug("CACHE EXPIRED: {}", cacheKey);
            return false;
        }
        
        return true;
    }
    
    private void incrementCacheHit(String cacheKey) {
        cacheHits.merge(cacheKey, 1, (oldValue, newValue) -> oldValue + newValue);
    }
    
    private void incrementCacheMiss(String cacheKey) {
        cacheMisses.merge(cacheKey, 1, (oldValue, newValue) -> oldValue + newValue);
    }
    
    public void clearAllCache() {
        int size = cache.size();
        cache.clear();
        cacheTimestamps.clear();
        logger.info("CACHE CLEARED: {} entries removed", size);
    }
    
    public Map<String, Object> getCacheStatistics() {
        int totalHits = cacheHits.values().stream().mapToInt(Integer::intValue).sum();
        int totalMisses = cacheMisses.values().stream().mapToInt(Integer::intValue).sum();
        int totalRequests = totalHits + totalMisses;
        double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0;
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", cache.size());
        stats.put("totalHits", totalHits);
        stats.put("totalMisses", totalMisses);
        stats.put("totalRequests", totalRequests);
        stats.put("hitRate", String.format("%.2f%%", hitRate));
        
        logger.info("CACHE STATS: Size={}, Hits={}, Misses={}, Hit Rate={}", 
            cache.size(), totalHits, totalMisses, String.format("%.2f%%", hitRate));
        
        return stats;
    }
}
