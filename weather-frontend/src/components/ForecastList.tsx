import { useEffect, useState } from "react";
import { apiGet } from "../utils/api";
import {ForecastItem, ForecastResponse} from "../types/types";


function displayValue(n: number | null | undefined, unit = "") {
    return typeof n === "number" ? `${n}${unit}` : "-";
}

export default function ForecastList({ place }: { place: string }) {
    const [data, setData] = useState<ForecastItem[]>([]);
    const [error, setError] = useState("");

    useEffect(() => {
        let ignore = false;
        (async () => {
            setError("");
            setData([]);
            try {
                // 7 dagar hanteras redan i backend (getForecast)
                const res = await apiGet<ForecastResponse>(`/forecast/${encodeURIComponent(place)}`);
                if (!ignore) setData(res.forecasts ?? []);
            } catch {
                if (!ignore) setError("Could not retrieve 7-day forecast ❌");
            }
        })();
        return () => { ignore = true; };
    }, [place]);

    if (error) return <p className="text-red-600">{error}</p>;
    if (!data.length) return <p>Loading forecast...</p>;

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
