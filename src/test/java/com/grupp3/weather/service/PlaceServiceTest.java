package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeRepository);
    }

    @Test
    @DisplayName("findByName med befintlig plats ska returnera plats")
    void findByName_WithExistingPlace_ShouldReturnPlace() {
        // Arrange
        Place stockholm = new Place("Stockholm", 59.3293, 18.0686);
        when(placeRepository.findByNameIgnoreCase("Stockholm")).thenReturn(Optional.of(stockholm));

        // Act
        Optional<Place> result = placeService.findByName("Stockholm");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Stockholm");
        assertThat(result.get().getLat()).isEqualTo(59.3293);
    }

    @Test
    @DisplayName("findByName med icke-befintlig plats ska returnera tom Optional")
    void findByName_WithNonExistingPlace_ShouldReturnEmpty() {
        // Arrange
        when(placeRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        // Act
        Optional<Place> result = placeService.findByName("NonExistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("create ska spara och returnera plats")
    void create_ShouldSaveAndReturnPlace() {
        // Arrange
        Place newPlace = new Place("Malmö", 55.6050, 13.0038);
        Place savedPlace = new Place("Malmö", 55.6050, 13.0038);
        savedPlace.setId(1L);

        when(placeRepository.save(newPlace)).thenReturn(savedPlace);

        // Act
        Place result = placeService.create(newPlace);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Malmö");
        verify(placeRepository).save(newPlace);
    }

    @Test
    @DisplayName("update med befintlig plats ska uppdatera koordinater")
    void update_WithExistingPlace_ShouldUpdateCoordinates() {
        // Arrange
        Place existingPlace = new Place("Stockholm", 59.3293, 18.0686);
        existingPlace.setId(1L);

        Place updatedData = new Place("Stockholm", 59.3300, 18.0700);

        when(placeRepository.findByNameIgnoreCase("Stockholm")).thenReturn(Optional.of(existingPlace));
        when(placeRepository.save(existingPlace)).thenReturn(existingPlace);

        // Act
        Optional<Place> result = placeService.update("Stockholm", updatedData);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getLat()).isEqualTo(59.3300);
        assertThat(result.get().getLon()).isEqualTo(18.0700);
    }

    @Test
    @DisplayName("delete med befintlig plats ska returnera true")
    void delete_WithExistingPlace_ShouldReturnTrue() {
        // Arrange
        when(placeRepository.existsByNameIgnoreCase("Stockholm")).thenReturn(true);

        // Act
        boolean result = placeService.delete("Stockholm");

        // Assert
        assertThat(result).isTrue();
        verify(placeRepository).deleteByNameIgnoreCase("Stockholm");
    }

    @Test
    @DisplayName("setFavorite ska uppdatera favoritstatus")
    void setFavorite_ShouldUpdateFavoriteStatus() {
        // Arrange
        Place place = new Place("Stockholm", 59.3293, 18.0686);
        place.setFavorite(false);

        when(placeRepository.findByNameIgnoreCase("Stockholm")).thenReturn(Optional.of(place));
        when(placeRepository.save(place)).thenReturn(place);

        // Act
        Optional<Place> result = placeService.setFavorite("Stockholm", true);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().isFavorite()).isTrue();
    }

    @Test
    @DisplayName("findFavorites ska returnera endast favoritplatser")
    void findFavorites_ShouldReturnOnlyFavoritePlaces() {
        // Arrange
        Place favPlace1 = new Place("Stockholm", 59.3293, 18.0686);
        favPlace1.setFavorite(true);
        Place favPlace2 = new Place("Göteborg", 57.7089, 11.9746);
        favPlace2.setFavorite(true);

        List<Place> favorites = Arrays.asList(favPlace1, favPlace2);
        when(placeRepository.findFavorites()).thenReturn(favorites);

        // Act
        List<Place> result = placeService.findFavorites();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Place::isFavorite);
        assertThat(result).extracting(Place::getName).contains("Stockholm", "Göteborg");
    }

    @Test
    @DisplayName("exists ska returnera korrekt boolean värde")
    void exists_ShouldReturnCorrectBoolean() {
        // Arrange
        when(placeRepository.existsByNameIgnoreCase("Stockholm")).thenReturn(true);
        when(placeRepository.existsByNameIgnoreCase("NonExistent")).thenReturn(false);

        // Act & Assert
        assertThat(placeService.exists("Stockholm")).isTrue();
        assertThat(placeService.exists("NonExistent")).isFalse();
    }

    @Test
    @DisplayName("Null-hantering ska fungera korrekt")
    void nullHandling_ShouldWorkCorrectly() {
        // Act & Assert
        assertThat(placeService.exists(null)).isFalse();

        // Verify repository never called with null
        verify(placeRepository, never()).existsByNameIgnoreCase(null);
    }

}