package com.grupp3.weather.repository;

import com.grupp3.weather.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PlaceRepository - databas-gateway för Place-entiteter.
 *
 * Interface som Spring automatiskt implementerar med CRUD + custom queries.
 * Används av flera delar: PlaceService, ScheduledWeatherService, Controllers.
 *
 * Custom metoder löser specifika systemkrav:
 * - findByNameIgnoreCase(): Användarsökning ("stockholm" = "Stockholm")
 * - existsByNameIgnoreCase(): Validering innan API-anrop för att hålla nere API anrop
 * - findFavorites(): Hämtar platser för schemalagda väderuppdateringar
 * - deleteByNameIgnoreCase(): Admin-cleanup av oanvända platser
 *
 * Spring genererar SQL automatiskt från metodnamn och @Query annotations.
 */


@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // Hitta plats efter koordinater (huvudsättet nu)
    @Query("SELECT p FROM Place p WHERE ABS(p.lat - :lat) < 0.001 AND ABS(p.lon - :lon) < 0.001")
    Optional<Place> findByCoordinates(@Param("lat") double lat, @Param("lon") double lon);

    // Kolla om plats finns efter koordinater
    @Query("SELECT COUNT(p) > 0 FROM Place p WHERE ABS(p.lat - :lat) < 0.001 AND ABS(p.lon - :lon) < 0.001")
    boolean existsByCoordinates(@Param("lat") double lat, @Param("lon") double lon);

    // Ta bort plats efter koordinater
    @Query("DELETE FROM Place p WHERE ABS(p.lat - :lat) < 0.001 AND ABS(p.lon - :lon) < 0.001")
    void deleteByCoordinates(@Param("lat") double lat, @Param("lon") double lon);

    // Hitta alla favoritplatser
    @Query("SELECT p FROM Place p WHERE p.favorite = true")
    List<Place> findFavorites();

    // Bakåtkompatibilitet - hitta efter displayName (för befintliga platser)
    @Query("SELECT p FROM Place p WHERE LOWER(p.displayName) = LOWER(:name)")
    Optional<Place> findByDisplayName(@Param("name") String name);

    // Bakåtkompatibilitet, för befintlig kod som använder namn
    @Query("SELECT p FROM Place p WHERE LOWER(p.displayName) = LOWER(:name)")
    Optional<Place> findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT COUNT(p) > 0 FROM Place p WHERE LOWER(p.displayName) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("DELETE FROM Place p WHERE LOWER(p.displayName) = LOWER(:name)")
    void deleteByNameIgnoreCase(@Param("name") String name);
}