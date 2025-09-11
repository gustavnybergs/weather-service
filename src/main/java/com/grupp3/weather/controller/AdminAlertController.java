package com.grupp3.weather.controller;

import com.grupp3.weather.model.WeatherAlert;
import com.grupp3.weather.repository.WeatherAlertRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/alerts")
public class AdminAlertController {

    private final WeatherAlertRepository alertRepository;
    private static final String API_KEY = "topsecret123";

    public AdminAlertController(WeatherAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * CREATE - Skapa ny väderalert
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAlert(@RequestBody WeatherAlert alert,
                                                           @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        // Kontrollera API-nyckel
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validera input
        String validationError = validateAlert(alert);
        if (validationError != null) {
            Map<String, Object> error = Map.of("error", validationError);
            return ResponseEntity.badRequest().body(error);
        }

        // Kolla om alert med samma namn redan finns
        if (alertRepository.existsByNameIgnoreCase(alert.getName())) {
            Map<String, Object> error = Map.of("error", "Alert with name '" + alert.getName() + "' already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Spara alert
        WeatherAlert savedAlert = alertRepository.save(alert);

        Map<String, Object> response = Map.of(
                "message", "Alert created successfully",
                "alert", savedAlert
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * READ - Hämta alla alerts (admin view)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAlerts(@RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WeatherAlert> alerts = alertRepository.findAllOrderedForAdmin();

        // Räkna aktiva vs inaktiva
        long activeCount = alerts.stream().filter(WeatherAlert::getActive).count();
        long inactiveCount = alerts.size() - activeCount;

        Map<String, Object> response = Map.of(
                "total_alerts", alerts.size(),
                "active_alerts", activeCount,
                "inactive_alerts", inactiveCount,
                "alerts", alerts
        );

        return ResponseEntity.ok(response);
    }

    /**
     * READ - Hämta specifik alert
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAlert(@PathVariable Long id,
                                                        @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<WeatherAlert> alert = alertRepository.findById(id);
        if (alert.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = Map.of("alert", alert.get());
        return ResponseEntity.ok(response);
    }

    /**
     * UPDATE - Uppdatera befintlig alert
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAlert(@PathVariable Long id,
                                                           @RequestBody WeatherAlert updatedAlert,
                                                           @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Hitta befintlig alert
        Optional<WeatherAlert> existingAlertOpt = alertRepository.findById(id);
        if (existingAlertOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WeatherAlert existingAlert = existingAlertOpt.get();

        // Validera input
        String validationError = validateAlert(updatedAlert);
        if (validationError != null) {
            Map<String, Object> error = Map.of("error", validationError);
            return ResponseEntity.badRequest().body(error);
        }

        // Kolla namn-konflikt (bara om namnet ändras)
        if (!existingAlert.getName().equalsIgnoreCase(updatedAlert.getName()) &&
                alertRepository.existsByNameIgnoreCase(updatedAlert.getName())) {
            Map<String, Object> error = Map.of("error", "Alert with name '" + updatedAlert.getName() + "' already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Uppdatera fält
        existingAlert.setName(updatedAlert.getName());
        existingAlert.setAlertType(updatedAlert.getAlertType());
        existingAlert.setOperator(updatedAlert.getOperator());
        existingAlert.setThresholdValue(updatedAlert.getThresholdValue());
        existingAlert.setSeverity(updatedAlert.getSeverity());
        existingAlert.setMessage(updatedAlert.getMessage());
        existingAlert.setActive(updatedAlert.getActive());
        existingAlert.setUpdatedAt(LocalDateTime.now());

        WeatherAlert savedAlert = alertRepository.save(existingAlert);

        Map<String, Object> response = Map.of(
                "message", "Alert updated successfully",
                "alert", savedAlert
        );

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE - Ta bort alert
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAlert(@PathVariable Long id,
                                                           @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {

        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<WeatherAlert> alert = alertRepository.findById(id);
        if (alert.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        alertRepository.deleteById(id);

        Map<String, Object> response = Map.of(
                "message", "Alert deleted successfully",
                "deleted_alert_name", alert.get().getName()
        );

        return ResponseEntity.ok(response);
    }



    /**
     * Validera alert input
     */
    private String validateAlert(WeatherAlert alert) {
        if (alert.getName() == null || alert.getName().trim().isEmpty()) {
            return "Alert name is required";
        }

        if (alert.getAlertType() == null || alert.getAlertType().trim().isEmpty()) {
            return "Alert type is required";
        }

        // Validera alert type
        String[] validTypes = {"temperature", "wind_speed", "precipitation", "cloud_cover"};
        boolean validType = false;
        for (String type : validTypes) {
            if (type.equals(alert.getAlertType())) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            return "Invalid alert type. Must be one of: temperature, wind_speed, precipitation, cloud_cover";
        }

        if (alert.getOperator() == null || alert.getOperator().trim().isEmpty()) {
            return "Operator is required";
        }

        // Validera operator
        String[] validOperators = {"<", ">", "<=", ">=", "="};
        boolean validOperator = false;
        for (String op : validOperators) {
            if (op.equals(alert.getOperator())) {
                validOperator = true;
                break;
            }
        }
        if (!validOperator) {
            return "Invalid operator. Must be one of: <, >, <=, >=, =";
        }

        if (alert.getThresholdValue() == null) {
            return "Threshold value is required";
        }

        if (alert.getSeverity() == null || alert.getSeverity().trim().isEmpty()) {
            return "Severity is required";
        }

        // Validera severity
        String[] validSeverities = {"low", "medium", "high", "critical"};
        boolean validSeverity = false;
        for (String severity : validSeverities) {
            if (severity.equals(alert.getSeverity())) {
                validSeverity = true;
                break;
            }
        }
        if (!validSeverity) {
            return "Invalid severity. Must be one of: low, medium, high, critical";
        }

        if (alert.getMessage() == null || alert.getMessage().trim().isEmpty()) {
            return "Alert message is required";
        }

        return null; // Ingen fel
    }
}