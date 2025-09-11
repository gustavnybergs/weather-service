package com.grupp3.weather.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // "Extremkyla", "Orkanvarning"

    @Column(name = "alert_type", nullable = false)
    private String alertType; // "temperature", "wind_speed", "precipitation"

    @Column(name = "operator", nullable = false)
    private String operator; // "<", ">", ">=", "<=", "="

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue; // -10.0, 120.0

    @Column(name = "severity", nullable = false)
    private String severity; // "low", "medium", "high", "critical"

    @Column(name = "message", nullable = false, length = 500)
    private String message; // "Varning för extremkyla. Klä dig varmt."

    @Column(name = "active", nullable = false)
    private Boolean active = true; // Om alert är aktiverad

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Konstruktorer
    public WeatherAlert() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public WeatherAlert(String name, String alertType, String operator,
                        Double thresholdValue, String severity, String message) {
        this();
        this.name = name;
        this.alertType = alertType;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.severity = severity;
        this.message = message;
    }

    // Getters och Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Utility method för att kolla om alert ska triggas
    public boolean shouldTrigger(Double actualValue) {
        if (!active || actualValue == null) return false;

        return switch (operator) {
            case "<" -> actualValue < thresholdValue;
            case ">" -> actualValue > thresholdValue;
            case "<=" -> actualValue <= thresholdValue;
            case ">=" -> actualValue >= thresholdValue;
            case "=" -> Math.abs(actualValue - thresholdValue) < 0.1; // Floating point comparison
            default -> false;
        };
    }

    @Override
    public String toString() {
        return String.format("WeatherAlert{id=%d, name='%s', type='%s', condition='%s %s %.1f', severity='%s'}",
                id, name, alertType, alertType, operator, thresholdValue, severity);
    }
}