package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    private final List<Place> places = new ArrayList<>();
    private static final String API_KEY = "topsecret123";

    // GET: Hämta alla platser
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Place> all() {
        return places;
    }

    // POST: Skapa en ny plats
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Place> create(@RequestBody Place place,
                                        @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // Kontrollera API-nyckel
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validera namn
        if (place.getName() == null || place.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Kolla dubblett
        if (places.stream().anyMatch(p -> p.getName().equalsIgnoreCase(place.getName()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place '" + place.getName() + "already exists");
        }

        // Lägg till och returnera 201 Created
        places.add(place);
        return ResponseEntity
                .created(URI.create("/places/" + place.getName()))
                .body(place);
    }

    // PUT: Uppdatera en plats
    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Place> update(@PathVariable String name,
                                        @RequestBody Place updated,
                                        @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return places.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(existing -> {
                    existing.setLat(updated.getLat());
                    existing.setLon(updated.getLon());
                    return ResponseEntity.ok(existing);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Place '" + name + "' does not exist, can therefor not update. Try again"));
    }

    // DELETE: Ta bort en plats
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name,
                                       @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean removed = places.removeIf(p -> p.getName().equalsIgnoreCase(name));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Place '" + name + "' does not exist, can therefor for delete. Try again");
        }

        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
