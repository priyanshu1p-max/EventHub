import { useEffect, useState } from "react";
import api from "../api/axiosInstance";
import Loader from "../components/Loader";
import { getErrorMessage } from "../api/apiUtils";
import EventCard from "../components/EventCard/EventCard";
import EventDetailsModal from "../components/EventDetailsModal/EventDetailsModal";
import BookingModal from "../components/BookingModal/BookingModal";
import styles from "./NearbyEvents.module.css";


function NearbyEvents() {
  const [events, setEvents] = useState([]);
  const [savedEventIds, setSavedEventIds] = useState([]);
  const [savingEventId, setSavingEventId] = useState(null);

  const [radius, setRadius] = useState("25");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Event details modal
  const [selectedEvent, setSelectedEvent] = useState(null);

  // Booking modal
  const [bookingEvent, setBookingEvent] = useState(null);
  const [bookingLoading, setBookingLoading] = useState(false);

  // Fetch nearby events
  const fetchNearbyEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/customer/nearby-events", {
        params: {
          radiusKm: Number(radius),
        },
      });

      setEvents(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load nearby events."));
    } finally {
      setLoading(false);
    }
  };

  // Fetch saved event IDs
  const fetchSavedEvents = async () => {
    try {
      const response = await api.get("/customer/saved-events");

      const ids = Array.isArray(response.data)
        ? response.data
            .map((item) => item.event?.id)
            .filter((id) => id !== undefined && id !== null)
        : [];

      setSavedEventIds(ids);
    } catch (err) {
      console.error("Unable to load saved events:", err);
    }
  };

  useEffect(() => {
    fetchNearbyEvents();
    fetchSavedEvents();
  }, []);

  // Save or unsave an event
  const handleSaveEvent = async (eventId) => {
    try {
      setSavingEventId(eventId);
      setMessage("");

      if (savedEventIds.includes(eventId)) {
        await api.delete(`/customer/saved-events/${eventId}`);

        setSavedEventIds((previous) =>
          previous.filter((id) => id !== eventId)
        );

        setMessage("Event removed from saved events.");
      } else {
        await api.post(`/customer/saved-events/${eventId}`);

        setSavedEventIds((previous) =>
          previous.includes(eventId) ? previous : [...previous, eventId]
        );

        setMessage("Event saved successfully.");
      }
    } catch (err) {
      setMessage(getErrorMessage(err, "Unable to update saved event."));
    } finally {
      setSavingEventId(null);
    }
  };

  // Open event details modal
  const handleViewDetails = (event) => {
    setBookingEvent(null);
    setSelectedEvent(event);
  };

  // Open booking modal
  const handleBookTickets = (event) => {
    setSelectedEvent(null);
    setBookingEvent(event);
    setMessage("");
  };

  // Confirm booking
  const confirmBooking = async (quantity) => {
    if (!bookingEvent) return;

    try {
      setBookingLoading(true);
      setMessage("");

      await api.post("/customer/bookEvent", null, {
        params: {
          eventId: bookingEvent.id,
          ticketQuantity: quantity,
        },
      });

      setMessage("Tickets booked successfully!");
      setBookingEvent(null);

      // Refresh nearby events and saved events
      await fetchNearbyEvents();
      await fetchSavedEvents();
    } catch (err) {
      setMessage(getErrorMessage(err, "Unable to book tickets."));
    } finally {
      setBookingLoading(false);
    }
  };

  return (
    <div className="events-page nearby-events-page">
      <h1>Nearby Events 📍</h1>

      <p>Discover approved events close to your preferred location.</p>

      {/* Radius filter */}
      <div className="nearby-filter">
        <div>
          <label htmlFor="radius">Search radius (kilometres)</label>

          <input
            id="radius"
            type="number"
            min="1"
            max="500"
            value={radius}
            onChange={(e) => setRadius(e.target.value)}
          />
        </div>

        <button
          type="button"
          disabled={
            loading ||
            radius === "" ||
            Number(radius) <= 0 ||
            Number(radius) > 500
          }
          onClick={fetchNearbyEvents}
        >
          {loading ? "Searching..." : "Find Nearby Events"}
        </button>
      </div>

      {/* Success and error messages */}
      {message && <p className="success-message">{message}</p>}
      {error && <p className="error-message">{error}</p>}

      {/* Loader */}
      {loading && <Loader message="Finding nearby events..." />}

      {/* No events found */}
      {!loading && !error && events.length === 0 && (
        <p>No events found within {radius} km.</p>
      )}

      {/* Nearby event cards */}
      <div className={styles.eventsGrid}>
        {events.map((item) => {
          const event = item.event;

          if (!event) return null;

          return (
            <EventCard
              key={event.id}
              event={event}
              distanceKm={item.distanceKm}
              isSaved={savedEventIds.includes(event.id)}
              isSaving={savingEventId === event.id}
              onSave={handleSaveEvent}
              onViewDetails={handleViewDetails}
              onBook={handleBookTickets}
            />
          );
        })}
      </div>

      {/* Event details modal */}
      <EventDetailsModal
        event={selectedEvent}
        onClose={() => setSelectedEvent(null)}
        onSave={handleSaveEvent}
        onBook={handleBookTickets}
        isSaved={
          selectedEvent
            ? savedEventIds.includes(selectedEvent.id)
            : false
        }
        isSaving={
          selectedEvent
            ? savingEventId === selectedEvent.id
            : false
        }
      />

      {/* Booking modal */}
      <BookingModal
        event={bookingEvent}
        onClose={() => setBookingEvent(null)}
        onConfirm={confirmBooking}
        loading={bookingLoading}
      />
    </div>
  );
}

export default NearbyEvents;