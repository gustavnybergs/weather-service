package com.grupp3.weather.repository;

import com.grupp3.weather.model.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * WeatherForecastRepository - datumbaserad prognoshämtare för framtida väderdata.
 *
 * Fokuserar på LocalDate (bara datum) istället för LocalDateTime (exakt tid).
 * Prognoser är per dag och byts ut när nya hämtas, till skillnad från WeatherData som samlas historiskt.
 *
 * Huvudanvändning:
 * - findByPlaceNameAndForecastDate(): Specifik dag ("Hur blir fredagen?")
 * - findForecastsForNextDays(): X-dagars prognos framåt i kronologisk ordning
 * - deleteOldForecasts(): Rensning av gårdagens prognoser (ej historisk data)
 *
 * Används av ForecastController och ScheduledWeatherService för att visa/uppdatera
 * 7-dagars prognoser från Open-Meteo API.
 */

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {

    // Hitta prognoser för en plats sorterat på datum
    @Query("SELECT w FROM WeatherForecast w WHERE w.placeName = :placeName ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findByPlaceNameOrderByForecastDate(@Param("placeName") String placeName);

    // Hitta prognos för specifik plats och datum
    @Query("SELECT w FROM WeatherForecast w WHERE w.placeName = :placeName AND w.forecastDate = :date")
    Optional<WeatherForecast> findByPlaceNameAndForecastDate(@Param("placeName") String placeName,
                                                             @Param("date") LocalDate date);

    // Hitta prognoser inom ett datumintervall
    @Query("SELECT w FROM WeatherForecast w WHERE w.placeName = :placeName AND w.forecastDate BETWEEN :startDate AND :endDate ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findByPlaceNameAndForecastDateBetween(@Param("placeName") String placeName,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    // Ta bort gamla prognoser (äldre än idag)
    @Query("DELETE FROM WeatherForecast w WHERE w.forecastDate < :today")
    void deleteOldForecasts(@Param("today") LocalDate today);

    // Hitta alla unika platsnamn som har prognoser
    @Query("SELECT DISTINCT w.placeName FROM WeatherForecast w")
    List<String> findDistinctPlaceNames();

    // Hitta framtida prognoser för en plats (från idag och framåt)
    @Query("SELECT w FROM WeatherForecast w WHERE w.placeName = :placeName AND w.forecastDate >= :today ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findFutureForecasts(@Param("placeName") String placeName,
                                              @Param("today") LocalDate today);

    // Hitta prognoser för kommande X dagar
    @Query("SELECT w FROM WeatherForecast w WHERE w.placeName = :placeName AND w.forecastDate BETWEEN :today AND :endDate ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findForecastsForNextDays(@Param("placeName") String placeName,
                                                   @Param("today") LocalDate today,
                                                   @Param("endDate") LocalDate endDate);
}