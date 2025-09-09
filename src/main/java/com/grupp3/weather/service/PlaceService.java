package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlaceService {
    // Key = lowercase name för enkel matchning
    private final Map<String, Place> places = new ConcurrentHashMap<>();

    public List<Place> findAll() { return new ArrayList<>(places.values()); }

    public Optional<Place> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(places.get(name.toLowerCase()));
    }

    public boolean exists(String name) { return findByName(name).isPresent(); }

    public Place create(Place p) {
        places.put(p.getName().toLowerCase(), p);
        return p;
    }

    public Optional<Place> update(String name, Place incoming) {
        String key = name.toLowerCase();
        if (!places.containsKey(key)) return Optional.empty();
        Place curr = places.get(key);
        curr.setLat(incoming.getLat());
        curr.setLon(incoming.getLon());
        return Optional.of(curr);
    }

    public boolean delete(String name) {
        return places.remove(name.toLowerCase()) != null;
    }
}
