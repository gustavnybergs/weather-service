package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import com.grupp3.weather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final PlaceService placeService;
    private final WeatherService weatherService;

    public WeatherController(PlaceService placeService, WeatherService weatherService) {
        this.placeService = placeService;
        this.weatherService = weatherService;
    }

    @GetMapping("/{placeName}")
    public ResponseEntity<?> current(@PathVariable String placeName) {
        // 404 NOT_FOUND och fel meddelande att plats inte hittades
        Place p = placeService.findByName(placeName).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Place '" + placeName + "' does not exist. Try again"
                ));

        // Hämta "nu-väder" från Open-Meteo
        Map<String,Object> raw = weatherService.fetchCurrent(p.getLat(), p.getLon());

        // Forma ett litet, stabilt svar (kan byggas ut senare)
        Map<String,Object> out = Map.of(
            "place", Map.of("name", p.getName(), "lat", p.getLat(), "lon", p.getLon()),
            "source", "open-meteo",
            "data", raw.get("current")  // Open-Meteo lägger current-värden under "current"
        );
        return ResponseEntity.ok(out);
    }
}
