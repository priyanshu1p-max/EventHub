
import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { loginUser } from "../api/authService";

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Check whether the user was redirected because of an expired session.
  const sessionExpired =
    new URLSearchParams(location.search).get("sessionExpired") === "true";

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const data = await loginUser(form);

      // Get JWT token from backend response.
      const token = data.token;

      if (!token) {
        setError("No JWT token was returned.");
        return;
      }

      // Store authentication details.
      localStorage.setItem("token", token);

      // Get user role and remove ROLE_ prefix if present.
      const role = String(data.roleName || "").replace("ROLE_", "");

      if (!role) {
        localStorage.removeItem("token");
        setError("User role was not returned by the server.");
        return;
      }

      localStorage.setItem("userRole", role);

      // Store email if returned by backend.
      if (data.email) {
        localStorage.setItem("email", data.email);
      }

      // Redirect according to user role.
      if (role === "ADMIN") {
        navigate("/admin", { replace: true });
      } else if (role === "ORGANISER") {
        navigate("/organiser", { replace: true });
      } else if (role === "CUSTOMER") {
        navigate("/events", { replace: true });
      } else {
        localStorage.removeItem("token");
        localStorage.removeItem("userRole");
        localStorage.removeItem("email");

        setError("Invalid user role. Please contact support.");
      }

    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Login failed. Check your email and password."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-container">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Welcome Back</h1>

        <p className="muted">
          Log in to your EventHub account.
        </p>

        {/* Session expired message */}
        {sessionExpired && (
          <p className="error-message">
            Your session has expired. Please log in again.
          </p>
        )}

        {/* Login error */}
        {error && (
          <p className="error-message">
            {error}
          </p>
        )}

        <label htmlFor="email">Email</label>

        <input
          id="email"
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="you@example.com"
          required
        />

        <label htmlFor="password">Password</label>

        <input
          id="password"
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Enter password"
          required
        />

        <button
          className="primary-button full-width"
          type="submit"
          disabled={loading}
        >
          {loading ? "Logging in..." : "Login"}
        </button>

        <p className="auth-footer">
          Don't have an account?{" "}
          <Link to="/register">Register</Link>
        </p>

        <p className="auth-footer">
          <Link to="/">Back to Home</Link>
        </p>
      </form>
    </main>
  );
}

export default Login;
