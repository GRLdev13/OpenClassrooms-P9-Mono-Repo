package com.openclassroom.devops.orion.microcrm;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);
    private static final ThreadMXBean threadMetrics = ManagementFactory.getThreadMXBean();
    private final ObjectMapper objectMapper;

    public HttpRequestLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        long cpuStartedAt = currentThreadCpuTime();
        long heapUsedBefore = usedHeapBytes();

        try {
            filterChain.doFilter(request, response);
        } finally {
            Map<String, Object> metrics = new LinkedHashMap<>();
            long heapUsedAfter = usedHeapBytes();
            metrics.put("heap_used_bytes", heapUsedAfter);
            metrics.put("heap_delta_bytes", heapUsedAfter - heapUsedBefore);

            long cpuFinishedAt = currentThreadCpuTime();
            if (cpuStartedAt >= 0 && cpuFinishedAt >= cpuStartedAt) {
                metrics.put("thread_cpu_ns", cpuFinishedAt - cpuStartedAt);
            }

            Map<String, Object> accessLog = new LinkedHashMap<>();
            Map<String, Object> requestLog = new LinkedHashMap<>();
            requestLog.put("method", request.getMethod());
            requestLog.put("uri", request.getRequestURI());

            Object matchedRoute = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (matchedRoute != null) {
                requestLog.put("route", matchedRoute.toString());
            }

            accessLog.put("logger", "http.log.access.back");
            accessLog.put("msg", "handled request");
            accessLog.put("request", requestLog);
            accessLog.put("status", response.getStatus());
            accessLog.put("duration", (System.nanoTime() - startedAt) / 1_000_000_000.0);
            accessLog.put("metrics", metrics);

            try {
                logger.info(objectMapper.writeValueAsString(accessLog));
            } catch (JsonProcessingException exception) {
                logger.warn("Failed to serialize HTTP access log", exception);
            }
        }
    }

    private static long currentThreadCpuTime() {
        return threadMetrics.isCurrentThreadCpuTimeSupported() && threadMetrics.isThreadCpuTimeEnabled()
                ? threadMetrics.getCurrentThreadCpuTime()
                : -1;
    }

    private static long usedHeapBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
