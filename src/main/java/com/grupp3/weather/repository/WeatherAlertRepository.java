package com.grupp3.weather.repository;

import com.grupp3.weather.model.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    // Hitta alla aktiva alerts
    @Query("SELECT w FROM WeatherAlert w WHERE w.active = true ORDER BY w.severity DESC, w.createdAt DESC")
    List<WeatherAlert> findActiveAlerts();

    // Hitta alerts baserat på typ (temperature, wind_speed, etc.)
    @Query("SELECT w FROM WeatherAlert w WHERE w.alertType = :alertType AND w.active = true")
    List<WeatherAlert> findActiveAlertsByType(@Param("alertType") String alertType);

    // Hitta alerts baserat på severity
    @Query("SELECT w FROM WeatherAlert w WHERE w.severity = :severity AND w.active = true")
    List<WeatherAlert> findActiveAlertsBySeverity(@Param("severity") String severity);

    // Hitta alert baserat på namn (case-insensitive)
    @Query("SELECT w FROM WeatherAlert w WHERE LOWER(w.name) = LOWER(:name)")
    Optional<WeatherAlert> findByNameIgnoreCase(@Param("name") String name);

    // Kolla om alert med samma namn redan finns
    @Query("SELECT COUNT(w) > 0 FROM WeatherAlert w WHERE LOWER(w.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Hitta alerts som ska triggas för specifik vädertyp och värde
    @Query("SELECT w FROM WeatherAlert w WHERE w.alertType = :alertType AND w.active = true")
    List<WeatherAlert> findAlertsToCheck(@Param("alertType") String alertType);

    // Hitta alla aktiva critical alerts
    @Query("SELECT w FROM WeatherAlert w WHERE w.severity = 'critical' AND w.active = true")
    List<WeatherAlert> findCriticalAlerts();

    // Hitta alerts sorterat på severity (critical först)
    @Query("SELECT w FROM WeatherAlert w WHERE w.active = true ORDER BY " +
            "CASE w.severity " +
            "WHEN 'critical' THEN 1 " +
            "WHEN 'high' THEN 2 " +
            "WHEN 'medium' THEN 3 " +
            "WHEN 'low' THEN 4 " +
            "ELSE 5 END, w.createdAt DESC")
    List<WeatherAlert> findActiveAlertsBySeverityOrder();

    // Hitta alla alerts (både aktiva och inaktiva) för admin
    @Query("SELECT w FROM WeatherAlert w ORDER BY w.active DESC, w.createdAt DESC")
    List<WeatherAlert> findAllOrderedForAdmin();

    // Räkna antal aktiva alerts per typ
    @Query("SELECT w.alertType, COUNT(w) FROM WeatherAlert w WHERE w.active = true GROUP BY w.alertType")
    List<Object[]> countActiveAlertsByType();
}