package com.grupp3.weather.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * WeatherData-klassen är en mall för tidsstämplade väderrapporter.
 *
 * Varje objekt är en ögonblicksbild av vädret vid en specifik tidpunkt
 * NYA objekt skapas var 30:e minut
 * Kopplas till Place via placeName string, inte databas-relation
 * Varje WeatherData-objekt har ett placeName fält som innehåller samma namn som Place-objektet:
 *
 * Exempel: 3 WeatherData-objekt för "Stockholm" med olika tider och temperaturer:
 *
 * WeatherData kl10 = new WeatherData("Stockholm", 59.3293, 18.0686, 15.0, ...);
 * WeatherData kl1030 = new WeatherData("Stockholm", 59.3293, 18.0686, 16.0, ...);
 * WeatherData kl11 = new WeatherData("Stockholm", 59.3293, 18.0686, 14.0, ...);
 *
 * Bygger upp historisk data för trendanalys och alert-system
 */

@Entity
@Table(name = "weather_data")
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "cloud_cover")
    private Integer cloudCover;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "observation_time", nullable = false)
    private LocalDateTime observationTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_source")
    private String dataSource = "open-meteo";

    // Konstruktorer
    public WeatherData() {
        this.createdAt = LocalDateTime.now();
    }

    public WeatherData(String placeName, Double latitude, Double longitude,
                       Double temperature, Integer cloudCover, Double windSpeed,
                       LocalDateTime observationTime) {
        this();
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.cloudCover = cloudCover;
        this.windSpeed = windSpeed;
        this.observationTime = observationTime;
    }

    // Getters och Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getCloudCover() { return cloudCover; }
    public void setCloudCover(Integer cloudCover) { this.cloudCover = cloudCover; }

    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public LocalDateTime getObservationTime() { return observationTime; }
    public void setObservationTime(LocalDateTime observationTime) { this.observationTime = observationTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}