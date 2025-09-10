package com.grupp3.weather.controller;

import com.grupp3.weather.model.WeatherAlert;
import com.grupp3.weather.repository.WeatherAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final WeatherAlertRepository alertRepository;

    public AlertController(WeatherAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Lista alla aktiva alert-definitioner (readonly för users)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getActiveAlerts() {
        List<WeatherAlert> activeAlerts = alertRepository.findActiveAlerts();

        Map<String, Object> response = Map.of(
                "total_alerts", activeAlerts.size(),
                "alerts", activeAlerts,
                "info", "These alerts are monitored for your favorite places"
        );

        return ResponseEntity.ok(response);
    }
}