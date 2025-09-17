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

    // Hitta plats efter namn (case-insensitive)
    @Query("SELECT p FROM Place p WHERE LOWER(p.name) = LOWER(:name)")
    Optional<Place> findByNameIgnoreCase(@Param("name") String name);

    // Kolla om plats finns (case-insensitive)
    @Query("SELECT COUNT(p) > 0 FROM Place p WHERE LOWER(p.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Ta bort plats efter namn (case-insensitive)
    @Query("DELETE FROM Place p WHERE LOWER(p.name) = LOWER(:name)")
    void deleteByNameIgnoreCase(@Param("name") String name);

    // Hitta alla favoritplatser
    @Query("SELECT p FROM Place p WHERE p.favorite = true")
    List<Place> findFavorites();
}