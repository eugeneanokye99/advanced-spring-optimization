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
    private static final long SLOW_METHOD_THRESHOLD = 1000;
    private static final long SLOW_DB_THRESHOLD = 500;
    private static final long SLOW_API_THRESHOLD = 2000;
    
    @Autowired
    private PerformanceMetricsCollector metricsCollector;
    
    @Around("com.shopjoy.aspect.CommonPointcuts.serviceMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            metricsCollector.recordMetric("service", methodKey, executionTime);
            
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Pageable p && p.getSort().isSorted()) {
                    metricsCollector.recordMetric("sorting", methodKey + "[" + p.getSort().toString() + "]", executionTime);
                } else if (arg instanceof Sort s && s.isSorted()) {
                    metricsCollector.recordMetric("sorting", methodKey + "[" + s.toString() + "]", executionTime);
                }
            }

            if (executionTime > SLOW_METHOD_THRESHOLD) {
                logger.warn("SLOW SERVICE METHOD: {}.{} took {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            } else {
                logger.debug("Service method {}.{} executed in {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            }
            
            return result;
        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsCollector.recordMetric("service", methodKey, executionTime);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.repositoryMethods()")
    public Object monitorDatabasePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            metricsCollector.recordMetric("database", methodKey, executionTime);
            
            if (executionTime > SLOW_DB_THRESHOLD) {
                logger.warn("SLOW DATABASE QUERY: {}.{} took {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            } else {
                logger.debug("Database query {}.{} executed in {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            }
            
            return result;
        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsCollector.recordMetric("database", methodKey, executionTime);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.controllerMethods()")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            metricsCollector.recordMetric("api", methodKey, executionTime);
            
            if (executionTime > SLOW_API_THRESHOLD) {
                logger.warn("SLOW API ENDPOINT: {}.{} took {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            } else {
                logger.info("API endpoint {}.{} responded in {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            }
            
            return result;
        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsCollector.recordMetric("api", methodKey, executionTime);
            throw t;
        }
    }
    
    @Around("com.shopjoy.aspect.CommonPointcuts.graphqlResolverMethods()")
    public Object monitorGraphQLPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = AspectUtils.extractClassName(joinPoint);
        String methodName = AspectUtils.extractMethodName(joinPoint);
        String methodKey = className + "." + methodName;
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            metricsCollector.recordMetric("graphql", methodKey, executionTime);
            
            if (executionTime > SLOW_API_THRESHOLD) {
                logger.warn("SLOW GRAPHQL RESOLVER: {}.{} took {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            } else {
                logger.debug("GraphQL resolver {}.{} executed in {}", 
                    className, methodName, AspectUtils.formatExecutionTime(executionTime));
            }
            
            return result;
        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsCollector.recordMetric("graphql", methodKey, executionTime);
            throw t;
        }
    }
}
