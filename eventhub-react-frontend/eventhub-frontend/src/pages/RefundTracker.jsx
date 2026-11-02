
import { useEffect, useState } from "react";
import api from "../api/axiosInstance";

function RefundTracker() {
  const [bookings, setBookings] = useState([]);
  const [refundId, setRefundId] = useState("");
  const [timeline, setTimeline] = useState(null);
  const [loading, setLoading] = useState(false);
  const [refundingBookingId, setRefundingBookingId] = useState(null);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [refundReason, setRefundReason] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const getErrorMessage = (err, fallback) => {
    const data = err.response?.data;

    if (typeof data === "string" && data.trim()) {
      return data;
    }

    return data?.message || data?.error || fallback;
  };

  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/customer/bookings");
      const data = response.data;

      setBookings(
        Array.isArray(data)
          ? data
          : data?.content || data?.bookings || []
      );
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load your bookings."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  // Open the refund reason textbox
  const openRefundForm = (booking) => {
    if (!booking.bookingId) {
      setError("Booking ID is missing.");
      return;
    }

    if (booking.paymentStatus !== "PAID") {
      setError("A monetary refund is available only for paid bookings.");
      return;
    }

    setError("");
    setMessage("");
    setSelectedBooking(booking);
    setRefundReason("");
  };

  // Submit refund with the entered reason
  const requestRefund = async (e) => {
    e.preventDefault();

    if (!refundReason.trim()) {
      setError("Please enter a reason for requesting the refund.");
      return;
    }

    const booking = selectedBooking;

    if (!booking?.bookingId) {
      setError("Please select a valid booking.");
      return;
    }

    try {
      setRefundingBookingId(booking.bookingId);
      setError("");
      setMessage("");
      setTimeline(null);

      const response = await api.post(
        `/customer/bookings/${booking.bookingId}/refund`,
        {
          reason: refundReason.trim()
        }
      );

      const data = response.data;
      const newRefundId = data?.refundId ?? data?.id;

      setMessage(
        typeof data === "string"
          ? data
          : data?.message || "Refund request submitted successfully."
      );

      if (newRefundId != null) {
        setRefundId(String(newRefundId));
      }

      setSelectedBooking(null);
      setRefundReason("");

      await fetchBookings();
    } catch (err) {
      setError(
        getErrorMessage(
          err,
          "Unable to request a refund. Check the booking and payment status."
        )
      );
    } finally {
      setRefundingBookingId(null);
    }
  };

  const trackRefund = async (e) => {
    e.preventDefault();

    if (!refundId.trim()) {
      setError("Please enter a refund ID.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setMessage("");
      setTimeline(null);

      const response = await api.get(
        `/customer/refunds/${encodeURIComponent(refundId.trim())}/timeline`
      );

      setTimeline(response.data);
    } catch (err) {
      setError(
        getErrorMessage(
          err,
          "Unable to find the refund timeline. Check the refund ID."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="refund-page">
      <h1>Refund Tracker</h1>
      <p>Request a refund and track its progress.</p>

      {error && (
        <p className="refund-error" role="alert">
          {error}
        </p>
      )}

      {message && (
        <p className="refund-success" role="status">
          {message}
        </p>
      )}

      <section className="refund-section">
        <h2>My Bookings</h2>

        {loading && <p>Loading...</p>}

        {!loading && bookings.length === 0 && (
          <p>No bookings found.</p>
        )}

        {bookings.map((booking) => {
          const isPaid = booking.paymentStatus === "PAID";
          const isRequesting =
            refundingBookingId === booking.bookingId;

          return (
            <div className="refund-booking" key={booking.bookingId}>
              <div>
                <h3>{booking.event?.name || "Event booking"}</h3>

                <p>Booking ID: {booking.bookingId}</p>
                <p>Tickets: {booking.quantity ?? "N/A"}</p>
                <p>Booking Status: {booking.bookingStatus || "N/A"}</p>
                <p>Payment Status: {booking.paymentStatus || "N/A"}</p>

                <p>
                  Total Price: ₹
                  {Number(booking.totalPrice || 0).toFixed(2)}
                </p>

                {!isPaid && (
                  <p>Refund requests require a paid booking.</p>
                )}
              </div>

              <button
                type="button"
                onClick={() => openRefundForm(booking)}
                disabled={
                  !booking.bookingId ||
                  !isPaid ||
                  isRequesting ||
                  refundingBookingId !== null
                }
              >
                Request Refund
              </button>

              {selectedBooking?.bookingId === booking.bookingId && (
                <form onSubmit={requestRefund} className="refund-form">
                  <h4>Refund Reason</h4>

                  <textarea
                    rows="4"
                    placeholder="Please explain why you want a refund..."
                    value={refundReason}
                    onChange={(e) => setRefundReason(e.target.value)}
                    required
                    disabled={isRequesting}
                  />

                  <button type="submit" disabled={isRequesting}>
                    {isRequesting ? "Submitting..." : "Submit Refund Request"}
                  </button>

                  <button
                    type="button"
                    onClick={() => {
                      setSelectedBooking(null);
                      setRefundReason("");
                      setError("");
                    }}
                    disabled={isRequesting}
                  >
                    Cancel
                  </button>
                </form>
              )}
            </div>
          );
        })}
      </section>

      <section className="refund-section">
        <h2>Track Your Refund</h2>

        <form onSubmit={trackRefund} className="refund-form">
          <input
            type="text"
            placeholder="Enter Refund ID"
            value={refundId}
            onChange={(e) => setRefundId(e.target.value)}
          />

          <button type="submit" disabled={loading}>
            Track Refund
          </button>
        </form>

        {timeline && (
          <div className="refund-timeline">
            <h3>Refund Timeline</h3>

            {typeof timeline === "string" ? (
              <p>{timeline}</p>
            ) : (
              <pre>{JSON.stringify(timeline, null, 2)}</pre>
            )}
          </div>
        )}
      </section>
    </div>
  );
}

export default RefundTracker;
