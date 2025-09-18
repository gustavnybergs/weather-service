package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PlaceService - business logic-koordinator för Place-entiteter.
 *
 * Sitter mellan controllers och repository som "smart mellanlager".
 * Implementerar affärsregler, validering och säkerhetslogik innan databas-access.
 *
 * Affärslogik inkluderar:
 * - Input-validering: null-skydd förhindrar systemkrasch vid felaktiga API-anrop
 * - Säker radering: kontrollerar existens först, ger tydlig feedback om resultat
 * - Immutable names: platsnamn kan aldrig ändras för att bevara datakonsistens
 * - State-persistence: favoritändringar sparas omedelbart för att undvika dataförlust
 *
 * @Transactional säkerställer databas-rollback vid fel.
 * Används av controllers för CRUD och favorithantering.
 */

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

    // Nya koordinatbaserade metoder
    public Optional<Place> findByCoordinates(double lat, double lon) {
        return placeRepository.findByCoordinates(lat, lon);
    }

    public boolean existsByCoordinates(double lat, double lon) {
        return placeRepository.existsByCoordinates(lat, lon);
    }

    public Optional<Place> setFavoriteByCoordinates(double lat, double lon, boolean favorite) {
        return placeRepository.findByCoordinates(lat, lon)
                .map(place -> {
                    place.setFavorite(favorite);
                    return placeRepository.save(place);
                });
    }

    @Transactional
    public boolean deleteByCoordinates(double lat, double lon) {
        if (placeRepository.existsByCoordinates(lat, lon)) {
            placeRepository.deleteByCoordinates(lat, lon);
            return true;
        }
        return false;
    }

    // Bakåtkompatibla metoder för befintlig kod
    public Optional<Place> findByName(String name) {
        return placeRepository.findByNameIgnoreCase(name);
    }

    public boolean exists(String name) {
        if (name == null) return false;
        return placeRepository.existsByNameIgnoreCase(name);
    }

    public Optional<Place> setFavorite(String name, boolean favorite) {
        return placeRepository.findByNameIgnoreCase(name)
                .map(place -> {
                    place.setFavorite(favorite);
                    return placeRepository.save(place);
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

    // Gemensamma metoder
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

    public List<Place> findFavorites() {
        return placeRepository.findFavorites();
    }
}