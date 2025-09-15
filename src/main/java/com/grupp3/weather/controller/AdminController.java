package com.grupp3.weather.controller;

import com.grupp3.weather.service.ScheduledWeatherService;
import com.grupp3.weather.service.WeatherCacheService;
import com.grupp3.weather.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ScheduledWeatherService scheduledWeatherService;
    private final WeatherCacheService cacheService;
    private final WeatherDataRepository weatherDataRepository;
    private static final String API_KEY = "topsecret123";

    @Autowired
    private DataSource dataSource;

    public AdminController(ScheduledWeatherService scheduledWeatherService,
                           WeatherCacheService cacheService,
                           WeatherDataRepository weatherDataRepository) {
        this.scheduledWeatherService = scheduledWeatherService;
        this.cacheService = cacheService;
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * Manuell trigger för väderuppdatering
     */
    @PostMapping("/weather/update")
    public ResponseEntity<Map<String, String>> triggerWeatherUpdate(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }

        scheduledWeatherService.triggerManualUpdate();
        return ResponseEntity.ok(Map.of("message", "Weather update triggered"));
    }

    /**
     * Rensa all cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> clearCache(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }

        cacheService.clearAllCache();
        return ResponseEntity.ok(Map.of("message", "All cache cleared"));
    }

    /**
     * Statistik över systemet
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }

        long totalWeatherRecords = weatherDataRepository.count();
        var distinctPlaces = weatherDataRepository.findDistinctPlaceNames();

        Map<String, Object> stats = Map.of(
                "total_weather_records", totalWeatherRecords,
                "tracked_places", distinctPlaces.size(),
                "place_names", distinctPlaces
        );

        return ResponseEntity.ok(stats);
    }

    /**
    * Kontrollerar om systemet är igång
    */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }

    @GetMapping("/ready")
    public ResponseEntity<String> ready() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return ResponseEntity.ok("{\"status\": \"READY\"}");
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("{\"status\":\"DB not responding\"}");
        }
        catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("{\"status\":\"NOT READY\", \"error\":\"" + e.getMessage() + "\"}");
        }
    }
}