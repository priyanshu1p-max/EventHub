
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

// Attach JWT token to requests.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Handle expired or invalid JWT.
api.interceptors.response.use(
  (response) => response,

  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("userRole");
      localStorage.removeItem("email");

      const currentPath = window.location.pathname;

      // Avoid repeated redirects on public authentication pages.
      if (currentPath !== "/login" && currentPath !== "/register") {
        window.location.replace("/login?sessionExpired=true");
      }
    }

    return Promise.reject(error);
  }
);

export default api;
