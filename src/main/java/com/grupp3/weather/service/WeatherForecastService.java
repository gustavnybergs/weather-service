package com.grupp3.weather.service;

import com.grupp3.weather.model.Place;
import com.grupp3.weather.model.WeatherForecast;
import com.grupp3.weather.repository.WeatherForecastRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WeatherForecastService {

    private final WeatherForecastRepository forecastRepository;
    private final WeatherService weatherService;

    public WeatherForecastService(WeatherForecastRepository forecastRepository,
                                  WeatherService weatherService) {
        this.forecastRepository = forecastRepository;
        this.weatherService = weatherService;
    }

    /**
     * Hämta och spara prognosdata för en plats
     */
    public List<WeatherForecast> fetchAndSaveForecast(Place place) {
        try {
            // Hämta rådata från Open-Meteo
            Map<String, Object> rawData = weatherService.fetchForecast(place.getLat(), place.getLon());

            @SuppressWarnings("unchecked")
            Map<String, Object> dailyData = (Map<String, Object>) rawData.get("daily");

            if (dailyData == null) {
                System.err.println("No daily data received for " + place.getName());
                return new ArrayList<>();
            }

            // Extrahera arrays från API response
            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) dailyData.get("time");
            @SuppressWarnings("unchecked")
            List<Number> tempMax = (List<Number>) dailyData.get("temperature_2m_max");
            @SuppressWarnings("unchecked")
            List<Number> tempMin = (List<Number>) dailyData.get("temperature_2m_min");
            @SuppressWarnings("unchecked")
            List<Number> precipitation = (List<Number>) dailyData.get("precipitation_sum");
            @SuppressWarnings("unchecked")
            List<Number> windSpeed = (List<Number>) dailyData.get("wind_speed_10m_max");
            @SuppressWarnings("unchecked")
            List<Number> weatherCodes = (List<Number>) dailyData.get("weather_code");

            List<WeatherForecast> forecasts = new ArrayList<>();

            // Processa varje dag
            for (int i = 0; i < dates.size(); i++) {
                try {
                    LocalDate forecastDate = LocalDate.parse(dates.get(i), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    // Kolla om prognos redan finns för denna dag
                    WeatherForecast forecast = forecastRepository
                            .findByPlaceNameAndForecastDate(place.getName(), forecastDate)
                            .orElse(new WeatherForecast(place.getName(), place.getLat(), place.getLon(), forecastDate));

                    // Uppdatera värden
                    if (tempMax != null && i < tempMax.size() && tempMax.get(i) != null) {
                        forecast.setTemperatureMax(tempMax.get(i).doubleValue());
                    }
                    if (tempMin != null && i < tempMin.size() && tempMin.get(i) != null) {
                        forecast.setTemperatureMin(tempMin.get(i).doubleValue());
                    }
                    if (precipitation != null && i < precipitation.size() && precipitation.get(i) != null) {
                        forecast.setPrecipitationSum(precipitation.get(i).doubleValue());
                    }
                    if (windSpeed != null && i < windSpeed.size() && windSpeed.get(i) != null) {
                        forecast.setWindSpeedMax(windSpeed.get(i).doubleValue());
                    }
                    if (weatherCodes != null && i < weatherCodes.size() && weatherCodes.get(i) != null) {
                        forecast.setWeatherCode(weatherCodes.get(i).intValue());
                    }

                    WeatherForecast saved = forecastRepository.save(forecast);
                    forecasts.add(saved);

                } catch (Exception e) {
                    System.err.println("Error processing forecast day " + i + " for " + place.getName() + ": " + e.getMessage());
                }
            }

            return forecasts;

        } catch (Exception e) {
            System.err.println("Error fetching forecast for " + place.getName() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Hämta prognoser för en plats
     */
    public List<WeatherForecast> getForecastsForPlace(String placeName) {
        return forecastRepository.findFutureForecasts(placeName, LocalDate.now());
    }

    /**
     * Hämta prognoser för kommande X dagar
     */
    public List<WeatherForecast> getForecastsForNextDays(String placeName, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days - 1);
        return forecastRepository.findForecastsForNextDays(placeName, today, endDate);
    }

    /**
     * Rensa gamla prognoser (körs periodiskt)
     */
    @Transactional
    public void cleanupOldForecasts() {
        forecastRepository.deleteOldForecasts(LocalDate.now());
    }

    /**
     * Hämta prognos för specifikt datum
     */
    public WeatherForecast getForecastForDate(String placeName, LocalDate date) {
        return forecastRepository.findByPlaceNameAndForecastDate(placeName, date).orElse(null);
    }
}