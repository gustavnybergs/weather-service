package com.grupp3.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class WeatherCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String CACHE_PREFIX = "weather:";

    public WeatherCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Hämta cachad väderdata för en plats
     */
    public Optional<Map<String, Object>> getCachedWeather(String placeName) {
        try {
            String cacheKey = CACHE_PREFIX + placeName.toLowerCase();
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);

            if (cachedJson != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> weatherData = objectMapper.readValue(cachedJson, Map.class);
                return Optional.of(weatherData);
            }

            return Optional.empty();
        } catch (JsonProcessingException e) {
            // Log error och returnera empty för att trigga ny hämtning
            System.err.println("Error reading cached weather data: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cacha väderdata för en plats med TTL
     */
    public void cacheWeather(String placeName, Map<String, Object> weatherData) {
        try {
            String cacheKey = CACHE_PREFIX + placeName.toLowerCase();
            String jsonData = objectMapper.writeValueAsString(weatherData);

            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_TTL);
        } catch (JsonProcessingException e) {
            // Log error men låt applikationen fortsätta
            System.err.println("Error caching weather data: " + e.getMessage());
        }
    }

    /**
     * Ta bort cachad data för en plats
     */
    public void evictCache(String placeName) {
        String cacheKey = CACHE_PREFIX + placeName.toLowerCase();
        redisTemplate.delete(cacheKey);
    }

    /**
     * Rensa all väder-cache
     */
    public void clearAllCache() {
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"));
    }

    /**
     * Kolla om data finns i cache
     */
    public boolean isCached(String placeName) {
        String cacheKey = CACHE_PREFIX + placeName.toLowerCase();
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }
}