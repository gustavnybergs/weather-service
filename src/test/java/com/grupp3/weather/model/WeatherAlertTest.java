package com.grupp3.weather.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class WeatherAlertTest {

    @Test
    @DisplayName("Konstruktor ska skapa alert med korrekta värden")
    void constructor_ShouldCreateAlertWithCorrectValues() {
        // Arrange & Act
        WeatherAlert alert = new WeatherAlert(
                "Kyla", "temperature", "<", 0.0, "high", "Varning för kyla"
        );

        // Assert
        assertThat(alert.getName()).isEqualTo("Kyla");
        assertThat(alert.getAlertType()).isEqualTo("temperature");
        assertThat(alert.getOperator()).isEqualTo("<");
        assertThat(alert.getThresholdValue()).isEqualTo(0.0);
        assertThat(alert.getSeverity()).isEqualTo("high");
        assertThat(alert.getActive()).isTrue();
        assertThat(alert.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("shouldTrigger med < operator ska fungera korrekt")
    void shouldTrigger_WithLessThanOperator_ShouldWorkCorrectly() {
        // Arrange
        WeatherAlert alert = new WeatherAlert("Cold", "temperature", "<", 5.0, "medium", "Cold weather");

        // Act & Assert
        assertThat(alert.shouldTrigger(3.0)).isTrue();   // 3 < 5
        assertThat(alert.shouldTrigger(5.0)).isFalse();  // 5 not < 5
        assertThat(alert.shouldTrigger(7.0)).isFalse();  // 7 not < 5
    }

    @Test
    @DisplayName("shouldTrigger med > operator ska fungera korrekt")
    void shouldTrigger_WithGreaterThanOperator_ShouldWorkCorrectly() {
        // Arrange
        WeatherAlert alert = new WeatherAlert("Hot", "temperature", ">", 25.0, "medium", "Hot weather");

        // Act & Assert
        assertThat(alert.shouldTrigger(30.0)).isTrue();  // 30 > 25
        assertThat(alert.shouldTrigger(25.0)).isFalse(); // 25 not > 25
        assertThat(alert.shouldTrigger(20.0)).isFalse(); // 20 not > 25
    }

    @Test
    @DisplayName("shouldTrigger med = operator ska hantera floating point korrekt")
    void shouldTrigger_WithEqualsOperator_ShouldWorkCorrectly() {
        // Arrange
        WeatherAlert alert = new WeatherAlert("Exact", "temperature", "=", 15.0, "low", "Exact temperature");

        // Act & Assert
        assertThat(alert.shouldTrigger(15.0)).isTrue();   // 15 = 15
        assertThat(alert.shouldTrigger(15.05)).isTrue();  // 15.05 ≈ 15 (within 0.1)
        assertThat(alert.shouldTrigger(14.95)).isTrue();  // 14.95 ≈ 15 (within 0.1)
        assertThat(alert.shouldTrigger(15.2)).isFalse();  // 15.2 not ≈ 15 (outside 0.1)
    }

    @Test
    @DisplayName("shouldTrigger när alert är inaktiv ska returnera false")
    void shouldTrigger_WhenInactive_ShouldReturnFalse() {
        // Arrange
        WeatherAlert alert = new WeatherAlert("Inactive", "temperature", "<", 5.0, "low", "Inactive alert");
        alert.setActive(false);

        // Act & Assert
        assertThat(alert.shouldTrigger(3.0)).isFalse(); // Should trigger if active, but it's inactive
    }

    @Test
    @DisplayName("shouldTrigger med null värde ska returnera false")
    void shouldTrigger_WithNullValue_ShouldReturnFalse() {
        // Arrange
        WeatherAlert alert = new WeatherAlert("Test", "temperature", "<", 5.0, "low", "Test alert");

        // Act & Assert
        assertThat(alert.shouldTrigger(null)).isFalse();
    }

    @Test
    @DisplayName("setActive ska uppdatera timestamp")
    void setActive_ShouldUpdateTimestamp() {
        // Arrange
        WeatherAlert alert = new WeatherAlert();
        LocalDateTime originalTime = alert.getUpdatedAt();

        // Act
        alert.setActive(false);

        // Assert
        assertThat(alert.getActive()).isFalse();
        assertThat(alert.getUpdatedAt()).isNotEqualTo(originalTime);
        assertThat(alert.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Verkligt scenario: Stark vind alert")
    void realScenario_StrongWindAlert() {
        // Arrange - Alert för stark vind över 15 m/s
        WeatherAlert windAlert = new WeatherAlert(
                "Stark vind", "wind_speed", ">", 15.0, "medium", "Varning för stark vind"
        );

        // Act & Assert
        assertThat(windAlert.shouldTrigger(20.5)).isTrue();  // Stark vind - ska trigga
        assertThat(windAlert.shouldTrigger(15.0)).isFalse(); // Exakt på gränsen - ska inte trigga
        assertThat(windAlert.shouldTrigger(10.2)).isFalse(); // Normal vind - ska inte trigga
    }

    @Test
    @DisplayName("Verkligt scenario: Extremkyla alert")
    void realScenario_ExtremeColdAlert() {
        // Arrange - Alert för extremkyla under -20°C
        WeatherAlert coldAlert = new WeatherAlert(
                "Extremkyla", "temperature", "<", -20.0, "critical", "Varning för extremkyla"
        );

        // Act & Assert
        assertThat(coldAlert.shouldTrigger(-25.3)).isTrue();  // Mycket kallt - ska trigga
        assertThat(coldAlert.shouldTrigger(-20.0)).isFalse(); // Exakt på gränsen - ska inte trigga
        assertThat(coldAlert.shouldTrigger(-15.7)).isFalse(); // Kallt men inte extremt - ska inte trigga
    }
}