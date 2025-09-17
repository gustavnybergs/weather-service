package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import com.grupp3.weather.service.WeatherCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @InjectMocks
    private WeatherController weatherController;

    @Mock
    private PlaceService placeService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private WeatherCacheService weatherCacheService;

    @Test
    @DisplayName("getCurrentWeather med befintlig plats ska returnera väderdata")
    void getCurrentWeather_WithExistingPlace_ShouldReturnWeatherData() {
        // Arrange
        Place stockholm = new Place("Stockholm", 59.3293, 18.0686);
        Map<String, Object> weatherData = Map.of("temperature_2m", 15.4);

        when(placeService.findByName("Stockholm")).thenReturn(Optional.of(stockholm));
        when(weatherCacheService.getCachedWeather("Stockholm")).thenReturn(Optional.empty());
        when(weatherService.fetchCurrent(59.3293, 18.0686))
                .thenReturn(Map.of("current", weatherData));

        // Act
        ResponseEntity<?> result = weatherController.current("Stockholm");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    @DisplayName("getCurrentWeather med icke-befintlig plats ska kasta exception")
    void getCurrentWeather_WithNonExistentPlace_ShouldThrowException() {
        // Arrange
        when(placeService.findByName("NonExistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> weatherController.current("NonExistent"))
                .isInstanceOf(ResponseStatusException.class);
    }
}