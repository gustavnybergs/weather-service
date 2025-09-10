package com.grupp3.weather.repository;

import com.grupp3.weather.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    // Hitta senaste väderdata för en plats
    @Query("SELECT w FROM WeatherData w WHERE w.placeName = :placeName ORDER BY w.observationTime DESC")
    List<WeatherData> findByPlaceNameOrderByObservationTimeDesc(@Param("placeName") String placeName);

    // Hitta senaste väderdata för en plats (bara den första)
    @Query("SELECT w FROM WeatherData w WHERE w.placeName = :placeName ORDER BY w.observationTime DESC LIMIT 1")
    Optional<WeatherData> findLatestByPlaceName(@Param("placeName") String placeName);

    // Hitta väderdata inom ett tidsintervall
    @Query("SELECT w FROM WeatherData w WHERE w.placeName = :placeName AND w.observationTime BETWEEN :start AND :end ORDER BY w.observationTime DESC")
    List<WeatherData> findByPlaceNameAndObservationTimeBetween(
            @Param("placeName") String placeName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Hitta all data från senaste X timmarna
    @Query("SELECT w FROM WeatherData w WHERE w.observationTime >= :since ORDER BY w.observationTime DESC")
    List<WeatherData> findRecentData(@Param("since") LocalDateTime since);

    // Hitta alla unika platsnamn
    @Query("SELECT DISTINCT w.placeName FROM WeatherData w")
    List<String> findDistinctPlaceNames();
}