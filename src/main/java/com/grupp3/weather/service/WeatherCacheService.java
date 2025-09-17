package com.grupp3.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * WeatherCacheService - Redis-baserad cache-coordinator för väderdata prestanda.
 *
 * Skiljer sig från PlaceService och WeatherService genom att fungera som snabb
 * mellanlagring istället för databas-access eller externa API-anrop.
 *
 * Huvudfunktioner:
 * - getCachedWeather(String placeName): Hämta cachad väderdata → Optional för null-safety
 * - cacheWeather(String placeName, Map data): Spara väderdata med 5 min TTL
 * - evictCache(String placeName): Manuell cache-radering för specifik plats
 * - clearAllCache(): Töm hela väder-cachen för admin-ändamål
 *
 * Cache-logik implementerar:
 * - TTL-baserad förfallotid: Data försvinner automatiskt efter 5 minuter
 * - Lowercase normalisering: "Stockholm" och "stockholm" blir samma cache-nyckel
 * - JSON-serialisering: Map<String, Object> konverteras till Redis-kompatibel string
 * - Graceful degradation: Fel i cache triggar fresh API-anrop istället för krasch
 *
 * Används av WeatherController för att minska Open-Meteo API-belastning.
 * Cache miss → API-anrop, Cache hit → direkt svar utan externa anrop.
 */

@Service
public class WeatherCacheService {

    // === CACHE CONFIGURATION ===
    private static final int CACHE_TTL_MINUTES = 5;
    private static final Duration CACHE_TTL = Duration.ofMinutes(CACHE_TTL_MINUTES);
    private static final String CACHE_PREFIX = "weather:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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