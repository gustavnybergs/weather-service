import { useEffect, useState } from "react";
import { apiGet } from "../utils/api";
import {ForecastItem, ForecastResponse, FavCheck} from "../types/types";

function displayValue(n: number | null | undefined, unit = "") {
    return typeof n === "number" ? `${n}${unit}` : "-";
}

export default function ForecastList({ place }: { place: string }) {
    const [data, setData] = useState<ForecastItem[]>([]);
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [isFavorite, setIsFavorite] = useState(false);

    useEffect(() => {
        let ignore = false;
        (async () => {
            setError("");
            setData([]);
            setIsLoading(true);
            setIsFavorite(false);

            try {
                // Använd samma logik som FavoriteToggle för att kolla favorit-status
                const favoriteCheck = await apiGet<FavCheck>(`/favorites/${encodeURIComponent(place)}`);
                
                if (!ignore) {
                    setIsFavorite(favoriteCheck.is_favorite);
                    
                    if (favoriteCheck.is_favorite) {
                        // Om platsen är favorit, försök hämta prognoser
                        try {
                            const res = await apiGet<ForecastResponse>(`/forecast/${encodeURIComponent(place)}`);
                            if (!ignore) setData(res.forecasts ?? []);
                        } catch (forecastError) {
                            // Om forecast misslyckas för favoritplats, försök med actual_place_name
                            if (favoriteCheck.actual_place_name && favoriteCheck.actual_place_name !== place) {
                                try {
                                    const res = await apiGet<ForecastResponse>(`/forecast/${encodeURIComponent(favoriteCheck.actual_place_name)}`);
                                    if (!ignore) setData(res.forecasts ?? []);
                                } catch {
                                    if (!ignore) setError("Could not load forecast data");
                                }
                            } else {
                                if (!ignore) setError("Could not load forecast data");
                            }
                        }
                    }
                }
            } catch (err) {
                // Om favorites API misslyckas, platsen är inte favorit
                if (!ignore) {
                    setIsFavorite(false);
                    setError("");
                }
            } finally {
                if (!ignore) setIsLoading(false);
            }
        })();
        return () => { ignore = true; };
    }, [place]);

    if (isLoading) return <p>Loading forecast...</p>;
    
    if (!isFavorite) {
        return (
            <div className="border rounded-lg p-4 shadow bg-yellow-50">
                <h3 className="text-xl font-semibold mb-2">7-day forecast – {place}</h3>
                <p className="text-yellow-600">
                    ⭐ Add {place} as a favorite to see 7-day forecast!
                </p>
                <p className="text-sm text-gray-500 mt-2">
                    Forecasts are only available for favorite locations that are updated automatically every 30 minutes.
                </p>
            </div>
        );
    }
    
    if (error) return <p className="text-red-600">{error}</p>;
    if (!data.length) return <p>No forecast data available - will be updated within 30 minutes</p>;

    return (
        <div className="border rounded-lg p-4 shadow bg-white">
            <h3 className="text-xl font-semibold mb-2">7-day forecast – {place}</h3>
            <ul className="space-y-2">
                {data.map((d, i) => (
                    <li key={`${d.forecastDate}-${i}`} className="flex justify-between border-b pb-1">
                        <span>{d.forecastDate}</span>
                         <span className="text-sm">
                            🌡️ {displayValue(d.temperatureMin, "°C")} – {displayValue(d.temperatureMax, "°C")}
                            {" "}💧 {displayValue(d.precipitationSum, " mm")}
                            {" "}💨 {displayValue(d.windSpeedMax, " m/s")}
                         </span>
                    </li>
                ))}
            </ul>
        </div>
    );
}