import { BrowserRouter, Routes, Route } from "react-router-dom";

import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Events from "./pages/Events";
import MyBookings from "./pages/MyBookings";
import RefundTracker from "./pages/RefundTracker";
import OrganiserDashboard from "./pages/OrganiserDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import NearbyEvents from "./pages/NearbyEvents";

import CustomerProfile from "./pages/CustomerProfile";
import OrganiserProfile from "./pages/OrganiserProfile"; // NEW

function App() {
  return (
    <BrowserRouter>
      <Navbar />

      <Routes>
        {/* Public routes */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Customer-only routes */}
        <Route element={<ProtectedRoute allowedRoles={["CUSTOMER"]} />}>
          <Route path="/events" element={<Events />} />
          <Route path="/customer/nearby-events" element={<NearbyEvents />} />
          <Route path="/my-bookings" element={<MyBookings />} />
          <Route path="/refund-tracker" element={<RefundTracker />} />

          {/* NEW: Customer profile */}
          <Route path="/customer/profile" element={<CustomerProfile />} />
        </Route>

        {/* Organiser-only routes */}
        <Route element={<ProtectedRoute allowedRoles={["ORGANISER"]} />}>
          <Route path="/organiser" element={<OrganiserDashboard />} />

          {/* NEW: Organiser profile */}
          <Route path="/organiser/profile" element={<OrganiserProfile />} />
        </Route>

        {/* Admin-only routes */}
        <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
          <Route path="/admin" element={<AdminDashboard />} />
        </Route>

        {/* Unknown URL */}
        <Route path="*" element={<h2>404 - Page Not Found</h2>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;