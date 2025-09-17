import { useEffect, useState } from "react";
import { apiDelete, apiGet, apiPut } from "../utils/api";
import {FavCheck} from "../types/types";


export default function FavoriteToggle({ place }: { place: string }) {
    const [isFav, setIsFav] = useState<boolean | null>(null);
    const [err, setErr] = useState("");

    async function refresh() {
        setErr("");
        try {
            const res = await apiGet<FavCheck>(`/favorites/${encodeURIComponent(place)}`);
            setIsFav(res.favorite);
        } catch {
            setIsFav(false);
        }
    }

    useEffect(() => {
        refresh();
    }, [place]);

    async function toggle() {
        if (isFav === null) return;
        setErr("");
        try {
            if (isFav) {
                await apiDelete(`/favorites/${encodeURIComponent(place)}`);
                setIsFav(false);
            } else {
                await apiPut(`/favorites/${encodeURIComponent(place)}`);
                setIsFav(true);
            }
        } catch {
            setErr("Could not update favorite status ❌");
        }
    }

    if (isFav === null) return <button className="px-3 py-2 rounded bg-gray-200">...</button>;

    return (
        <div className="flex items-center gap-3">
            <button
                onClick={toggle}
                className={`px-3 py-2 rounded text-white ${isFav ? "bg-red-300 hover:bg-red-500" : "bg-blue-400 hover:bg-blue-600"}`}
            >
                {isFav ? "<Remove favorite>" : "⭐"}
            </button>
            {err && <span className="text-red-600 text-sm">{err}</span>}
        </div>
    );
}
