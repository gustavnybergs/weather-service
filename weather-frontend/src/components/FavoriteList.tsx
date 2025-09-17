import { useEffect, useState } from "react";
import { apiGet } from "../utils/api";
import WeatherCard from "./WeatherCard";
import {Place} from "../types/types";


// Hämtar lista från places
export default function FavoriteList() {
    const [places, setPlaces] = useState<Place[]>([]);

    useEffect(() => {
        apiGet<Place[]>("/places").then(setPlaces);
    }, []);

    //Hämtar favorites = true
    const favorites = places.filter(p => p.favorite);

    if (favorites.length === 0) return <p>No favorites.</p>;

    return (
        <div>
            <h2 className="text-xl font-bold mb-2">My favorites</h2>
            <div className="grid gap-4 md:grid-cols-2">
                {favorites.map(f => (
                    <WeatherCard key={f.name} place={f.name} /> // visar väderinfo, håller ordning på listan
                ))}
            </div>
        </div>
    );
}
