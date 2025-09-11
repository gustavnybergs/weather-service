package com.grupp3.weather.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    // Rate limits för olika typer av endpoints
    private static final int WEATHER_REQUESTS_PER_MINUTE = 30;  // 30 requests per minut för väder
    private static final int ADMIN_REQUESTS_PER_MINUTE = 10;    // 10 requests per minut för admin
    private static final int PLACES_WRITE_REQUESTS_PER_MINUTE = 20; // 20 skrivande requests per minut

    // DDoS detection thresholds
    private static final int DDOS_THRESHOLD_PER_MINUTE = 100;   // 100+ requests = misstänkt DDoS

    /**
     * Kolla om request tillåts för weather endpoints
     */
    public boolean isAllowed(String clientIp, EndpointType endpointType) {
        Bucket bucket = getBucket(clientIp, endpointType);
        return bucket.tryConsume(1);
    }

    /**
     * Kolla om IP:n visar DDoS-beteende
     */
    public boolean isDDoSBehavior(String clientIp) {
        Bucket ddosBucket = getDDoSBucket(clientIp);
        return !ddosBucket.tryConsume(1);
    }

    /**
     * Få tillgängliga tokens för en IP
     */
    public long getAvailableTokens(String clientIp, EndpointType endpointType) {
        Bucket bucket = getBucket(clientIp, endpointType);
        return bucket.getAvailableTokens();
    }

    /**
     * Få bucket för specifik IP och endpoint-typ
     */
    private Bucket getBucket(String clientIp, EndpointType endpointType) {
        String key = clientIp + ":" + endpointType.name();
        return cache.computeIfAbsent(key, k -> createBucket(endpointType));
    }

    /**
     * Få DDoS detection bucket
     */
    private Bucket getDDoSBucket(String clientIp) {
        String key = clientIp + ":DDOS";
        return cache.computeIfAbsent(key, k -> createDDoSBucket());
    }

    /**
     * Skapa bucket baserat på endpoint-typ
     */
    private Bucket createBucket(EndpointType endpointType) {
        Bandwidth limit;

        switch (endpointType) {
            case WEATHER:
                limit = Bandwidth.classic(WEATHER_REQUESTS_PER_MINUTE,
                        Refill.intervally(WEATHER_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
            case ADMIN:
                limit = Bandwidth.classic(ADMIN_REQUESTS_PER_MINUTE,
                        Refill.intervally(ADMIN_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
            case PLACES_WRITE:
                limit = Bandwidth.classic(PLACES_WRITE_REQUESTS_PER_MINUTE,
                        Refill.intervally(PLACES_WRITE_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
            default:
                // Default för övriga endpoints (GET places etc)
                limit = Bandwidth.classic(60,
                        Refill.intervally(60, Duration.ofMinutes(1)));
                break;
        }

        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Skapa DDoS detection bucket
     */
    private Bucket createDDoSBucket() {
        Bandwidth limit = Bandwidth.classic(DDOS_THRESHOLD_PER_MINUTE,
                Refill.intervally(DDOS_THRESHOLD_PER_MINUTE, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Rensa gamla buckets (körs periodiskt)
     */
    public void cleanupOldBuckets() {
        // I en riktig implementation skulle vi spåra senast använd tid
        // och rensa buckets som inte använts på länge
        if (cache.size() > 10000) {  // Enkel cleanup
            cache.clear();
        }
    }

    public enum EndpointType {
        WEATHER,
        ADMIN,
        PLACES_WRITE,
        PLACES_READ,
        OTHER
    }
}