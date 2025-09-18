import { useEffect, useState } from 'react'
import { apiGet } from '../utils/api'
import { WeatherResponse } from '../types/types'

export default function WeatherCard({ place }: { place: string }) {
  const [weather, setWeather] = useState<WeatherResponse | null>(null)
  const [error, setError] = useState('')

  function convertRawWeatherToResponse(rawData: any, placeName: string): WeatherResponse {
    return {
      place: {
        name: placeName,
        lat: rawData.latitude,
        lon: rawData.longitude,
        favorite: false // Okänt för nya platser
      },
      source: "open-meteo",
      cached: false,
      data: rawData.current
    };
  }

  useEffect(() => {
    let ignore = false
    ;(async () => {
      setError('')
      setWeather(null)
      try {
        // Först försök favorit-endpoint
        const res = await apiGet<WeatherResponse>(`/weather/${encodeURIComponent(place)}`);
        if (!ignore) setWeather(res);
      } catch {
        try {
          // Fallback till live-sökning
          const rawRes = await apiGet<any>(`/weather/weatherAtLocation/${encodeURIComponent(place)}`);
          const convertedRes = convertRawWeatherToResponse(rawRes, place);
          if (!ignore) setWeather(convertedRes);
        } catch {
          if (!ignore) setError('Could not retrieve the weather ❌');
        }
      }
    })()
    return () => {
      ignore = true
    }
  }, [place])

  if (error) return <p className="text-red-600">{error}</p>
  if (!weather) return <p>Loading weather data...</p>

  return (
    <div className="border rounded-lg p-4 shadow bg-white">
      <h3 className="text-xl font-semibold mb-2">
        Weather right now – {weather.place.name}
      </h3>
      <ul className="space-y-1">
        <li>🌡️ Temperatur: {weather.data.temperature_2m} °C</li>
        <li>💨 Vind: {weather.data.wind_speed_10m} m/s</li>
        <li>☁️ Molnighet: {weather.data.cloud_cover} %</li>
      </ul>
      <p className="text-xs text-gray-500 mt-2">
        Source: {weather.source} {weather.cached && '(cache)'}
      </p>
    </div>
  )
}