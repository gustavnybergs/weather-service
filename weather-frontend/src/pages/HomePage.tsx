import SearchBar from "../components/SearchBar";


export default function Home() {
    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">🌤️ WeatherApp 🌤</h1>
            <p className="text-gray-700">
                Here you can search for cities' current weather,
                see 7-day forecasts and favorite locations
            </p>

            {/* Sökfältet direkt på startsidan */}
            <SearchBar />
        </div>
    );
}

