package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.model.WeatherForecast;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    private final PlaceService placeService;
    private final WeatherForecastService forecastService;

    public ForecastController(PlaceService placeService, WeatherForecastService forecastService) {
        this.placeService = placeService;
        this.forecastService = forecastService;
    }

    /**
     * Hämta 7-dagars prognos för en plats
     */
    @GetMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> getForecast(@PathVariable String placeName) {
        // Kolla om plats finns
        Place place = placeService.findByName(placeName).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }

        // Hämta prognoser
        List<WeatherForecast> forecasts = forecastService.getForecastsForPlace(placeName);

        Map<String, Object> response = Map.of(
                "place", Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                "source", "open-meteo",
                "forecast_days", forecasts.size(),
                "forecasts", forecasts
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Hämta prognos för specifika antal dagar
     */
    @GetMapping("/{placeName}/days/{days}")
    public ResponseEntity<Map<String, Object>> getForecastForDays(@PathVariable String placeName,
                                                                  @PathVariable int days) {
        if (days < 1 || days > 14) {
            Map<String, Object> error = Map.of("error", "Days must be between 1 and 14");
            return ResponseEntity.badRequest().body(error);
        }

        Place place = placeService.findByName(placeName).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }

        List<WeatherForecast> forecasts = forecastService.getForecastsForNextDays(placeName, days);

        Map<String, Object> response = Map.of(
                "place", Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                "source", "open-meteo",
                "requested_days", days,
                "actual_days", forecasts.size(),
                "forecasts", forecasts
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Hämta prognos för specifikt datum
     */
    @GetMapping("/{placeName}/date/{date}")
    public ResponseEntity<Map<String, Object>> getForecastForDate(@PathVariable String placeName,
                                                                  @PathVariable String date) {
        try {
            LocalDate forecastDate = LocalDate.parse(date);

            // Kolla om datum är i framtiden
            if (forecastDate.isBefore(LocalDate.now())) {
                Map<String, Object> error = Map.of("error", "Date must be today or in the future");
                return ResponseEntity.badRequest().body(error);
            }

            Place place = placeService.findByName(placeName).orElse(null);
            if (place == null) {
                return ResponseEntity.notFound().build();
            }

            WeatherForecast forecast = forecastService.getForecastForDate(placeName, forecastDate);
            if (forecast == null) {
                Map<String, Object> error = Map.of("error", "No forecast available for this date");
                return ResponseEntity.status(404).body(error);
            }

            Map<String, Object> response = Map.of(
                    "place", Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                    "source", "open-meteo",
                    "date", forecastDate,
                    "forecast", forecast
            );

            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            Map<String, Object> error = Map.of("error", "Invalid date format. Use YYYY-MM-DD");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Manuell uppdatering av prognoser för en plats (admin endpoint)
     */
    @PostMapping("/{placeName}/update")
    public ResponseEntity<Map<String, Object>> updateForecast(@PathVariable String placeName,
                                                              @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (!"topsecret123".equals(apiKey)) {
            return ResponseEntity.status(401).build();
        }

        Place place = placeService.findByName(placeName).orElse(null);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<WeatherForecast> forecasts = forecastService.fetchAndSaveForecast(place);

            Map<String, Object> response = Map.of(
                    "message", "Forecast updated successfully",
                    "place", placeName,
                    "forecasts_updated", forecasts.size()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = Map.of("error", "Failed to update forecast: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}