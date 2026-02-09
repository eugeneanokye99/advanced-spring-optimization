package com.shopjoy.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AspectUtils {
    
    private static final String SENSITIVE_DATA_MASK = "***REDACTED***";
    private static final String[] SENSITIVE_FIELD_NAMES = {
        "password", "pwd", "token", "secret", "key", "apikey", "api_key",
        "authorization", "auth", "credential", "passphrase"
    };
    
    public static String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.stream(args)
            .map(AspectUtils::sanitizeObject)
            .collect(Collectors.joining(", ", "[", "]"));
    }
    
    private static String sanitizeObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        String str = obj.toString();
        
        if (containsSensitiveField(obj.getClass().getSimpleName())) {
            return SENSITIVE_DATA_MASK;
        }
        
        for (String sensitiveField : SENSITIVE_FIELD_NAMES) {
            if (str.toLowerCase().contains(sensitiveField)) {
                return SENSITIVE_DATA_MASK;
            }
        }
        
        return str;
    }
    
    private static boolean containsSensitiveField(String className) {
        String lowerClassName = className.toLowerCase();
        for (String sensitiveField : SENSITIVE_FIELD_NAMES) {
            if (lowerClassName.contains(sensitiveField)) {
                return true;
            }
        }
        return false;
    }
    

    public static String extractMethodSignature(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        String paramTypes = Arrays.stream(signature.getParameterTypes())
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "));
        return String.format("%s.%s(%s)", className, methodName, paramTypes);
    }
    
    public static String formatExecutionTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    public static String extractClassName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getSimpleName();
    }
    
    public static String extractMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName();
    }
}
