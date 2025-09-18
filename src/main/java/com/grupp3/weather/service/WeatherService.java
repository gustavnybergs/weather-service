package com.grupp3.weather.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * WeatherService - external API-integrator och gateway till Open-Meteo väder-API.
 *
 * Skiljer sig från PlaceService genom att kommunicera med externa API:er via HTTP-anrop
 * istället för intern databas.
 *
 * Huvudfunktioner:
 * - fetchCurrent(double lat, double lon): Direkta väderanrop med koordinater → aktuellt väder
 * - fetchCurrentWeatherAtSpecificLocation(String location): Geocoding + väder i ett flöde
 * - fetchForecast(double lat, double lon): 7-dagars prognos via koordinater
 * - fetchLocationByName(String location): Platsökning via Open-Meteo geocoding API
 *
 * Använder RestClient för HTTP-requests och konverterar JSON-response till Map<String, Object>
 * där Object hanterar blandade datatyper från API:et (String, Double, Integer).
 * Felhantering kastar SERVICE_UNAVAILABLE vid externa API-fel.
 */

@Service
public class WeatherService {

    // === OPEN-METEO API CONFIGURATION ===
    private static final String OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1";
    private static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1";

    // === API PARAMETERS ===
    private static final String CURRENT_WEATHER_PARAMS = "current=temperature_2m,cloud_cover,wind_speed_10m";
    private static final String FORECAST_PARAMS = "daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,weather_code&timezone=auto";
    private static final int DEFAULT_FORECAST_DAYS = 7;

    private final RestClient http = RestClient.create();

    /**
     * Hämta nuvarande väderdata
     */
    public Map<String, Object> fetchCurrent(double lat, double lon) {
        String url = String.format(
                "%s/forecast?latitude=%s&longitude=%s&%s",
                OPEN_METEO_BASE_URL, lat, lon, CURRENT_WEATHER_PARAMS
        );

        try{
            return http.get().uri(url).retrieve().body(Map.class);
        } catch (Exception e){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Weather provider is currently unavailable, try again later",
                    e
            );
        }
    }

    /**
     * Hämta väderprognos för 7 dagar
     */
    public Map<String, Object> fetchForecast(double lat, double lon) {
        String url = String.format(
                "%s/forecast?latitude=%s&longitude=%s&%s&forecast_days=%d",
                OPEN_METEO_BASE_URL, lat, lon, FORECAST_PARAMS, DEFAULT_FORECAST_DAYS
        );
        return http.get().uri(url).retrieve().body(Map.class);
    }

    /**
     * Hämta väder historik för 7 och 30 dagar
     */
    public Map<String, Object> fetchHistory(double lat, double lon, int days) {
        LocalDate end = LocalDate.now().minusDays(1); // visar från gårdagens datum
        LocalDate start = end.minusDays(days -1);

        String url = String.format( // rätt url????
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,cloud_cover,wind_speed_10m",
                lat, lon
        );
        try{
            return http.get().uri(url).retrieve().body(Map.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Weather provider is currently unavailable, try again later",
                    e
            );
        }
    }

    /**
     * Hämta både current och forecast i ett anrop (effektivare)
     */
    public Map<String, Object> fetchCurrentAndForecast(double lat, double lon) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,cloud_cover,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,weather_code&timezone=auto&forecast_days=7",
                lat, lon
        );
        return http.get().uri(url).retrieve().body(Map.class);
    }

    /**
     *  Hämtar en specifik plats beroende på user:ens input samt fetchar vädret på platsen
     * @param location
     * @return
     */
    public Map<String, Object> fetchCurrentWeatherAtSpecificLocation(String location)  {
        String url = GEOCODING_BASE_URL + "/search?name=" + location;
        Map<String, Object> urlResponse = http.get().uri(url).retrieve().body(Map.class);

        List<Map<String, Object>> results = (List<Map<String,Object>>) urlResponse.get("results");
        if (results == null || results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found " + location);
        }

        Map<String, Object> firstResult = results.get(0);
        double latitude = ((Number) firstResult.get("latitude")).doubleValue();
        double longitude = ((Number) firstResult.get("longitude")).doubleValue();

        return fetchCurrent(latitude, longitude);
    }

    /**
     *  Hämtar en plats via namn
     * @param location
     * @return returns en bad request om staden inte finns eller stadens namn
     */

    public Map<String, Object> fetchLocationByName(String location) {
        String url = GEOCODING_BASE_URL + "/search?name=" + location;
        Map<String, Object> urlResponse = http.get().uri(url).retrieve().body(Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) urlResponse.get("results");
        if (results == null || results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found " + location);
        }

        return results.get(0);
    }
}