import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import HomePage from "./pages/HomePage";
import FavoritesPage from "./pages/FavoritesPage";
import ForecastPage from "./pages/ForecastPage";

export default function App() {
    return (
        <Router>
            <div className="max-w-4xl mx-auto p-4">
                <Navbar />
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/favorites" element={<FavoritesPage />} />
                    <Route path="/forecast" element={<ForecastPage />} />
                </Routes>
            </div>
        </Router>
    );
}
