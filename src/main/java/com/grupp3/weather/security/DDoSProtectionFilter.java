package com.grupp3.weather.security;

import com.grupp3.weather.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Kör efter API key filter
public class DDoSProtectionFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    // Spåra misstänkta IP:s
    private final ConcurrentHashMap<String, AtomicInteger> suspiciousIPs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockedIPs = new ConcurrentHashMap<>();

    private static final long BLOCK_DURATION_MS = 15 * 60 * 1000; // 15 minuter

    public DDoSProtectionFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String path = request.getRequestURI();

        // 1. Kolla om IP är blockerad
        if (isBlocked(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.setHeader("Retry-After", "900"); // 15 minuter
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"IP temporarily blocked due to suspicious activity\"}");
            return;
        }

        // 2. Upptäck misstänkt beteende
        if (isSuspiciousBehavior(request, clientIp, userAgent)) {
            blockIP(clientIp);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Suspicious activity detected. IP blocked.\"}");
            return;
        }

        // 3. Rate limiting baserat på endpoint-typ
        RateLimitingService.EndpointType endpointType = determineEndpointType(path, request.getMethod());

        if (!rateLimitingService.isAllowed(clientIp, endpointType)) {
            // Räkna upp misstänkt aktivitet
            suspiciousIPs.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).incrementAndGet();

            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        // Lägg till rate limit headers
        long remaining = rateLimitingService.getAvailableTokens(clientIp, endpointType);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        filterChain.doFilter(request, response);
    }

    /**
     * Upptäck misstänkt beteende
     */
    private boolean isSuspiciousBehavior(HttpServletRequest request, String clientIp, String userAgent) {
        // 1. Kolla DDoS-tröskelvärden
        if (rateLimitingService.isDDoSBehavior(clientIp)) {
            return true;
        }

        // 2. Kolla User-Agent
        if (userAgent == null || userAgent.isEmpty() || isSuspiciousUserAgent(userAgent)) {
            return true;
        }

        // 3. Kolla om samma IP gjort för många misstänkta requests
        AtomicInteger suspiciousCount = suspiciousIPs.get(clientIp);
        if (suspiciousCount != null && suspiciousCount.get() > 10) {
            return true;
        }

        // 4. Kolla efter bot-patterns
        String referer = request.getHeader("Referer");
        if (isBotlikeBehavior(userAgent, referer)) {
            return true;
        }

        return false;
    }

    /**
     * Blockera IP temporärt
     */
    private void blockIP(String clientIp) {
        blockedIPs.put(clientIp, System.currentTimeMillis() + BLOCK_DURATION_MS);
        System.err.println("[SECURITY] Blocked IP " + clientIp + " for suspicious activity");
    }

    /**
     * Kolla om IP är blockerad
     */
    private boolean isBlocked(String clientIp) {
        Long blockExpiry = blockedIPs.get(clientIp);
        if (blockExpiry == null) return false;

        if (System.currentTimeMillis() > blockExpiry) {
            blockedIPs.remove(clientIp);
            suspiciousIPs.remove(clientIp);
            return false;
        }
        return true;
    }

    /**
     * Bestäm endpoint-typ för rate limiting
     */
    private RateLimitingService.EndpointType determineEndpointType(String path, String method) {
        if (path.startsWith("/admin/")) {
            return RateLimitingService.EndpointType.ADMIN;
        }
        if (path.startsWith("/weather/")) {
            return RateLimitingService.EndpointType.WEATHER;
        }
        if (path.startsWith("/places") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return RateLimitingService.EndpointType.PLACES_WRITE;
        }
        if (path.startsWith("/places")) {
            return RateLimitingService.EndpointType.PLACES_READ;
        }
        return RateLimitingService.EndpointType.OTHER;
    }

    /**
     * Få riktig client IP (hanterar proxies/load balancers)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Kolla misstänkta User-Agents
     */
    private boolean isSuspiciousUserAgent(String userAgent) {
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") ||
                ua.contains("crawler") ||
                ua.contains("spider") ||
                ua.contains("scraper") ||
                ua.length() < 10 ||
                ua.equals("curl") ||
                ua.equals("wget");
    }

    /**
     * Kolla bot-liknande beteende
     */
    private boolean isBotlikeBehavior(String userAgent, String referer) {
        // Detta är en förenklad implementation
        // I verkligheten skulle vi ha mer sofistikerad bot-detection
        return userAgent != null && userAgent.toLowerCase().contains("python");
    }
}