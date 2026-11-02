import { useEffect, useState } from "react";
import api from "../api/axiosInstance";
import Loader from "../components/Loader";
import { getErrorMessage } from "../api/apiUtils";

function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Fetch customer bookings
  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/customer/bookings");
      const data = response.data;

      setBookings(
        Array.isArray(data) ? data : data.content || data.bookings || [],
      );
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load bookings."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  // Cancel a booking
  const handleCancel = async (bookingId) => {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this booking?",
    );

    if (!confirmed) return;

    try {
      setCancellingId(bookingId);
      setError("");
      setMessage("");

      await api.delete(`/customer/bookings/${bookingId}`);

      setMessage(`Booking ${bookingId} cancelled successfully.`);
      await fetchBookings();
    } catch (err) {
      setError(getErrorMessage(err, "Unable to cancel this booking."));
    } finally {
      setCancellingId(null);
    }
  };

  const formatDate = (date) => {
    if (!date) return "Date not available";

    const parsedDate = new Date(date);

    if (Number.isNaN(parsedDate.getTime())) {
      return "Date not available";
    }

    return parsedDate.toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  const getStatusClass = (status) => {
    const normalized = String(status || "pending").toLowerCase();

    if (normalized.includes("confirm")) return "confirmed";
    if (normalized.includes("cancel")) return "cancelled";
    return "pending";
  };

  return (
    <div className="my-bookings-page">
      <div className="page-header-row">
        <div>
          <p className="section-kicker">Your reservations</p>
          <h1>My Bookings</h1>
        </div>

        <button
          type="button"
          className="refresh-button"
          onClick={fetchBookings}
          disabled={loading}
        >
          {loading ? "Refreshing..." : "Refresh Bookings"}
        </button>
      </div>

      <p className="page-subtitle">View and manage your event bookings.</p>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {message && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}

      {loading ? (
        <Loader message="Loading your bookings..." />
      ) : bookings.length === 0 ? (
        <div className="empty-state-card">
          <h2>No bookings yet</h2>
          <p>You have no bookings yet. Explore events to book tickets.</p>
        </div>
      ) : (
        <div className="bookings-grid">
          {bookings.map((booking) => (
            <article className="booking-card" key={booking.bookingId}>
              <div className="booking-card-header">
                <div>
                  <span className="booking-card-label">Reservation</span>
                  <h2>{booking.event?.name || "Event booking"}</h2>
                </div>
                <span
                  className={`booking-status-badge ${getStatusClass(
                    booking.bookingStatus,
                  )}`}
                >
                  {booking.bookingStatus ?? "Pending"}
                </span>
              </div>

              <div className="receipt-grid">
                <div>
                  <span>Booking ID</span>
                  <strong>{booking.bookingId}</strong>
                </div>
                <div>
                  <span>Location</span>
                  <strong>{booking.event?.location || "Not available"}</strong>
                </div>
                <div>
                  <span>Event date</span>
                  <strong>{formatDate(booking.event?.eventDateTime)}</strong>
                </div>
                <div>
                  <span>Tickets</span>
                  <strong>{booking.quantity ?? "Not available"}</strong>
                </div>
              </div>

              <div className="booking-card-footer">
                <div>
                  <span className="footer-label">Summary</span>
                  <strong>{booking.event?.name || "Event booking"}</strong>
                </div>

                <button
                  type="button"
                  className="cancel-button"
                  onClick={() => handleCancel(booking.bookingId)}
                  disabled={
                    !booking.bookingId || cancellingId === booking.bookingId
                  }
                >
                  {cancellingId === booking.bookingId
                    ? "Cancelling..."
                    : "Cancel Booking"}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}

export default MyBookings;
