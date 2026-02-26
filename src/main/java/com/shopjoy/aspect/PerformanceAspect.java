package com.shopjoy.aspect;

import com.shopjoy.util.AspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;
    private static final long SLOW_DB_THRESHOLD_MS = 500;
    private static final long SLOW_API_THRESHOLD_MS = 2000;

    @Autowired
    private PerformanceMetricsCollector metricsCollector;
    
    @Around("com.shopjoy.aspect.CommonPointcuts.nonProductReadServiceMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

            metricsCollector.recordMetric("service", methodKey, executionTimeMs);

            boolean hasPagination = false;
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Pageable p && p.getSort().isSorted()) {
                    metricsCollector.recordMetric("sorting", methodKey + "[" + p.getSort() + "]", executionTimeMs);
                    hasPagination = true;
                } else if (arg instanceof Sort s && s.isSorted()) {
                    metricsCollector.recordMetric("sorting", methodKey + "[" + s + "]", executionTimeMs);
                    hasPagination = true;
                }
            }

            if (executionTimeMs > SLOW_METHOD_THRESHOLD_MS) {
                logger.warn("SLOW SERVICE METHOD: {}.{} took {}ms",
                    className, methodName, executionTimeMs);
            } else if (logger.isDebugEnabled() && !hasPagination) {
                logger.debug("Service method {}.{} executed in {}ms",
                    className, methodName, executionTimeMs);
            }
            
            return result;
        } catch (Throwable t) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordMetric("service", methodKey, executionTimeMs);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.repositoryMethods() && com.shopjoy.aspect.CommonPointcuts.dataModificationMethods()")
    public Object monitorDatabasePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

            metricsCollector.recordMetric("database", methodKey, executionTimeMs);

            if (executionTimeMs > SLOW_DB_THRESHOLD_MS) {
                logger.warn("SLOW DATABASE QUERY: {}.{} took {}ms",
                    className, methodName, executionTimeMs);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Database query {}.{} executed in {}ms",
                    className, methodName, executionTimeMs);
            }
            
            return result;
        } catch (Throwable t) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordMetric("database", methodKey, executionTimeMs);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.nonProductReadControllerMethods()")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

            metricsCollector.recordMetric("api", methodKey, executionTimeMs);

            if (executionTimeMs > SLOW_API_THRESHOLD_MS) {
                logger.warn("SLOW API ENDPOINT: {}.{} took {}ms",
                    className, methodName, executionTimeMs);
            } else if (logger.isInfoEnabled()) {
                logger.info("API endpoint {}.{} responded in {}ms",
                    className, methodName, executionTimeMs);
            }
            
            return result;
        } catch (Throwable t) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordMetric("api", methodKey, executionTimeMs);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.graphqlResolverMethods()")
    public Object monitorGraphQLPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

            metricsCollector.recordMetric("graphql", methodKey, executionTimeMs);

            if (executionTimeMs > SLOW_API_THRESHOLD_MS) {
                logger.warn("SLOW GRAPHQL RESOLVER: {}.{} took {}ms",
                    className, methodName, executionTimeMs);
            } else if (logger.isDebugEnabled()) {
                logger.debug("GraphQL resolver {}.{} executed in {}ms",
                    className, methodName, executionTimeMs);
            }
            
            return result;
        } catch (Throwable t) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordMetric("graphql", methodKey, executionTimeMs);
            throw t;
        }
    }
}
