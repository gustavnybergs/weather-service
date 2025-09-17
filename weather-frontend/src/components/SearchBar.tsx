import {useEffect, useState} from "react";
import WeatherCard from "./WeatherCard";
import ForecastList from "./ForecastList";
import FavoriteToggle from "./FavoriteToggle";
import {apiGet} from "../utils/api";
import {Place} from "../types/types";


export default function SearchBar() {
    const [query, setQuery] = useState("");
    const [place, setPlace] = useState<string>("");
    const [places, setPlaces] = useState<Place[]>([]);

    // hämtar alla platser från backend
    useEffect(() => {
        apiGet<Place[]>("/places")
            .then(setPlaces)
            .catch(() => setPlaces([]));
    }, []);

    function doSearch() {
        const trimmed = query.trim();
        if (!trimmed) return;
        setPlace(trimmed);
    }

    return (
        <div className="space-y-6">
            <div className="flex gap-2">
                <input
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search location.."
                    className="border rounded p-2 w-full"
                />
                <button onClick={doSearch} className="px-4 py-2 bg-blue-400 text-white rounded hover:bg-blue-700">
                    Search
                </button>
            </div>

            {place && (
                <div className="space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-semibold">{place}</h2>
                        <FavoriteToggle place={place} />
                    </div>

                    <WeatherCard place={place} />
                    <ForecastList place={place} />
                </div>
            )}
        </div>
    );
}
