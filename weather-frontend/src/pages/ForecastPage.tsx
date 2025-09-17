import { useState } from "react";
import ForecastList from "../components/ForecastList";

export default function ForecastPage() {
    const [place, setPlace] = useState("");

    return (
        <div className="space-y-6">
            <h2 className="text-xl font-bold">7-day forecast</h2>
            <div className="flex gap-2">
                <input
                    type="text"
                    value={place}
                    onChange={(e) => setPlace(e.target.value)}
                    placeholder="Enter location.."
                    className="border rounded p-2 w-full"
                />
            </div>

            {place && <ForecastList place={place} />}
        </div>
    );
}
