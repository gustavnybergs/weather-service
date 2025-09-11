package com.grupp3.weather.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {
    private final RestClient http = RestClient.create();

    /**
     * Hämta nuvarande väderdata
     */
    public Map<String, Object> fetchCurrent(double lat, double lon) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,cloud_cover,wind_speed_10m",
                lat, lon
        );
        return http.get().uri(url).retrieve().body(Map.class);
    }

    /**
     * Hämta väderprognos för 7 dagar
     */
    public Map<String, Object> fetchForecast(double lat, double lon) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,weather_code&timezone=auto&forecast_days=7",
                lat, lon
        );
        return http.get().uri(url).retrieve().body(Map.class);
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
     *  Hämtar en specifik plats beroende på user:ens input och fetchar vädret på platsen
     * @param location
     * @return
     */
    public Map<String, Object> fetchCurrentWeatherAtSpecificLocation(String location)  {
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + location;
        Map<String, Object> urlResponse = http.get().uri(url).retrieve().body(Map.class);

        List<Map<String, Object>> results = (List<Map<String,Object>>) urlResponse.get("results");
        if (results == null || results.isEmpty()) {
            System.out.println("Location not found." + location);
            return null;
        }

        Map<String, Object> firstResult = results.get(0);
        double latitude = ((Number) firstResult.get("latitude")).doubleValue();
        double longitude = ((Number) firstResult.get("longitude")).doubleValue();

        return fetchCurrent(latitude, longitude);
    }
}