
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/authService";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
    roleName: "ROLE_CUSTOMER",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    // NEW: Validate Indian 10-digit mobile number
    if (!/^[6-9]\d{9}$/.test(form.phone)) {
      setError("Enter a valid 10-digit mobile number.");
      return;
    }

    setLoading(true);

    try {
      // Convert phone string into a number for backend Long
      const requestData = {
        ...form,
        phone: Number(form.phone),
      };

      await registerUser(requestData);

      setSuccess("Registration successful! You can now log in.");

      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      console.error("Registration error:", err);
      console.error("Backend response:", err.response?.data);

      const status = err.response?.status;
      const responseData = err.response?.data;

      const message =
        typeof responseData === "string"
          ? responseData
          : responseData?.message || responseData?.error || err.message;

      setError(
        `Registration failed${status ? ` (${status})` : ""}: ${message}`
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-container">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Create Account</h1>
        <p className="muted">Join EventHub today.</p>

        {error && <p className="error-message">{error}</p>}
        {success && <p>{success}</p>}

        <label htmlFor="name">Full Name</label>
        <input
          id="name"
          name="name"
          value={form.name}
          onChange={handleChange}
          placeholder="Enter your name"
          required
        />

        <label htmlFor="register-email">Email</label>
        <input
          id="register-email"
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="you@example.com"
          required
        />

        {/* NEW: Customer phone number */}
        <label htmlFor="phone">Phone Number</label>
        <input
          id="phone"
          type="tel"
          name="phone"
          value={form.phone}
          onChange={handleChange}
          placeholder="Enter your 10-digit phone number"
          maxLength={10}
          required
        />

        <label htmlFor="register-password">Password</label>
        <input
          id="register-password"
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Create password"
          required
        />

        <label htmlFor="roleName">Account Type</label>
        <select
          id="roleName"
          name="roleName"
          value={form.roleName}
          onChange={handleChange}
        >
          <option value="ROLE_CUSTOMER">Customer</option>
          <option value="ROLE_ORGANISER">Organiser</option>
        </select>

        <button
          className="primary-button full-width"
          type="submit"
          disabled={loading}
        >
          {loading ? "Registering..." : "Register"}
        </button>

        <p className="auth-footer">
          Already registered? <Link to="/login">Login</Link>
        </p>
      </form>
    </main>
  );
}

export default Register;
