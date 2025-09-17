import { useEffect, useState } from "react";
import { apiDelete, apiGet } from "../utils/api";
import WeatherCard from "../components/WeatherCard";
import {Place} from "../types/types";


export default function FavoritesPage() {
    const [favorites, setFavorites] = useState<Place[]>([]);
    const [error, setError] = useState("");

    async function loadFavorites() {
        try {
            const res = await apiGet<{ favorites: Place[] }>("/favorites");
            setFavorites(res.favorites);
        } catch {
            setError("Could not retrieve favorites ❌");
        }
    }

    useEffect(() => {
        loadFavorites();
    }, []);

    async function removeFavorite(name: string) {
        try {
            await apiDelete(`/favorites/${encodeURIComponent(name)}`);
            loadFavorites();
        } catch {
            setError("Could not remove favorite ❌");
        }
    }

    if (error) return <p className="text-red-600">{error}</p>;
    if (favorites.length === 0) return <p>No favorites yet</p>;

    return (
        <div className="space-y-4">
            <h2 className="text-xl font-bold">My favorites⭐</h2>
            {favorites.map((f) => (
                <div key={f.name} className="space-y-2 border p-3 rounded bg-white shadow">
                    <WeatherCard place={f.name} />
                    <button
                        onClick={() => removeFavorite(f.name)}
                        className="px-2 py-1 bg-red-300 text-white rounded hover:bg-red-500">
                        Remove
                    </button>
                </div>
            ))}
        </div>
    );
}

