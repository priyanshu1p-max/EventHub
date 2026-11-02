import { useEffect, useState } from "react";

import api from "../api/axiosInstance";
import Loader from "../components/Loader";
import { getErrorMessage } from "../api/apiUtils";

import QRScanner from "../components/Organiser/QRScanner";
import TicketDashboard from "../components/Organiser/TicketDashboard";
import CheckInTimeline from "../components/Organiser/CheckInTimeline";
import OrganiserProfile from "./OrganiserProfile";

function OrganiserDashboard() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [images, setImages] = useState([]);

  const [form, setForm] = useState({
    name: "",
    description: "",
    category: "",
    location: "",
    eventDateTime: "",
    ticketPrice: "",
    availableTickets: "",
  });

  const [activeTab, setActiveTab] = useState("events");
  const [selectedEventId, setSelectedEventId] = useState("");

  const [bookings, setBookings] = useState([]);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingError, setBookingError] = useState("");

  // Fetch organiser events
  const fetchMyEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/organiser/myEvents");
      setEvents(response.data);
    } catch (err) {
      setError(
        err.response?.status === 403
          ? "Access denied. Please log in as an organiser."
          : getErrorMessage(err, "Unable to load your events."),
      );
    } finally {
      setLoading(false);
    }
  };

  // Fetch organiser bookings
  const fetchOrganiserBookings = async () => {
    try {
      setBookingLoading(true);
      setBookingError("");

      const response = await api.get("/organiser/bookings");
      setBookings(response.data);
    } catch (err) {
      setBookingError(getErrorMessage(err, "Unable to load bookings."));
    } finally {
      setBookingLoading(false);
    }
  };

  useEffect(() => {
    fetchMyEvents();
  }, []);

  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  };

  // Reset create-event form
  const resetForm = () => {
    setForm({
      name: "",
      description: "",
      category: "",
      location: "",
      eventDateTime: "",
      ticketPrice: "",
      availableTickets: "",
    });

    setImages([]);
  };

  // Create event
  const handleCreateEvent = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (
      !form.name.trim() ||
      !form.description.trim() ||
      !form.category ||
      !form.location.trim()
    ) {
      setError("Please fill in all event details, including category.");
      return;
    }

    if (!form.eventDateTime || new Date(form.eventDateTime) <= new Date()) {
      setError("Please select a future event date and time.");
      return;
    }

    if (
      form.ticketPrice === "" ||
      !Number.isFinite(Number(form.ticketPrice)) ||
      Number(form.ticketPrice) < 0
    ) {
      setError("Enter a valid ticket price.");
      return;
    }

    if (
      form.availableTickets === "" ||
      !Number.isInteger(Number(form.availableTickets)) ||
      Number(form.availableTickets) < 1
    ) {
      setError("Available tickets must be a positive whole number.");
      return;
    }

    try {
      setLoading(true);

      const formData = new FormData();

      const eventData = {
        ...form,
        ticketPrice: Number(form.ticketPrice),
        availableTickets: Number(form.availableTickets),
      };

      formData.append(
        "event",
        new Blob([JSON.stringify(eventData)], {
          type: "application/json",
        }),
      );

      images.forEach((file) => {
        formData.append("images", file);
      });

      await api.post("/organiser/registerEvent", formData);

      setMessage("Event created successfully. Admin approval may be required.");

      resetForm();

      await fetchMyEvents();
    } catch (err) {
      setError(getErrorMessage(err, "Failed to create event."));
    } finally {
      setLoading(false);
    }
  };

  // Upload image for an existing event
  const handleImageUpload = async (eventId, file) => {
    if (!file) return;

    const imageData = new FormData();
    imageData.append("image", file);

    try {
      await api.post(`/organiser/events/${eventId}/image`, imageData);

      setMessage("Event image uploaded successfully.");
      await fetchMyEvents();
    } catch (err) {
      setError(getErrorMessage(err, "Image upload failed."));
    }
  };

  // Select an event for ticket operations
  const handleManageEvent = (event) => {
    setSelectedEventId(String(event.id));
    setActiveTab("operations");
    setError("");
    setMessage("");
  };

  return (
    <div className="organiser-page">
      <div className="dashboard-hero organiser-hero">
        <div>
          <p className="hero-kicker">Operations hub</p>
          <h1>Organiser Dashboard</h1>
        </div>

        <div className="hero-pills">
          <span>Live events</span>
          <span>Bookings</span>
          <span>Insights</span>
        </div>
      </div>

      <p className="dashboard-subtitle">Create and manage your events.</p>

      {/* Navigation tabs */}
      <div className="organiser-tabs">
        <button
          type="button"
          className={
            activeTab === "events" ? "active-tab tab-button" : "tab-button"
          }
          onClick={() => setActiveTab("events")}
        >
          My Events
        </button>

        <button
          type="button"
          className={
            activeTab === "bookings" ? "active-tab tab-button" : "tab-button"
          }
          onClick={() => {
            setActiveTab("bookings");
            fetchOrganiserBookings();
          }}
        >
          Bookings
        </button>

        <button
          type="button"
          className={
            activeTab === "operations" ? "active-tab tab-button" : "tab-button"
          }
          onClick={() => setActiveTab("operations")}
        >
          Event Operations
        </button>

        <button
          type="button"
          className={
            activeTab === "profile" ? "active-tab tab-button" : "tab-button"
          }
          onClick={() => {
            setActiveTab("profile");
            setError("");
            setMessage("");
          }}
        >
          My Profile
        </button>
      </div>

      {message && <p className="success-message">{message}</p>}

      {error && <p className="error-message">{error}</p>}

      {/* MY EVENTS TAB */}
      {activeTab === "events" && (
        <>
          <section className="organiser-form-section">
            <h2>Create New Event</h2>

            <form onSubmit={handleCreateEvent}>
              <label htmlFor="eventName">Event Name</label>
              <input
                id="eventName"
                name="name"
                value={form.name}
                onChange={handleChange}
                required
              />

              <label htmlFor="eventDescription">Description</label>
              <textarea
                id="eventDescription"
                name="description"
                value={form.description}
                onChange={handleChange}
                required
              />

              {/* NEW: Event Category */}
              <label htmlFor="eventCategory">Event Category</label>
              <select
                id="eventCategory"
                name="category"
                value={form.category}
                onChange={handleChange}
                required
              >
                <option value="">-- Select Event Category --</option>
                <option value="MUSIC">Music</option>
                <option value="SPORTS">Sports</option>
                <option value="TECHNOLOGY">Technology</option>
                <option value="EDUCATION">Education</option>
                <option value="BUSINESS">Business</option>
                <option value="ART">Art & Culture</option>
                <option value="FOOD">Food & Drinks</option>
                <option value="ENTERTAINMENT">Entertainment</option>
                <option value="TRAVEL">Travel & Adventure</option>
                <option value="OTHER">Other</option>
              </select>

              <label htmlFor="eventLocation">Location</label>
              <input
                id="eventLocation"
                name="location"
                value={form.location}
                onChange={handleChange}
                required
              />

              <label htmlFor="eventDateTime">Date and Time</label>
              <input
                id="eventDateTime"
                type="datetime-local"
                name="eventDateTime"
                value={form.eventDateTime}
                onChange={handleChange}
                required
              />

              <label htmlFor="ticketPrice">Ticket Price (₹)</label>
              <input
                id="ticketPrice"
                type="number"
                name="ticketPrice"
                min="0"
                step="0.01"
                value={form.ticketPrice}
                onChange={handleChange}
                required
              />

              <label htmlFor="availableTickets">Available Tickets</label>
              <input
                id="availableTickets"
                type="number"
                name="availableTickets"
                min="1"
                step="1"
                value={form.availableTickets}
                onChange={handleChange}
                required
              />

              <label htmlFor="eventImages">Select Event Pictures</label>

              <input
                id="eventImages"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                onChange={(e) => {
                  setImages(Array.from(e.target.files || []));
                }}
              />

              {images.length > 0 && (
                <div className="selected-images">
                  <p>{images.length} picture(s) selected</p>

                  {images.map((file, index) => (
                    <div key={`${file.name}-${index}`}>
                      <img
                        src={URL.createObjectURL(file)}
                        alt={file.name}
                        width="100"
                      />
                      <p>{file.name}</p>
                    </div>
                  ))}
                </div>
              )}

              <button type="submit" disabled={loading}>
                {loading ? "Please wait..." : "Submit Event"}
              </button>
            </form>
          </section>

          <section className="organiser-events-section">
            <h2>My Events</h2>

            {loading && <Loader message="Loading your events..." />}

            {!loading && events.length === 0 && (
              <p>You haven't created any events yet.</p>
            )}

            <div className="organiser-events-grid">
              {events.map((event) => (
                <article className="organiser-event-card" key={event.id}>
                  <h3>{event.name}</h3>

                  <p>{event.description}</p>
                  <p>Category: {event.category || "Not specified"}</p>
                  <p>Location: {event.location}</p>

                  <p>
                    Date:{" "}
                    {event.eventDateTime
                      ? new Date(event.eventDateTime).toLocaleString("en-IN")
                      : "Not available"}
                  </p>

                  <p>Price: ₹{event.ticketPrice}</p>
                  <p>Available tickets: {event.availableTickets}</p>
                  <p>Status: {event.status}</p>

                  <label>
                    Upload event image
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      onChange={(e) =>
                        handleImageUpload(event.id, e.target.files?.[0])
                      }
                    />
                  </label>

                  <button
                    type="button"
                    onClick={() => handleManageEvent(event)}
                  >
                    Manage Tickets & Check-in
                  </button>
                </article>
              ))}
            </div>
          </section>
        </>
      )}

      {/* BOOKINGS TAB */}
      {activeTab === "bookings" && (
        <section className="organiser-bookings-section">
          <h2>Customer Bookings</h2>

          <button
            type="button"
            onClick={fetchOrganiserBookings}
            disabled={bookingLoading}
          >
            {bookingLoading ? "Refreshing..." : "Refresh Bookings"}
          </button>

          {bookingError && <p className="error-message">{bookingError}</p>}

          {bookingLoading && <Loader message="Loading customer bookings..." />}

          {!bookingLoading && !bookingError && bookings.length === 0 && (
            <p>No bookings found for your events.</p>
          )}

          {!bookingLoading && bookings.length > 0 && (
            <>
              <div className="booking-summary">
                <div className="booking-summary-card">
                  <h3>
                    {bookings
                      .filter((b) => b.bookingStatus === "CONFIRMED")
                      .reduce((total, b) => total + b.quantity, 0)}
                  </h3>
                  <p>Confirmed Tickets</p>
                </div>

                <div className="booking-summary-card">
                  <h3>
                    {bookings
                      .filter((b) => b.bookingStatus === "CANCELLED")
                      .reduce((total, b) => total + b.quantity, 0)}
                  </h3>
                  <p>Cancelled Tickets</p>
                </div>

                <div className="booking-summary-card">
                  <h3>{bookings.length}</h3>
                  <p>Total Bookings</p>
                </div>
              </div>

              <div className="booking-table-container">
                <table className="booking-table">
                  <thead>
                    <tr>
                      <th>Booking ID</th>
                      <th>Customer</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Event</th>
                      <th>Tickets</th>
                      <th>Total Price</th>
                      <th>Booking Date</th>
                      <th>Status</th>
                    </tr>
                  </thead>

                  <tbody>
                    {bookings.map((booking) => (
                      <tr key={booking.bookingId}>
                        <td>{booking.bookingId}</td>
                        <td>{booking.customerName}</td>
                        <td>{booking.customerEmail}</td>
                        <td>{booking.customerPhone || "Not provided"}</td>
                        <td>{booking.eventName}</td>
                        <td>{booking.quantity}</td>
                        <td>₹{booking.totalPrice}</td>

                        <td>
                          {booking.bookingDateTime
                            ? new Date(booking.bookingDateTime).toLocaleString(
                                "en-IN",
                              )
                            : "N/A"}
                        </td>

                        <td>
                          <span
                            className={`booking-status ${booking.bookingStatus?.toLowerCase()}`}
                          >
                            {booking.bookingStatus}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </section>
      )}

      {/* EVENT OPERATIONS TAB */}
      {activeTab === "operations" && (
        <section className="organiser-operations-section">
          <h2>Event Operations</h2>

          {events.length === 0 ? (
            <p>Create an event before managing tickets.</p>
          ) : (
            <>
              <label htmlFor="manageEvent">Select Event</label>

              <select
                id="manageEvent"
                value={selectedEventId}
                onChange={(e) => setSelectedEventId(e.target.value)}
              >
                <option value="">-- Select an event --</option>

                {events.map((event) => (
                  <option key={event.id} value={String(event.id)}>
                    {event.name}
                  </option>
                ))}
              </select>

              {selectedEventId ? (
                <>
                  <TicketDashboard eventId={selectedEventId} />
                  <QRScanner eventId={selectedEventId} />
                  <CheckInTimeline eventId={selectedEventId} />
                </>
              ) : (
                <p>
                  Select an event to view its ticket dashboard and scan customer
                  tickets.
                </p>
              )}
            </>
          )}
        </section>
      )}

      {/* ORGANISER PROFILE TAB */}
      {activeTab === "profile" && (
        <section className="organiser-profile-section">
          <OrganiserProfile />
        </section>
      )}
    </div>
  );
}

export default OrganiserDashboard;
