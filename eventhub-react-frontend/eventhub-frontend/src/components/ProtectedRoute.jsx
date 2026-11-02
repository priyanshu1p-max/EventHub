
import { Navigate, Outlet, useLocation } from "react-router-dom";

function ProtectedRoute({ allowedRoles }) {
  const location = useLocation();

  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("userRole");

  // No login information: redirect to Login.
  if (!token || !userRole) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location }}
      />
    );
  }

  // User does not have permission for this page.
  if (allowedRoles && !allowedRoles.includes(userRole)) {
    if (userRole === "ADMIN") {
      return <Navigate to="/admin" replace />;
    }

    if (userRole === "ORGANISER") {
      return <Navigate to="/organiser" replace />;
    }

    if (userRole === "CUSTOMER") {
      return <Navigate to="/events" replace />;
    }

    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default ProtectedRoute;
