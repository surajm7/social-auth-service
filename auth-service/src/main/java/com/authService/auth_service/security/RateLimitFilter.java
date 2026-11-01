package com.authService.auth_service.security;

import com.authService.auth_service.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.login-capacity:10}")
    private int capacity;

    @Value("${app.rate-limit.login-refill-tokens:10}")
    private int refillTokens;

    @Value("${app.rate-limit.login-refill-minutes:1}")
    private int refillMinutes;

    private final Map<String, RateLimitBucket> bucketCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isRateLimitedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = getClientIp(request);
        RateLimitBucket bucket = bucketCache.computeIfAbsent(clientIp, k -> new RateLimitBucket(capacity, refillMinutes * 60 * 1000));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiResponse.error("Too many requests. Please try again later.", "RATE_LIMIT_EXCEEDED")
                    )
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isRateLimitedEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return "POST".equals(method) && (path.equals("/api/auth/login")
                || path.equals("/api/auth/forgot-password"));
    }

    // Simple rate limit bucket implementation
    private static class RateLimitBucket {
        private final int capacity;
        private final long refillWindowMs;
        private int tokens;
        private long lastRefillTime;

        RateLimitBucket(int capacity, long refillWindowMs) {
            this.capacity = capacity;
            this.refillWindowMs = refillWindowMs;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime >= refillWindowMs) {
                tokens = capacity;
                lastRefillTime = now;
            }

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
    }
}
