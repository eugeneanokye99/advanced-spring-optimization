package com.shopjoy.aspect;

import com.shopjoy.util.AspectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@org.springframework.core.annotation.Order(1)
public class CachingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(CachingAspect.class);
    
    @Autowired
    private PerformanceMetricsCollector metricsCollector;
    
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    private static final long CACHE_TTL = 300000;
    
    @Around("execution(* com.shopjoy.service..*.find*(..)) || execution(* com.shopjoy.service..*.get*(..))")
    public Object cacheResult(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = AspectUtils.extractMethodName(pjp);
        
        // Skip caching for metrics collection itself or modification methods that might have "get" in them (rare)
        if (methodName.equals("getMetrics") || methodName.equals("getCacheStats")) {
            return pjp.proceed();
        }

        String cacheKey = generateCacheKey(pjp);
        
        if (isCacheValid(cacheKey)) {
            metricsCollector.recordCacheHit(cacheKey);
            Object cachedResult = cache.get(cacheKey);
            logger.debug("CACHE HIT: {} - returning cached result", cacheKey);
            return cachedResult;
        }
        
        metricsCollector.recordCacheMiss(cacheKey);
        logger.debug("CACHE MISS: {} - executing method", cacheKey);
        
        Object result = pjp.proceed();
        
        if (result != null) {
            cache.put(cacheKey, result);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            logger.debug("CACHE STORED: {}", cacheKey);
        }
        
        return result;
    }
    
    @After("execution(* com.shopjoy.service..*.update*(..)) || " +
           "execution(* com.shopjoy.service..*.delete*(..)) || " +
           "execution(* com.shopjoy.service..*.save*(..)) || " +
           "execution(* com.shopjoy.service..*.create*(..)) || " +
           "execution(* com.shopjoy.service..*.add*(..)) || " +
           "execution(* com.shopjoy.service..*.remove*(..)) || " +
           "execution(* com.shopjoy.service..*.clear*(..)) || " +
           "execution(* com.shopjoy.service..*.process*(..)) || " +
           "execution(* com.shopjoy.service..*.cancel*(..)) || " +
           "execution(* com.shopjoy.service..*.place*(..))")
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
    
    public void clearAllCache() {
        int size = cache.size();
        cache.clear();
        cacheTimestamps.clear();
        logger.info("CACHE CLEARED: {} entries removed", size);
    }
}
