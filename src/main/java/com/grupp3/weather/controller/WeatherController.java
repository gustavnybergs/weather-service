package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import com.grupp3.weather.service.WeatherCacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final PlaceService placeService;
    private final WeatherService weatherService;
    private final WeatherCacheService weatherCacheService;

    public WeatherController(PlaceService placeService,
                             WeatherService weatherService,
                             WeatherCacheService weatherCacheService) {
        this.placeService = placeService;
        this.weatherService = weatherService;
        this.weatherCacheService = weatherCacheService;
    }

    @GetMapping("/{placeName}")
    public ResponseEntity<?> current(@PathVariable String placeName) {
        // 1. Kolla om plats finns
        Place place = placeService.findByName(placeName).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Försök hämta från cache först
        Optional<Map<String, Object>> cachedWeather = weatherCacheService.getCachedWeather(placeName);
        if (cachedWeather.isPresent()) {
            // Cache hit! Lägg till en indikator
            Map<String, Object> response = cachedWeather.get();
            response.put("cached", true);
            return ResponseEntity.ok(response);
        }

        // 3. Cache miss - hämta från Open-Meteo
        Map<String, Object> rawWeatherData = weatherService.fetchCurrent(place.getLat(), place.getLon());

        // 4. Forma svaret
        Map<String, Object> response = Map.of(
                "place", Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                "source", "open-meteo",
                "data", rawWeatherData.get("current"),
                "cached", false
        );

        // 5. Spara i cache för framtida anrop
        weatherCacheService.cacheWeather(placeName, response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/weatherAtLocation/{placeName}")
    public ResponseEntity<Map<String, Object>> getWeatherAtSpecificLocation(@PathVariable String placeName) {
        Map<String, Object> weather = weatherService.fetchCurrentWeatherAtSpecificLocation((placeName));
        if (weather == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Location not found: " + placeName));
        }

        return ResponseEntity.ok(weather);
    }

    @GetMapping("/locationByName/{placeName}")
    public ResponseEntity<Map<String, Object>> fetchLocationByName(@PathVariable String placeName) {
        Map<String,Object> response = weatherService.fetchLocationByName(placeName);
        if (response == null) {
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok(response);
    }

    // Bonus: Endpoint för att rensa cache
    @DeleteMapping("/cache/{placeName}")
    public ResponseEntity<Void> clearCache(@PathVariable String placeName) {
        weatherCacheService.evictCache(placeName);
        return ResponseEntity.noContent().build();
    }

    // Bonus: Endpoint för att rensa all cache
    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearAllCache() {
        weatherCacheService.clearAllCache();
        return ResponseEntity.noContent().build();
    }
}