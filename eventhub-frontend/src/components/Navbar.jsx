import { Link, useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();

  const token = localStorage.getItem("token");
  const role = localStorage.getItem("userRole");

  const isLoggedIn = Boolean(token && role);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    localStorage.removeItem("email");
    navigate("/login", { replace: true });
    window.location.reload();
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <span>Event</span>Hub
      </Link>

      <div className="navbar-links">
        <Link to="/">Home</Link>

        {!isLoggedIn && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}

        {isLoggedIn && role === "CUSTOMER" && (
          <>
            <Link to="/events">Explore Events</Link>
            <Link to="/customer/nearby-events">Nearby Events</Link>
            <Link to="/my-bookings">My Bookings</Link>
            <Link to="/refund-tracker">Refund Tracker</Link>
            <Link to="/customer/profile">My Profile</Link>
          </>
        )}

        {isLoggedIn && role === "ORGANISER" && (
          <>
            <Link to="/organiser">Organiser Dashboard</Link>
            <Link to="/organiser/profile">My Profile</Link>
          </>
        )}

        {isLoggedIn && role === "ADMIN" && (
          <Link to="/admin">Admin Dashboard</Link>
        )}

        {isLoggedIn && (
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
