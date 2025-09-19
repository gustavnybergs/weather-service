package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesControllerTest {

    @InjectMocks
    private FavoritesController favoritesController;

    @Mock
    private PlaceService placeService;

    @Mock
    private WeatherService weatherService;

    @Test
    @DisplayName("markAsFavorite med befintlig plats ska markera som favorit")
    void markAsFavorite_WithExistingPlace_ShouldMarkAsFavorite() {
        // Arrange
        Place place = new Place("Stockholm", 59.3293, 18.0686);
        place.setFavorite(true);

        Map<String, Object> locationData = Map.of(
                "latitude", 59.3293,
                "longitude", 18.0686
        );

        when(weatherService.fetchLocationByName("Stockholm")).thenReturn(locationData);
        when(placeService.existsByCoordinates(59.3293, 18.0686)).thenReturn(true);
        when(placeService.setFavoriteByCoordinates(59.3293, 18.0686, true)).thenReturn(Optional.of(place));

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.markAsFavorite("Stockholm");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isEqualTo("Place marked as favorite");

        Place returnedPlace = (Place) body.get("place");
        assertThat(returnedPlace.getName()).isEqualTo("Stockholm");
        assertThat(returnedPlace.isFavorite()).isTrue();
    }

    @Test
    @DisplayName("markAsFavorite med icke-befintlig plats ska returnera 404")
    void markAsFavorite_WithNonExistentPlace_ShouldReturn404() {
        // Arrange
        when(weatherService.fetchLocationByName("NonExistent")).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.markAsFavorite("NonExistent");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(404);
        verify(placeService, never()).setFavoriteByCoordinates(anyDouble(), anyDouble(), anyBoolean());
    }

    @Test
    @DisplayName("removeFromFavorites med befintlig plats ska ta bort från favoriter")
    void removeFromFavorites_WithExistingPlace_ShouldRemoveFromFavorites() {
        // Arrange
        Place place = new Place("Stockholm", 59.3293, 18.0686);
        place.setFavorite(false);

        when(placeService.findByName("Stockholm")).thenReturn(Optional.of(place));
        when(placeService.setFavoriteByCoordinates(59.3293, 18.0686, false)).thenReturn(Optional.of(place));

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.removeFromFavorites("Stockholm");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isEqualTo("Place removed from favorites");

        Place returnedPlace = (Place) body.get("place");
        assertThat(returnedPlace.getName()).isEqualTo("Stockholm");
        assertThat(returnedPlace.isFavorite()).isFalse();
    }

    @Test
    @DisplayName("getFavorites ska returnera alla favoritplatser")
    void getFavorites_ShouldReturnAllFavorites() {
        // Arrange
        Place stockholm = new Place("Stockholm", 59.3293, 18.0686);
        stockholm.setFavorite(true);
        Place gothenburg = new Place("Göteborg", 57.7089, 11.9746);
        gothenburg.setFavorite(true);

        List<Place> favorites = Arrays.asList(stockholm, gothenburg);
        when(placeService.findFavorites()).thenReturn(favorites);

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.getFavorites();

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("total_favorites")).isEqualTo(2);

        @SuppressWarnings("unchecked")
        List<Place> returnedFavorites = (List<Place>) body.get("favorites");
        assertThat(returnedFavorites).hasSize(2);
        assertThat(returnedFavorites.get(0).getName()).isEqualTo("Stockholm");
        assertThat(returnedFavorites.get(1).getName()).isEqualTo("Göteborg");
    }

    @Test
    @DisplayName("getFavorites utan favoriter ska returnera tom lista")
    void getFavorites_WithNoFavorites_ShouldReturnEmptyList() {
        // Arrange
        when(placeService.findFavorites()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.getFavorites();

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("total_favorites")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<Place> favorites = (List<Place>) body.get("favorites");
        assertThat(favorites).isEmpty();
    }

    @Test
    @DisplayName("isFavorite med befintlig plats ska returnera favoritstatus")
    void isFavorite_WithExistingPlace_ShouldReturnFavoriteStatus() {
        // Arrange
        Place place = new Place("Stockholm", 59.3293, 18.0686);
        place.setFavorite(true);

        when(placeService.findByName("Stockholm")).thenReturn(Optional.of(place));

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.isFavorite("Stockholm");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("place")).isEqualTo("Stockholm");
        assertThat(body.get("is_favorite")).isEqualTo(true);
    }

    @Test
    @DisplayName("isFavorite med icke-befintlig plats ska returnera false")
    void isFavorite_WithNonExistentPlace_ShouldReturnFalse() {
        // Arrange
        Map<String, Object> locationData = Map.of(
                "latitude", 59.0,
                "longitude", 18.0
        );

        when(placeService.findByName("NonExistent")).thenReturn(Optional.empty());
        when(weatherService.fetchLocationByName("NonExistent")).thenReturn(locationData);
        when(placeService.findByCoordinates(59.0, 18.0)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> result = favoritesController.isFavorite("NonExistent");

        // Assert
        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        Map<String, Object> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("place")).isEqualTo("NonExistent");
        assertThat(body.get("is_favorite")).isEqualTo(false);
    }

    @Test
    @DisplayName("Verifierar att PlaceService anropas korrekt")
    void shouldCallPlaceServiceCorrectly() {
        // Arrange
        Place place = new Place("Test", 0.0, 0.0);
        Map<String, Object> locationData = Map.of("latitude", 0.0, "longitude", 0.0);

        when(weatherService.fetchLocationByName("Test")).thenReturn(locationData);
        when(placeService.existsByCoordinates(0.0, 0.0)).thenReturn(true);
        when(placeService.setFavoriteByCoordinates(0.0, 0.0, true)).thenReturn(Optional.of(place));

        // Act
        favoritesController.markAsFavorite("Test");

        // Assert
        verify(weatherService).fetchLocationByName("Test");
        verify(placeService).setFavoriteByCoordinates(0.0, 0.0, true);
    }
}