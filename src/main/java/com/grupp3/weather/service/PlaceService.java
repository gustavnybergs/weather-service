package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> findAll() {
        return placeRepository.findAll();
    }

    public Optional<Place> findByName(String name) {
        if (name == null) return Optional.empty();
        return placeRepository.findByNameIgnoreCase(name);
    }

    public boolean exists(String name) {
        if (name == null) return false;
        return placeRepository.existsByNameIgnoreCase(name);
    }

    public Place create(Place place) {
        return placeRepository.save(place);
    }

    public Optional<Place> update(String name, Place incoming) {
        return placeRepository.findByNameIgnoreCase(name)
                .map(existing -> {
                    existing.setLat(incoming.getLat());
                    existing.setLon(incoming.getLon());
                    return placeRepository.save(existing);
                });
    }

    @Transactional
    public boolean delete(String name) {
        if (placeRepository.existsByNameIgnoreCase(name)) {
            placeRepository.deleteByNameIgnoreCase(name);
            return true;
        }
        return false;
    }

    public Optional<Place> setFavorite(String name, boolean favorite) {
        return placeRepository.findByNameIgnoreCase(name)
                .map(place -> {
                    place.setFavorite(favorite);
                    return placeRepository.save(place);
                });
    }

    public List<Place> findFavorites() {
        return placeRepository.findFavorites();
    }
}