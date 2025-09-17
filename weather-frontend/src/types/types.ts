// Plats
export type Place = {
    name: string;
    lat: number;
    lon: number;
    favorite: boolean;
};

// Väderdata (current weather)
export type WeatherData = {
    temperature_2m: number;
    wind_speed_10m: number;
    cloud_cover: number;
};

// Backend-svar för väder
export type WeatherResponse = {
    place: Place;
    source: string;
    data: WeatherData;
    cached: boolean;
};

// Prognosdata (forecast för 1 dag)
export type ForecastItem = {
    forecastDate: string;
    temperatureMax?: number | null;
    temperatureMin?: number | null;
    precipitationSum?: number | null;
    windSpeedMax?: number | null;
    weatherCode?: number | null;
};

// Backend-svar för prognos
export type ForecastResponse = {
    place: Place;
    source: string;
    forecast_days: number;
    forecasts: ForecastItem[];
};

// Favorit-check (om en plats är favorit)
export type FavCheck = {
    place: string;
    favorite: boolean;
};

// Backend-svar för lista av favoriter
export type FavoritesResponse = {
    favorites: Place[];
};
