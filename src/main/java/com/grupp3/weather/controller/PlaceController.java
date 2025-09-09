package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    private final List<Place> places = new ArrayList<>();

    @GetMapping
    public List<Place> all() {
        return places;
    }

    @PostMapping
    public Place create(@RequestBody Place place) {
        places.add(place);
        return place;
    }

    @PutMapping("/{name}")
    public ResponseEntity<Place> update(@PathVariable String name, @RequestBody Place incoming) {
        for (Place p : places) {
            if (p.getName().equalsIgnoreCase(name)) {
                if (incoming.getName() != null && !incoming.getName().isBlank()) {
                    p.setName(incoming.getName());
                }
                p.setLat(incoming.getLat());
                p.setLon(incoming.getLon());
                return ResponseEntity.ok(p);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        boolean removed = places.removeIf(p -> p.getName().equalsIgnoreCase(name));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
