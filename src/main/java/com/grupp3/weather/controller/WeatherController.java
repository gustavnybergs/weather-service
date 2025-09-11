package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import com.grupp3.weather.service.WeatherCacheService;
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

    @GetMapping("/{placeName}/history")
    public ResponseEntity<?> history(@PathVariable String placeName, @RequestParam(defaultValue = "7") int days){

        // 1. Kolla om plats finns
        Place place = placeService.findByName(placeName).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Begränsar till att bara kolla historia för 7 eller 30 dagar
        if (days != 7 && days != 30) {
            return ResponseEntity.badRequest().body(
            Map.of("error", "only 7 och 30 days are allowed")
            );
        }

        // 3. Hämta historiska data
        Map<String, Object> raw = weatherService.fetchHistory(place.getLat(), place.getLon(), days);

        // 4. Forma svar
        Map<String, Object> response = Map.of(
                "place",  Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                "source", "open-meteo",
                "days", days,
                "data", raw.get("daily")
        );
        return ResponseEntity.ok(response);
    }
}

