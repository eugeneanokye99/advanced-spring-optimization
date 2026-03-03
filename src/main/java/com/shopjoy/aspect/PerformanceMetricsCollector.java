package com.shopjoy.aspect;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PerformanceMetricsCollector {

    private final Map<String, CopyOnWriteArrayList<Long>> metrics = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> callCounts = new ConcurrentHashMap<>();

    public void recordMetric(String category, String methodKey, long executionTime) {
        String key = category + ":" + methodKey;

        metrics.computeIfAbsent(key, _ -> new CopyOnWriteArrayList<>()).add(executionTime);

        callCounts.computeIfAbsent(key, _ -> new AtomicLong(0)).incrementAndGet();
    }

    public Map<String, Map<String, Object>> getAllMetrics() {
        Map<String, Map<String, Object>> allMetrics = new LinkedHashMap<>();

        for (Map.Entry<String, CopyOnWriteArrayList<Long>> entry : metrics.entrySet()) {
            String key = entry.getKey();
            List<Long> times = entry.getValue();
            AtomicLong count = callCounts.get(key);

            allMetrics.put(key, calculateStatistics(times, count != null ? count.get() : 0L));
        }

        return allMetrics;
    }

    private Map<String, Object> calculateStatistics(List<Long> times, long callCount) {
        Map<String, Object> stats = new LinkedHashMap<>();

        if (times.isEmpty()) {
            return stats;
        }

        List<Long> sortedTimes = new ArrayList<>(times);
        Collections.sort(sortedTimes);

        long min     = sortedTimes.get(0);
        long max     = sortedTimes.get(sortedTimes.size() - 1);
        long sum     = sortedTimes.stream().mapToLong(Long::longValue).sum();
        long average = sum / sortedTimes.size();
        long median  = calculateMedian(sortedTimes);
        long p95     = calculatePercentile(sortedTimes, 95);
        long p99     = calculatePercentile(sortedTimes, 99);

        stats.put("callCount", callCount);
        stats.put("min", min);
        stats.put("max", max);
        stats.put("average", average);
        stats.put("median", median);
        stats.put("p95", p95);
        stats.put("p99", p99);
        stats.put("totalTime", sum);

        return stats;
    }

    private long calculateMedian(List<Long> sortedTimes) {
        int size = sortedTimes.size();
        if (size % 2 == 0) {
            return (sortedTimes.get(size / 2 - 1) + sortedTimes.get(size / 2)) / 2;
        }
        return sortedTimes.get(size / 2);
    }

    private long calculatePercentile(List<Long> sortedTimes, int percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
        index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
        return sortedTimes.get(index);
    }
}
