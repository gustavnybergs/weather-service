package com.example.weather;
import com.grupp3.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchLocationByName_found() {
        String location = "Stockholm";
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + location;

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", "Stockholm");
        resultMap.put("country", "Sweden");

        List<Map<String, Object>> results = Collections.singletonList(resultMap);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("results", results);

        when(restTemplate.getForObject(url, Map.class)).thenReturn(apiResponse);

        Map<String, Object> response = weatherService.fetchLocationByName(location);

        assertNotNull(response);
        assertEquals("Stockholm", response.get("name"));
        assertEquals("Sweden", response.get("country"));
    }


    @Test
    void testFetchCurrentWeatherAtSpecificLocation() {
        String location = "Stockholm";

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("name", "Stockholm");
        locationMap.put("latitude", 59.3293);
        locationMap.put("longitude", 18.0686);

        List<Map<String, Object>> results = Collections.singletonList(locationMap);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("results", results);

        when(restTemplate.getForObject("https://geocoding-api.open-meteo.com/v1/search?name=" + location, Map.class)).thenReturn(apiResponse);

        WeatherService spyService = spy(weatherService);
        Map<String, Object> weatherMap = new HashMap<>();
        weatherMap.put("temperature", 15);
        doReturn(weatherMap).when(spyService).fetchCurrent(59.3293, 18.0686);

        Map <String, Object> response = spyService.fetchCurrentWeatherAtSpecificLocation(location);

        assertNotNull(response);
        assertEquals(15, response.get("temperature"));

    }
}

