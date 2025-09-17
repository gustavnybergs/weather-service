import { Link } from "react-router-dom";

export default function Navbar() {
    return (
        <nav className="bg-yellow-200 text-blue-500 p-4 flex gap-10 mb-6 rounded">
            <Link to="/" className="hover:underline">Home</Link>
            <Link to="/favorites" className="hover:underline">Favorites</Link>
            <Link to="/forecast" className="hover:underline">Forecast</Link>
        </nav>
    );
}