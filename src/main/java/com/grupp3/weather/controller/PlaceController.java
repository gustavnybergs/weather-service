package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService placeService;
    public PlaceController(PlaceService placeService) { this.placeService = placeService; }

    @GetMapping
    public List<Place> all() {
        return placeService.findAll();
    }

    @PostMapping
    public ResponseEntity<Place> create(@RequestBody Place place) {
        // 400 om name saknas
        if (place.getName() == null || place.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // 409 om dubblett
        if (placeService.exists(place.getName())) {
            return ResponseEntity.status(409).build();
        }
        // 201 Created + Location
        Place saved = placeService.create(place);
        return ResponseEntity.created(URI.create("/places/" + saved.getName())).body(saved);
    }

    @PutMapping("/{name}")
    public ResponseEntity<Place> update(@PathVariable String name, @RequestBody Place incoming) {
        return placeService.update(name, incoming)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        return placeService.delete(name) ? ResponseEntity.noContent().build()
                                         : ResponseEntity.notFound().build();
    }
}
