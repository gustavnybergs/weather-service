import { useEffect, useState, useCallback } from "react";
import { apiDelete, apiGet, apiPut } from "../utils/api";
import {FavCheck} from "../types/types";

export default function FavoriteToggle({ place }: { place: string }) {
    const [isFav, setIsFav] = useState<boolean | null>(null);
    const [err, setErr] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const refresh = useCallback(async () => {
        setErr("");
        try {
            const res = await apiGet<FavCheck>(`/favorites/${encodeURIComponent(place)}`);
            setIsFav(res.is_favorite);
        } catch {
            setIsFav(false);
        }
    }, [place]);

    useEffect(() => {
        refresh();
    }, [refresh]);

    async function toggle() {
        if (isFav === null) return;
        setErr("");
        setIsLoading(true);
        
        try {
            if (isFav) {
                // Ta bort favorit
                await apiDelete(`/favorites/${encodeURIComponent(place)}`);
                setIsFav(false); // Uppdatera lokalt state direkt
            } else {
                // Lägg till favorit
                await apiPut(`/favorites/${encodeURIComponent(place)}`);
                setIsFav(true); // Uppdatera lokalt state direkt
            }
            
            // Dubbelkolla genom att hämta status från backend
            setTimeout(() => {
                refresh();
            }, 500);
            
        } catch (error) {
            setErr("Could not update favorite status ❌");
            // Återställ state vid fel
            refresh();
        } finally {
            setIsLoading(false);
        }
    }

    if (isFav === null) return <button className="px-3 py-2 rounded bg-gray-200">...</button>;

    return (
        <div className="flex items-center gap-3">
            <button
                onClick={toggle}
                disabled={isLoading}
                className={`px-3 py-2 rounded text-white ${
                    isLoading 
                        ? "bg-gray-400 cursor-not-allowed" 
                        : isFav 
                            ? "bg-red-300 hover:bg-red-500" 
                            : "bg-blue-400 hover:bg-blue-600"
                }`}
            >
                {isLoading ? "..." : isFav ? "Remove favorite" : "⭐ Add favorite"}
            </button>
            {err && <span className="text-red-600 text-sm">{err}</span>}
        </div>
    );
}