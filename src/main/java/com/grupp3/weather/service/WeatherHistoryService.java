package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.model.WeatherData;
import com.grupp3.weather.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * WeatherHistoryService - databas-coordinator för permanent väderdata lagring.
 *
 * Skiljer sig från WeatherCacheService genom att spara väderdata permanent i PostgreSQL
 * istället för temporär Redis-lagring.
 *
 * Huvudfunktioner:
 * - fetchAndSaveWeatherData(Place place): Hämta från API → konvertera → spara i databas
 * - getLatestWeatherData(String placeName): Senaste sparade väderdata för alert-kontroller
 * - getWeatherHistory(String placeName, int hours): Historisk data för trendanalys
 *
 * Dataflöde implementerar:
 * - API-response parsing: Extraherar temperature, wind_speed, cloud_cover från JSON
 * - Tidskonvertering: Open-Meteo format → Java LocalDateTime för databas-kompatibilitet
 * - Dubbel uppdatering: Sparar i databas OCH uppdaterar cache samtidigt för effektivitet
 * - Graceful degradation: Returnerar null vid API-fel istället för systemkrasch
 *
 * Används av ScheduledWeatherService för automatisk historikbyggnad.
 * Databas-lagring → permanent, Cache-lagring → 5 minuter.
 */

@Service
public class WeatherHistoryService {

    private final WeatherDataRepository weatherDataRepository;
    private final WeatherService weatherService;
    private final WeatherCacheService cacheService;

    public WeatherHistoryService(WeatherDataRepository weatherDataRepository,
                                 WeatherService weatherService,
                                 WeatherCacheService cacheService) {
        this.weatherDataRepository = weatherDataRepository;
        this.weatherService = weatherService;
        this.cacheService = cacheService;
    }

    /**
     * Hämta och spara väderdata för en plats
     */
    public WeatherData fetchAndSaveWeatherData(Place place) {
        try {
            // Hämta rådata från Open-Meteo
            Map<String, Object> rawData = weatherService.fetchCurrent(place.getLat(), place.getLon());

            @SuppressWarnings("unchecked")
            Map<String, Object> currentData = (Map<String, Object>) rawData.get("current");

            // Konvertera och spara
            WeatherData weatherData = new WeatherData();
            weatherData.setPlaceName(place.getName());
            weatherData.setLatitude(place.getLat());
            weatherData.setLongitude(place.getLon());

            // Extrahera värden från Open-Meteo response
            if (currentData.get("temperature_2m") != null) {
                weatherData.setTemperature(((Number) currentData.get("temperature_2m")).doubleValue());
            }
            if (currentData.get("cloud_cover") != null) {
                weatherData.setCloudCover(((Number) currentData.get("cloud_cover")).intValue());
            }
            if (currentData.get("wind_speed_10m") != null) {
                weatherData.setWindSpeed(((Number) currentData.get("wind_speed_10m")).doubleValue());
            }

            // Parse observation time från API
            if (currentData.get("time") != null) {
                String timeStr = currentData.get("time").toString();
                weatherData.setObservationTime(parseObservationTime(timeStr));
            } else {
                weatherData.setObservationTime(LocalDateTime.now());
            }

            // Spara till databas
            WeatherData saved = weatherDataRepository.save(weatherData);

            // Uppdatera cache samtidigt
            Map<String, Object> cacheData = Map.of(
                    "place", Map.of("name", place.getName(), "lat", place.getLat(), "lon", place.getLon()),
                    "source", "open-meteo",
                    "data", currentData,
                    "cached", false
            );
            cacheService.cacheWeather(place.getName(), cacheData);

            return saved;

        } catch (Exception e) {
            System.err.println("Error fetching weather for " + place.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Hämta senaste väderdata för en plats från databasen
     */
    public WeatherData getLatestWeatherData(String placeName) {
        return weatherDataRepository.findLatestByPlaceName(placeName).orElse(null);
    }

    /**
     * Hämta väderhistorik för en plats
     */
    public List<WeatherData> getWeatherHistory(String placeName, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return weatherDataRepository.findByPlaceNameAndObservationTimeBetween(
                placeName, since, LocalDateTime.now()
        );
    }

    /**
     * Parse observation time från Open-Meteo format
     */
    private LocalDateTime parseObservationTime(String timeStr) {
        try {
            // Open-Meteo returnerar format som "2025-09-10T17:00"
            if (timeStr.length() == 16) {
                return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            }
            // Fallback till nuvarande tid
            return LocalDateTime.now();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}