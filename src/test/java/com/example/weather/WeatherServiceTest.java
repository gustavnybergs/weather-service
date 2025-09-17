package com.example.weather;

import com.grupp3.weather.WeatherApiApplication;
import com.grupp3.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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
}

