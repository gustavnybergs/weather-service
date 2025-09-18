package com.grupp3.weather.model;

import jakarta.persistence.*;

/**
 * Place-klassen är en mall/blueprint som definierar vad varje place-objekt ska innehålla.
 *
 * Klassen bestämmer strukturen: "Varje place-objekt MÅSTE ha id, name, lat, lon, favorite"
 * Objekten innehåller den faktiska datan: "Stockholm", 59.3293, 18.0686, true
 *
 */

@Entity
@Table(name = "places",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lat", "lon"}))
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;  // Vad användaren sökte på (t.ex. "göteborg")

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    // Default constructor (required by JPA)
    public Place() {}

    public Place(String displayName, double lat, double lon) {
        this.displayName = displayName;
        this.lat = lat;
        this.lon = lon;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    // Behåll getName() för bakåtkompatibilitet
    public String getName() { return displayName; }
    public void setName(String name) { this.displayName = name; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    @Override
    public String toString() {
        return String.format("Place{id=%d, displayName='%s', lat=%.5f, lon=%.5f, favorite=%s}",
                id, displayName, lat, lon, favorite);
    }
}