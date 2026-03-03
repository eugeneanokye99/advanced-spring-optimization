package com.shopjoy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    /**
     * IO-bound: audit logging, security events — formula: cores * 2
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        int coreSize = CORES * 2;
        int maxSize  = CORES * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("taskExecutor: corePoolSize={}, maxPoolSize={} (IO-bound, cores*2)", coreSize, maxSize);
        return executor;
    }

    /**
     * IO-bound: order creation, payment, email — formula: cores * 2 core, cores * 4 max
     */
    @Bean(name = "appTaskExecutor")
    public ThreadPoolTaskExecutor appTaskExecutor() {
        int coreSize = CORES * 2;
        int maxSize  = CORES * 4;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("app-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler((_, _) ->
            log.error("Task rejected from appTaskExecutor — queue full, consider scaling")
        );
        executor.initialize();
        log.info("appTaskExecutor: corePoolSize={}, maxPoolSize={} (IO-bound, cores*2/cores*4)", coreSize, maxSize);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, _) ->
            log.error("Async method {} threw exception: {}", method.getName(), ex.getMessage(), ex);
    }
}

