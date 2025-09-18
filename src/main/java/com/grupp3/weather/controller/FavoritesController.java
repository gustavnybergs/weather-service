package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final PlaceService placeService;
    private final WeatherService weatherService;

    public FavoritesController(PlaceService placeService, WeatherService weatherService) {
        this.placeService = placeService;
        this.weatherService = weatherService;
    }

    /**
     * Markera en plats som favorit - koordinatbaserad
     */
    @PutMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> markAsFavorite(@PathVariable String placeName) {
        System.out.println("Trying to add favorite: " + placeName);

        try {
            // Hämta koordinater från Open-Meteo
            System.out.println("Fetching location data for: " + placeName);
            Map<String, Object> locationData = weatherService.fetchLocationByName(placeName);

            if (locationData == null) {
                System.out.println("Location not found: " + placeName);
                Map<String, Object> error = Map.of("error", "Location '" + placeName + "' not found");
                return ResponseEntity.notFound().build();
            }

            // Extrahera koordinater
            Double lat = ((Number) locationData.get("latitude")).doubleValue();
            Double lon = ((Number) locationData.get("longitude")).doubleValue();
            System.out.println("Coordinates: " + lat + ", " + lon);

            // Kolla om plats redan finns på dessa koordinater
            if (!placeService.existsByCoordinates(lat, lon)) {
                System.out.println("Creating new place at coordinates: " + lat + ", " + lon);

                // Skapa ny plats med användarens sökterm som displayName
                Place newPlace = new Place(placeName, lat, lon);  // placeName = vad användaren sökte
                Place createdPlace = placeService.create(newPlace);
                System.out.println("Place created: " + createdPlace);
            } else {
                System.out.println("Place already exists at these coordinates");
            }

            System.out.println("Setting place as favorite...");

            // Markera som favorit baserat på koordinater
            return placeService.setFavoriteByCoordinates(lat, lon, true)
                    .map(place -> {
                        System.out.println("Successfully marked as favorite: " + place);
                        Map<String, Object> response = Map.of(
                                "message", "Place marked as favorite",
                                "place", place,
                                "searched_name", placeName,
                                "coordinates", Map.of("lat", lat, "lon", lon)
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        System.out.println("Failed to set favorite");
                        return ResponseEntity.notFound().build();
                    });

        } catch (Exception e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = Map.of(
                    "error", "Could not add favorite: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Ta bort plats från favoriter
     */
    @DeleteMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@PathVariable String placeName) {
        try {
            // Hitta platsen först (försök både koordinat-baserat och namn-baserat)
            Optional<Place> place = placeService.findByName(placeName);

            if (place.isEmpty()) {
                // Försök hitta via Open-Meteo koordinater
                Map<String, Object> locationData = weatherService.fetchLocationByName(placeName);
                if (locationData != null) {
                    Double lat = ((Number) locationData.get("latitude")).doubleValue();
                    Double lon = ((Number) locationData.get("longitude")).doubleValue();
                    place = placeService.findByCoordinates(lat, lon);
                }
            }

            if (place.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Ta bort från favoriter
            return placeService.setFavoriteByCoordinates(place.get().getLat(), place.get().getLon(), false)
                    .map(updatedPlace -> {
                        Map<String, Object> response = Map.of(
                                "message", "Place removed from favorites",
                                "place", updatedPlace
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            Map<String, Object> error = Map.of("error", "Could not remove favorite: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Lista alla favoritplatser
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFavorites() {
        List<Place> favorites = placeService.findFavorites();

        Map<String, Object> response = Map.of(
                "total_favorites", favorites.size(),
                "favorites", favorites
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Kolla om en specifik plats är favorit
     */
    /**
     * Kolla om en specifik plats är favorit - använder samma söklogik som markAsFavorite
     */
    @GetMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> isFavorite(@PathVariable String placeName) {
        System.out.println("Checking if favorite: " + placeName);

        try {
            // Först försök hitta efter displayName (snabbare för befintliga platser)
            Optional<Place> place = placeService.findByName(placeName);

            if (place.isEmpty()) {
                System.out.println("Not found by name, trying coordinates...");
                // Om inte hittat, använd koordinater via Open-Meteo
                Map<String, Object> locationData = weatherService.fetchLocationByName(placeName);
                if (locationData != null) {
                    Double lat = ((Number) locationData.get("latitude")).doubleValue();
                    Double lon = ((Number) locationData.get("longitude")).doubleValue();
                    System.out.println("Searching by coordinates: " + lat + ", " + lon);
                    place = placeService.findByCoordinates(lat, lon);
                }
            }

            if (place.isPresent()) {
                boolean isFavorite = place.get().isFavorite();
                System.out.println("Found place: " + place.get().getDisplayName() + ", is_favorite: " + isFavorite);

                Map<String, Object> response = Map.of(
                        "place", placeName,
                        "is_favorite", isFavorite,
                        "actual_place_name", place.get().getDisplayName(),
                        "coordinates", Map.of("lat", place.get().getLat(), "lon", place.get().getLon())
                );
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Place not found in database: " + placeName);
                Map<String, Object> response = Map.of(
                        "place", placeName,
                        "is_favorite", false
                );
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            System.err.println("Error checking favorite status for " + placeName + ": " + e.getMessage());
            // Fallback - returnera false vid fel
            Map<String, Object> response = Map.of(
                    "place", placeName,
                    "is_favorite", false
            );
            return ResponseEntity.ok(response);
        }
    }
}