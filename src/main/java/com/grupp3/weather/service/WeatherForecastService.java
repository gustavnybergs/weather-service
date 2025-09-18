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
import java.util.function.Consumer;

/**
 * WeatherForecastService - databas-coordinator för 7-dagars väderprognos lagring.
 *
 * Skiljer sig från WeatherHistoryService genom att hantera framtida väderdata (prognoser)
 * istället för aktuell/historisk data från Open-Meteo API.
 *
 * Huvudfunktioner:
 * - fetchAndSaveForecast(Place place): Hämta prognos → array-processing → spara 7 dagar
 * - getForecastsForPlace(String placeName): Alla framtida prognoser från idag och framåt
 * - getForecastsForNextDays(String placeName, int days): Flexibel dagsmängd (1-14 dagar)
 * - getForecastForDate(String placeName, LocalDate date): Specifik datums-prognos
 * - cleanupOldForecasts(): Radera inaktuella prognoser äldre än idag
 *
 * Array-processing implementerar:
 * - Parallell array-hantering: Open-Meteo returnerar separata arrays per värdetyp
 * - Index-korrelation: Loop kombinerar time[i] + temp_max[i] + temp_min[i] till komplett dag-objekt
 * - Upsert-logik: findByPlaceNameAndForecastDate() → uppdatera befintlig eller skapa ny
 * - Datum-konvertering: "2025-09-17" string → LocalDate för databas-kompatibilitet
 *
 * Används av ForecastController och ScheduledWeatherService för prognoshantering.
 * Databas-lagring säkerställer en post per plats+datum utan duplicering.
 */

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
     * Inner class för att organisera Open-Meteo forecast arrays
     */
    private static class ForecastArrays {
        private final List<String> dates;
        private final List<Number> tempMax;
        private final List<Number> tempMin;
        private final List<Number> precipitation;
        private final List<Number> windSpeed;
        private final List<Number> weatherCodes;

        public ForecastArrays(List<String> dates, List<Number> tempMax, List<Number> tempMin,
                              List<Number> precipitation, List<Number> windSpeed, List<Number> weatherCodes) {
            this.dates = dates;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.precipitation = precipitation;
            this.windSpeed = windSpeed;
            this.weatherCodes = weatherCodes;
        }

        // Getters
        public List<String> getDates() { return dates; }
        public List<Number> getTempMax() { return tempMax; }
        public List<Number> getTempMin() { return tempMin; }
        public List<Number> getPrecipitation() { return precipitation; }
        public List<Number> getWindSpeed() { return windSpeed; }
        public List<Number> getWeatherCodes() { return weatherCodes; }
    }

    /**
     * Helper-metod för säker värde-sättning från Open-Meteo arrays
     */
    private void safeSetForecastValue(List<Number> dataArray, int index,
                                      Consumer<Double> setter) {
        if (dataArray != null && index < dataArray.size() && dataArray.get(index) != null) {
            setter.accept(dataArray.get(index).doubleValue());
        }
    }

    /**
     * Helper-metod för säker integer värde-sättning från Open-Meteo arrays
     */
    private void safeSetIntegerValue(List<Number> dataArray, int index,
                                     Consumer<Integer> setter) {
        if (dataArray != null && index < dataArray.size() && dataArray.get(index) != null) {
            setter.accept(dataArray.get(index).intValue());
        }
    }

    /**
     * Hämta och spara prognosdata för en plats
     */
    public List<WeatherForecast> fetchAndSaveForecast(Place place) {
        try {
            Map<String, Object> rawData = fetchRawForecastData(place);
            ForecastArrays arrays = parseApiResponse(rawData, place.getName());
            return processAndSaveForecasts(place, arrays);
        } catch (Exception e) {
            System.err.println("Error fetching forecast for " + place.getName() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Hämta rådata från Open-Meteo
     */
    private Map<String, Object> fetchRawForecastData(Place place) {
        return weatherService.fetchForecast(place.getLat(), place.getLon());
    }

    /**
     * Extrahera arrays från API response
     */
    @SuppressWarnings("unchecked")
    private ForecastArrays parseApiResponse(Map<String, Object> rawData, String placeName) {
        Map<String, Object> dailyData = (Map<String, Object>) rawData.get("daily");

        if (dailyData == null) {
            System.err.println("No daily data received for " + placeName);
            return new ForecastArrays(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        List<String> dates = (List<String>) dailyData.get("time");
        List<Number> tempMax = (List<Number>) dailyData.get("temperature_2m_max");
        List<Number> tempMin = (List<Number>) dailyData.get("temperature_2m_min");
        List<Number> precipitation = (List<Number>) dailyData.get("precipitation_sum");
        List<Number> windSpeed = (List<Number>) dailyData.get("wind_speed_10m_max");
        List<Number> weatherCodes = (List<Number>) dailyData.get("weather_code");

        return new ForecastArrays(dates, tempMax, tempMin, precipitation, windSpeed, weatherCodes);
    }

    /**
     * Processa varje dag
     */
    private List<WeatherForecast> processAndSaveForecasts(Place place, ForecastArrays arrays) {
        List<WeatherForecast> forecasts = new ArrayList<>();

        for (int i = 0; i < arrays.getDates().size(); i++) {
            try {
                LocalDate forecastDate = LocalDate.parse(arrays.getDates().get(i),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Kolla om prognos redan finns för denna dag
                WeatherForecast forecast = forecastRepository
                        .findByPlaceNameAndForecastDate(place.getName(), forecastDate)
                        .orElse(new WeatherForecast(place.getName(), place.getLat(), place.getLon(), forecastDate));

                // Uppdatera värden
                safeSetForecastValue(arrays.getTempMax(), i, forecast::setTemperatureMax);
                safeSetForecastValue(arrays.getTempMin(), i, forecast::setTemperatureMin);
                safeSetForecastValue(arrays.getPrecipitation(), i, forecast::setPrecipitationSum);
                safeSetForecastValue(arrays.getWindSpeed(), i, forecast::setWindSpeedMax);
                safeSetIntegerValue(arrays.getWeatherCodes(), i, forecast::setWeatherCode);

                WeatherForecast saved = forecastRepository.save(forecast);
                forecasts.add(saved);

            } catch (Exception e) {
                System.err.println("Error processing forecast day " + i + " for " + place.getName() + ": " + e.getMessage());
            }
        }

        return forecasts;
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