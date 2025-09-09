package com.grupp3.weather.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WeatherService {
    // Använd standardinställningar (Jackson finns redan via Spring Boot)
    private final RestClient http = RestClient.create();

    public Map<String, Object> fetchCurrent(double lat, double lon) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,cloud_cover,wind_speed_10m",
                lat, lon
        );
        return http.get().uri(url).retrieve().body(Map.class);
    }
}
