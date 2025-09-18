package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.model.WeatherData;
import com.grupp3.weather.model.WeatherForecast;
import com.grupp3.weather.model.WeatherAlert;
import com.grupp3.weather.repository.WeatherAlertRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ScheduledWeatherService - systemdirigent för automatisk väderdata-uppdatering och alert-hantering.
 *
 * Skiljer sig från andra services genom att köra automatiska bakgrundsprocesser
 * istället för att reagera på användarförfrågningar.
 *
 * Huvudfunktioner:
 * - updateWeatherForAllPlaces(): Automatisk uppdatering var 30:e minut för ENDAST favoriter
 * - checkAlertsForAllPlaces(): Kontrollera väderalerts mot aktuell data efter uppdatering
 * - cleanupOldData(): Daglig rensning kl 02:00 av gamla prognoser och buckets
 * - triggerManualUpdate(): Admin-triggered omedelbar uppdatering via endpoint
 *
 * Batch-processing implementerar:
 * - Per-plats error isolation: Ett API-fel stoppar inte uppdatering av andra platser
 * - API-anrops frekvens: 2 anrop per favoritplats (current + forecast) var 30:e minut
 * - Paus mellan platser: 1 sekund delay för att inte överbelasta Open-Meteo API
 * - Detaljerad loggning: Framgång/fel-statistik per körning för diagnostik
 *
 * Alert-system koordinerar WeatherAlert-definitioner mot faktisk väderdata.
 * Använder PlaceService, WeatherHistoryService, WeatherForecastService som dependencies.
 * Enda klassen som automatiskt uppdaterar favoritplatser - andra services är reaktiva.
 */

@Service
public class ScheduledWeatherService {

    // === SCHEDULING CONSTANTS ===
    private static final int WEATHER_UPDATE_INTERVAL_MS = 30 * 60 * 1000;  // fixedRate
    private static final int API_CALL_DELAY_MS = 1000;                     // Thread.sleep
    private static final int STARTUP_DELAY_MS = 30000;                     // initialDelay

    // === ALERT THRESHOLDS ===
    private final PlaceService placeService;
    private final WeatherHistoryService weatherHistoryService;
    private final WeatherForecastService forecastService;
    private final RateLimitingService rateLimitingService;
    private final WeatherAlertRepository alertRepository;

    public ScheduledWeatherService(PlaceService placeService,
                                   WeatherHistoryService weatherHistoryService,
                                   WeatherForecastService forecastService,
                                   RateLimitingService rateLimitingService,
                                   WeatherAlertRepository alertRepository) {
        this.placeService = placeService;
        this.weatherHistoryService = weatherHistoryService;
        this.forecastService = forecastService;
        this.rateLimitingService = rateLimitingService;
        this.alertRepository = alertRepository;
    }

    /**
     * Schemalagd uppdatering var 30:e minut
     * Hämtar både aktuellt väder och prognoser för alla registrerade platser
     */
    @Scheduled(fixedRate = WEATHER_UPDATE_INTERVAL_MS) // 30 minuter i millisekunder
    public void updateWeatherForAllPlaces() {
        List<Place> favoritePlaces = placeService.findFavorites();

        if (favoritePlaces.isEmpty()) {
            System.out.println("[" + LocalDateTime.now() + "] No places registered - skipping scheduled weather update");
            return;
        }

        System.out.println("[" + LocalDateTime.now() + "] Starting scheduled weather update for " + favoritePlaces.size() + " places");

        int currentSuccessCount = 0;
        int forecastSuccessCount = 0;
        int errorCount = 0;

        // Uppdatera väderdata för alla platser
        for (Place place : favoritePlaces) {
            try {
                // Uppdatera current weather
                WeatherData currentData = weatherHistoryService.fetchAndSaveWeatherData(place);
                if (currentData != null) {
                    currentSuccessCount++;
                    System.out.println("✓ Updated current weather for " + place.getName() +
                            " - Temp: " + currentData.getTemperature() + "°C");
                } else {
                    errorCount++;
                    System.err.println("✗ Failed to update current weather for " + place.getName());
                }

                Thread.sleep(API_CALL_DELAY_MS);

                // Uppdatera prognoser
                List<WeatherForecast> forecasts = forecastService.fetchAndSaveForecast(place);
                if (!forecasts.isEmpty()) {
                    forecastSuccessCount++;
                    System.out.println("✓ Updated forecast for " + place.getName() +
                            " - " + forecasts.size() + " days");
                } else {
                    errorCount++;
                    System.err.println("✗ Failed to update forecast for " + place.getName());
                }

                Thread.sleep(API_CALL_DELAY_MS);

            } catch (Exception e) {
                errorCount++;
                System.err.println("✗ Error updating weather for " + place.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("[" + LocalDateTime.now() + "] Weather update completed - " +
                "Current: " + currentSuccessCount + " successful, " +
                "Forecast: " + forecastSuccessCount + " successful, " +
                errorCount + " errors");

        // Kolla alerts för alla platser med uppdaterad data
        checkAlertsForAllPlaces();
    }

    /**
     * Kolla alerts för alla platser
     */
    private void checkAlertsForAllPlaces() {
        List<WeatherAlert> activeAlerts = alertRepository.findActiveAlerts();

        if (activeAlerts.isEmpty()) {
            return;
        }

        List<Place> favoritePlaces = placeService.findFavorites();
        int alertsTriggered = 0;

        System.out.println("[" + LocalDateTime.now() + "] Checking " + activeAlerts.size() +
                " active alerts for " + favoritePlaces.size() + " places");

        for (Place place : favoritePlaces) {
            // Hämta senaste väderdata för platsen
            WeatherData latestWeather = weatherHistoryService.getLatestWeatherData(place.getName());

            if (latestWeather == null) {
                continue; // Ingen väderdata för denna plats
            }

            // Kolla varje alert mot denna plats
            for (WeatherAlert alert : activeAlerts) {
                if (checkSingleAlert(alert, latestWeather, place)) {
                    alertsTriggered++;
                }
            }
        }

        if (alertsTriggered > 0) {
            System.out.println("[" + LocalDateTime.now() + "] Triggered " + alertsTriggered + " alerts");
        }
    }

    /**
     * Kolla en specifik alert mot väderdata för en plats
     */
    private boolean checkSingleAlert(WeatherAlert alert, WeatherData weatherData, Place place) {
        Double valueToCheck = null;

        // Hämta rätt värde baserat på alert-typ
        switch (alert.getAlertType()) {
            case "temperature":
                valueToCheck = weatherData.getTemperature();
                break;
            case "wind_speed":
                valueToCheck = weatherData.getWindSpeed();
                break;
            case "cloud_cover":
                valueToCheck = weatherData.getCloudCover() != null ? weatherData.getCloudCover().doubleValue() : null;
                break;
            case "precipitation":
                // För precipitation behöver vi kanske kolla forecast-data istället
                // Men för nu skippar vi detta
                return false;
            default:
                return false;
        }

        if (valueToCheck == null) {
            return false; // Ingen data att kolla mot
        }

        // Använd alert's shouldTrigger-metod
        if (alert.shouldTrigger(valueToCheck)) {
            // Alert triggad! Logga detta
            System.out.println(String.format("[ALERT] %s triggered for %s: %s %.1f %s %.1f (%s)",
                    alert.getName(),
                    place.getName(),
                    alert.getAlertType(),
                    valueToCheck,
                    alert.getOperator(),
                    alert.getThresholdValue(),
                    alert.getSeverity()));

            // Här skulle ni kunna skicka notifikationer, emails, etc.
            logTriggeredAlert(alert, place, valueToCheck);

            return true;
        }

        return false;
    }

    /**
     * Logga triggad alert (kan utökas till notifikationer senare)
     */
    private void logTriggeredAlert(WeatherAlert alert, Place place, Double actualValue) {
        String logMessage = String.format("[%s] ALERT: %s for %s (Actual: %.1f, Threshold: %s %.1f) - %s",
                LocalDateTime.now(),
                alert.getName(),
                place.getName(),
                actualValue,
                alert.getOperator(),
                alert.getThresholdValue(),
                alert.getMessage());

        System.out.println(logMessage);

        // I framtiden: spara till alert_log tabell, skicka email, push notification, etc.
    }

    /**
     * Rensa gamla prognoser en gång per dag
     */
    @Scheduled(cron = "0 0 2 * * *") // Kl 02:00 varje dag
    public void cleanupOldData() {
        System.out.println("[" + LocalDateTime.now() + "] Starting daily cleanup...");

        try {
            forecastService.cleanupOldForecasts();
            System.out.println("✓ Old forecasts cleaned up");
        } catch (Exception e) {
            System.err.println("✗ Error cleaning up old forecasts: " + e.getMessage());
        }

        // Rensa rate limit buckets
        try {
            rateLimitingService.cleanupOldBuckets();
            System.out.println("✓ Rate limit buckets cleaned up");
        } catch (Exception e) {
            System.err.println("✗ Error cleaning up rate limit buckets: " + e.getMessage());
        }

        System.out.println("[" + LocalDateTime.now() + "] Daily cleanup completed");
    }

    /**
     * Startup-metod som kör en gång när applikationen startar
     */
    @Scheduled(initialDelay = STARTUP_DELAY_MS, fixedRate = Long.MAX_VALUE)
    public void initialWeatherUpdate() {
        System.out.println("[" + LocalDateTime.now() + "] Running initial weather and forecast update...");
        updateWeatherForAllPlaces();
    }

    /**
     * Manual trigger för att tvinga en komplett uppdatering
     */
    public void triggerManualUpdate() {
        System.out.println("[" + LocalDateTime.now() + "] Manual weather and forecast update triggered");
        updateWeatherForAllPlaces();
    }
}