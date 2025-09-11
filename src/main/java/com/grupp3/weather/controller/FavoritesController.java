package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final PlaceService placeService;

    public FavoritesController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /**
     * Markera en plats som favorit
     */
    @PutMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> markAsFavorite(@PathVariable String placeName) {
        // Kolla om plats finns
        if (!placeService.exists(placeName)) {
            Map<String, Object> error = Map.of("error", "Place '" + placeName + "' not found");
            return ResponseEntity.notFound().build();
        }

        // Markera som favorit
        return placeService.setFavorite(placeName, true)
                .map(place -> {
                    Map<String, Object> response = Map.of(
                            "message", "Place marked as favorite",
                            "place", place
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Ta bort plats från favoriter
     */
    @DeleteMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@PathVariable String placeName) {
        // Kolla om plats finns
        if (!placeService.exists(placeName)) {
            return ResponseEntity.notFound().build();
        }

        // Ta bort från favoriter
        return placeService.setFavorite(placeName, false)
                .map(place -> {
                    Map<String, Object> response = Map.of(
                            "message", "Place removed from favorites",
                            "place", place
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
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
    @GetMapping("/{placeName}")
    public ResponseEntity<Map<String, Object>> isFavorite(@PathVariable String placeName) {
        return placeService.findByName(placeName)
                .map(place -> {
                    Map<String, Object> response = Map.of(
                            "place", place.getName(),
                            "is_favorite", place.isFavorite()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}