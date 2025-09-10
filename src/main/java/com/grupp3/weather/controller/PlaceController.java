package com.grupp3.weather.controller;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.service.PlaceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /**
     * GET: Hämta alla tillgängliga platser
     * Users kan se vilka platser som finns för att markera som favoriter
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Place> all() {
        return placeService.findAll();
    }
}