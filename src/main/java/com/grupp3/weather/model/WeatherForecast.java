package com.grupp3.weather.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_forecast")
public class WeatherForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "precipitation_sum")
    private Double precipitationSum;

    @Column(name = "wind_speed_max")
    private Double windSpeedMax;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_source")
    private String dataSource = "open-meteo";

    // Konstruktorer
    public WeatherForecast() {
        this.createdAt = LocalDateTime.now();
    }

    public WeatherForecast(String placeName, Double latitude, Double longitude,
                           LocalDate forecastDate) {
        this();
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecastDate = forecastDate;
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

    public LocalDate getForecastDate() { return forecastDate; }
    public void setForecastDate(LocalDate forecastDate) { this.forecastDate = forecastDate; }

    public Double getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(Double temperatureMax) { this.temperatureMax = temperatureMax; }

    public Double getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(Double temperatureMin) { this.temperatureMin = temperatureMin; }

    public Double getPrecipitationSum() { return precipitationSum; }
    public void setPrecipitationSum(Double precipitationSum) { this.precipitationSum = precipitationSum; }

    public Double getWindSpeedMax() { return windSpeedMax; }
    public void setWindSpeedMax(Double windSpeedMax) { this.windSpeedMax = windSpeedMax; }

    public Integer getWeatherCode() { return weatherCode; }
    public void setWeatherCode(Integer weatherCode) { this.weatherCode = weatherCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}