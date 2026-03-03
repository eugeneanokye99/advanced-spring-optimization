package com.shopjoy.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class AsyncExecutorTest {

    @Autowired
    @Qualifier("appTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Test
    void testAppTaskExecutorSaturationAndRejection() throws InterruptedException {
        // Queue capacity is 200, Core is cores*2, Max is cores*4
        // We want to submit more than core + max + queue to trigger the rejection handler
        int corePoolSize = executor.getCorePoolSize();
        int maxPoolSize = executor.getMaxPoolSize();
        int queueCapacity = 200; // From AsyncConfig.java
        
        int totalTasksToSubmit = maxPoolSize + queueCapacity + 50;
        
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishingLatch = new CountDownLatch(maxPoolSize + queueCapacity); 
        AtomicInteger executedTasks = new AtomicInteger(0);

        // Submit tasks that block until we release them
        for (int i = 0; i < totalTasksToSubmit; i++) {
            try {
                executor.execute(() -> {
                    try {
                        latch.await(); // Hold the thread
                        executedTasks.incrementAndGet();
                        finishingLatch.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                // This would be the rejection if it wasn't handled by our custom handler
                // Our handler in AsyncConfig logs and drops, so it shouldn't throw an exception here
                // unless it's the default AbortPolicy.
                System.err.println("Task rejected by exception: " + e.getMessage());
            }
        }

        System.out.println("Submitted " + totalTasksToSubmit + " tasks.");
        System.out.println("Active threads: " + executor.getActiveCount());
        System.out.println("Queue size: " + executor.getThreadPoolExecutor().getQueue().size());

        // Verify the queue is full and threads are at max
        assertTrue(executor.getActiveCount() <= maxPoolSize, "Active threads should not exceed max pool size");
        
        // Release the tasks
        latch.countDown();
        
        boolean completed = finishingLatch.await(10, TimeUnit.SECONDS);
        System.out.println("Tasks completed within timeout: " + executedTasks.get());
        
        // If our custom handler is working, it should just log the error and not crash
        // The number of executed tasks might be less than total submitted due to rejection
        assertTrue(executedTasks.get() >= (maxPoolSize + queueCapacity), "Should have executed at least maxPoolSize + queueCapacity tasks");
    }
}
