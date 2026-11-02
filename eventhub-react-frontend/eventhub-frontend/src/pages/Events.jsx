import { useEffect, useState } from "react";

import api from "../api/axiosInstance";
import Loader from "../components/Loader";
import { getErrorMessage } from "../api/apiUtils";
import EventCard from "../components/EventCard/EventCard";
import EventDetailsModal from "../components/EventDetailsModal/EventDetailsModal";
import styles from "./Events.module.css";

// Load Razorpay Checkout script
const loadRazorpayScript = () => {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existingScript = document.querySelector(
      'script[src="https://checkout.razorpay.com/v1/checkout.js"]',
    );

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(true), {
        once: true,
      });

      existingScript.addEventListener("error", () => resolve(false), {
        once: true,
      });

      return;
    }

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;

    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);

    document.body.appendChild(script);
  });
};

function Events() {
  const [events, setEvents] = useState([]);
  const [keyword, setKeyword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [selectedEvent, setSelectedEvent] = useState(null);

  // Personalized recommendations
  const [recommendations, setRecommendations] = useState([]);
  const [recommendationLoading, setRecommendationLoading] = useState(false);
  const [recommendationError, setRecommendationError] = useState("");

  // Saved events
  const [savedEventIds, setSavedEventIds] = useState([]);
  const [savingEventId, setSavingEventId] = useState(null);
  const [savedMessage, setSavedMessage] = useState("");

  // Weekend discovery
  const [weekendEvents, setWeekendEvents] = useState([]);
  const [weekendLoading, setWeekendLoading] = useState(false);
  const [weekendError, setWeekendError] = useState("");
  const [weekendBudget, setWeekendBudget] = useState(500);
  const [weekendGroupSize, setWeekendGroupSize] = useState(1);

  // Convert backend image path into a usable URL
  const getImageUrl = (imageUrl) => {
    if (!imageUrl) return null;

    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
      return imageUrl;
    }

    return `http://localhost:8080${
      imageUrl.startsWith("/") ? "" : "/"
    }${imageUrl}`;
  };

  // Format event date and time
  const formatDate = (date) => {
    if (!date) return "Date not available";

    return new Date(date).toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  // Check whether booking is closed
  const isBookingClosed = (event) => {
    if (!event?.eventDateTime) return true;

    const eventTime = new Date(event.eventDateTime).getTime();

    if (Number.isNaN(eventTime)) return true;

    return eventTime <= Date.now();
  };

  // Get booking button text
  const getBookingLabel = (event) => {
    if (isBookingClosed(event)) {
      return "Booking Closed";
    }

    if (Number(event.availableTickets) < 1) {
      return "Sold Out";
    }

    return "Book Tickets";
  };

  // Fetch approved events
  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/customer/events", {
        params: {
          keyword,
          page,
          size: 10,
          sortBy: "eventDateTime",
          direction: "asc",
        },
      });

      setEvents(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load events."));
    } finally {
      setLoading(false);
    }
  };

  // Fetch personalized recommendations
  const fetchRecommendations = async () => {
    try {
      setRecommendationLoading(true);
      setRecommendationError("");

      const response = await api.get("/customer/recommendations");

      setRecommendations(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setRecommendationError(
        getErrorMessage(err, "Unable to load recommendations."),
      );
    } finally {
      setRecommendationLoading(false);
    }
  };

  // Fetch weekend events
  const fetchWeekendEvents = async () => {
    try {
      setWeekendLoading(true);
      setWeekendError("");

      const response = await api.get("/customer/discover/weekend", {
        params: {
          budget: weekendBudget,
          groupSize: weekendGroupSize,
        },
      });

      setWeekendEvents(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setWeekendError(getErrorMessage(err, "Unable to find weekend events."));
    } finally {
      setWeekendLoading(false);
    }
  };

  // Fetch saved events
  const fetchSavedEvents = async () => {
    try {
      const response = await api.get("/customer/saved-events");

      const data = Array.isArray(response.data) ? response.data : [];

      setSavedEventIds(
        data
          .map((item) => item.event?.id)
          .filter((id) => id !== undefined && id !== null),
      );
    } catch (err) {
      console.error("Unable to load saved events:", err);
    }
  };

  // Save or unsave an event
  const handleSaveEvent = async (eventId) => {
    try {
      setSavingEventId(eventId);
      setSavedMessage("");
      setError("");

      if (savedEventIds.includes(eventId)) {
        await api.delete(`/customer/saved-events/${eventId}`);

        setSavedEventIds((previous) => previous.filter((id) => id !== eventId));

        setSavedMessage("Event removed from favourites.");
      } else {
        await api.post(`/customer/saved-events/${eventId}`);

        setSavedEventIds((previous) =>
          previous.includes(eventId) ? previous : [...previous, eventId],
        );

        setSavedMessage("Event saved to favourites!");
      }
    } catch (err) {
      setError(getErrorMessage(err, "Unable to update saved events."));
    } finally {
      setSavingEventId(null);
    }
  };

  // Load events when page or keyword changes
  useEffect(() => {
    fetchEvents();
  }, [page, keyword]);

  // Load recommendations and saved events when page opens
  useEffect(() => {
    fetchRecommendations();
    fetchSavedEvents();
  }, []);

  // Open event details
  const handleViewDetails = (event) => {
    setSelectedEvent(event);
  };

  // Book event tickets using Razorpay
  const handleBooking = async (eventId, event = null) => {
    // Check event date before starting payment
    if (event && isBookingClosed(event)) {
      setError(
        "Ticket booking is closed. This event has already started or is completed.",
      );
      return;
    }

    if (event && Number(event.availableTickets) < 1) {
      setError("Sorry, this event is sold out.");
      return;
    }

    const input = window.prompt(
      "How many tickets would you like to book?",
      "1",
    );

    if (input === null) return;

    const quantity = Number(input);

    if (!Number.isInteger(quantity) || quantity < 1) {
      window.alert("Please enter a valid ticket quantity.");
      return;
    }

    try {
      setError("");
      setMessage("");

      // Load Razorpay Checkout
      const loaded = await loadRazorpayScript();

      if (!loaded) {
        setError(
          "Unable to load Razorpay Checkout. Please check your internet connection.",
        );
        return;
      }

      // Check again after loading Razorpay script
      if (event && isBookingClosed(event)) {
        setError("Ticket booking is closed. This event has already started.");
        return;
      }

      // Create pending booking and Razorpay order
      const orderResponse = await api.post("/customer/bookEvent", null, {
        params: {
          eventId,
          ticketQuantity: quantity,
        },
      });

      const order = orderResponse.data;

      // Validate backend response
      if (
        !order ||
        !order.bookingId ||
        !order.razorpayOrderId ||
        !order.keyId ||
        !order.amount ||
        !order.currency
      ) {
        throw new Error("Invalid payment order response from server.");
      }

      // Refresh events because tickets have been temporarily reserved
      await fetchEvents();

      // Configure Razorpay Checkout
      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: "EventHub",
        description: order.eventName || "Event ticket booking",
        order_id: order.razorpayOrderId,

        handler: async function (response) {
          try {
            setError("");
            setMessage("Verifying your payment...");

            const verificationResponse = await api.post(
              "/customer/verify-payment",
              {
                bookingId: order.bookingId,
                razorpay_order_id: response.razorpay_order_id,
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_signature: response.razorpay_signature,
              },
            );

            if (verificationResponse.status === 200) {
              setMessage("Payment successful! Your booking is confirmed.");

              await fetchEvents();
              fetchRecommendations();
            }
          } catch (err) {
            setMessage("");

            setError(
              "Payment was submitted, but booking confirmation is pending. Please check your bookings before trying again.",
            );

            console.error("Payment verification error:", err);
          }
        },

        modal: {
          ondismiss: function () {
            setMessage(
              "Checkout closed. Any unpaid ticket reservation will be released after its expiry time.",
            );
          },
        },

        theme: {
          color: "#247bff",
        },
      };

      // Create Razorpay Checkout instance
      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", function (response) {
        setMessage("");

        setError(
          response?.error?.description ||
            "Payment failed. Please check your payment status before trying again.",
        );
      });

      // Open Checkout
      razorpay.open();
    } catch (err) {
      setMessage("");

      setError(
        getErrorMessage(err, "Unable to start payment. Please try again."),
      );
    }
  };

  // EventCard passes the complete event object
  const handleCardBooking = (event) => {
    handleBooking(event.id, event);
  };

  // Save button used in recommendation cards and modal
  const renderSaveButton = (eventId) => (
    <button
      type="button"
      className={styles.secondaryButton}
      onClick={() => handleSaveEvent(eventId)}
      disabled={savingEventId === eventId}
    >
      {savingEventId === eventId
        ? "Please wait..."
        : savedEventIds.includes(eventId)
          ? "♥ Saved"
          : "♡ Save Event"}
    </button>
  );

  // Reusable event card props
  const getEventCardProps = (event) => ({
    event,
    isSaved: savedEventIds.includes(event.id),
    isSaving: savingEventId === event.id,
    onSave: handleSaveEvent,
    onViewDetails: handleViewDetails,
    onBook: handleCardBooking,
  });

  return (
    <div className={`${styles.eventsPage} min-h-screen text-slate-100`}>
      <h1 className="mb-3 text-4xl font-black tracking-tight text-white">
        Explore Events
      </h1>

      <p
        className={`${styles.pageDescription} mb-8 max-w-2xl text-base text-slate-300`}
      >
        Discover events and book your tickets.
      </p>

      <div className={styles.heroStrip}>
        <div>
          <span className={styles.headlineBadge}>Curated picks</span>
          <h2>Find your next standout experience.</h2>
        </div>

        <div className={styles.statPills}>
          <span>Live deals</span>
          <span>Trending</span>
          <span>Weekend vibes</span>
        </div>
      </div>

      {/* Weekend Event Discovery */}
      <section className={styles.weekendSection}>
        <div className={styles.sectionHeaderRow}>
          <div>
            <h2>🎉 Discover This Weekend</h2>
            <p>Find events according to your budget and group size.</p>
          </div>
          <span className={styles.infoPill}>Smart planning</span>
        </div>

        <div className={styles.weekendFilters}>
          <div className={styles.filterField}>
            <label htmlFor="weekendBudget">Maximum budget per ticket (₹)</label>

            <input
              id="weekendBudget"
              type="number"
              min="0"
              value={weekendBudget}
              onChange={(e) => setWeekendBudget(e.target.value)}
            />
          </div>

          <div className={styles.filterField}>
            <label htmlFor="weekendGroupSize">Number of people</label>

            <input
              id="weekendGroupSize"
              type="number"
              min="1"
              value={weekendGroupSize}
              onChange={(e) => setWeekendGroupSize(e.target.value)}
            />
          </div>

          <button
            type="button"
            className={`${styles.primaryButton} rounded-xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow-lg shadow-blue-900/30 transition hover:shadow-blue-500/20`}
            disabled={
              weekendLoading ||
              weekendBudget === "" ||
              !Number.isFinite(Number(weekendBudget)) ||
              Number(weekendBudget) < 0 ||
              weekendGroupSize === "" ||
              !Number.isInteger(Number(weekendGroupSize)) ||
              Number(weekendGroupSize) < 1
            }
            onClick={fetchWeekendEvents}
          >
            {weekendLoading ? "Finding Events..." : "Find Weekend Events"}
          </button>
        </div>

        {weekendLoading && <Loader message="Finding weekend events..." />}

        {weekendError && <p className={styles.errorMessage}>{weekendError}</p>}

        {!weekendLoading && !weekendError && weekendEvents.length === 0 && (
          <p>No weekend events found. Try increasing your budget.</p>
        )}

        <div className={styles.eventsGrid}>
          {weekendEvents.map((event) => (
            <EventCard key={event.id} {...getEventCardProps(event)} />
          ))}
        </div>
      </section>

      {/* Personalized Recommendations */}
      <section className={styles.recommendationsSection}>
        <div className={styles.recommendationsHeading}>
          <div>
            <h2>Recommended For You ✨</h2>

            <p>Events selected based on your interests and preferences.</p>
          </div>

          <button
            type="button"
            className={styles.secondaryButton}
            onClick={fetchRecommendations}
            disabled={recommendationLoading}
          >
            {recommendationLoading ? "Loading..." : "Refresh"}
          </button>
        </div>

        {recommendationLoading && (
          <Loader message="Finding events for you..." />
        )}

        {recommendationError && (
          <p className={styles.errorMessage}>{recommendationError}</p>
        )}

        {!recommendationLoading &&
          !recommendationError &&
          recommendations.length === 0 && (
            <p>
              No recommendations yet. Save your preferences to get personalized
              event suggestions.
            </p>
          )}

        <div className={styles.recommendationsGrid}>
          {recommendations.slice(0, 4).map((item) => {
            const event = item.event;

            if (!event) return null;

            const bookingClosed = isBookingClosed(event);
            const soldOut = Number(event.availableTickets) < 1;

            return (
              <article className={styles.recommendationCard} key={event.id}>
                <div className={styles.recommendationScore}>
                  {item.score}/100 Match
                </div>

                <h3>{event.name}</h3>

                <p>📂 {event.category || "Other"}</p>
                <p>📍 {event.location}</p>
                <p>📅 {formatDate(event.eventDateTime)}</p>
                <p>🎟️ ₹{event.ticketPrice} per ticket</p>

                <p>Available tickets: {event.availableTickets}</p>

                {bookingClosed && (
                  <p className={styles.errorMessage}>
                    Booking closed: Event has started or completed.
                  </p>
                )}

                <div className={styles.recommendationReasons}>
                  <strong>Why recommended?</strong>

                  {item.reasons?.map((reason, index) => (
                    <p key={index}>✓ {reason}</p>
                  ))}
                </div>

                {renderSaveButton(event.id)}

                <button
                  type="button"
                  className={styles.secondaryButton}
                  onClick={() => handleViewDetails(event)}
                >
                  View Details
                </button>

                <button
                  type="button"
                  className={styles.primaryButton}
                  disabled={bookingClosed || soldOut}
                  onClick={() => handleBooking(event.id, event)}
                >
                  {getBookingLabel(event)}
                </button>
              </article>
            );
          })}
        </div>

        {savedMessage && (
          <p className={styles.successMessage}>{savedMessage}</p>
        )}
      </section>

      {/* Search Events */}
      <section className={styles.allEventsSection}>
        <h2>All Events</h2>

        <input
          className={styles.searchInput}
          type="text"
          placeholder="Search events by name or keyword..."
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value);
            setPage(0);
          }}
        />

        {loading && <Loader message="Loading events..." />}

        {message && <p className={styles.successMessage}>{message}</p>}

        {error && <p className={styles.errorMessage}>{error}</p>}

        {!loading && !error && events.length === 0 && (
          <p>No approved events found.</p>
        )}

        <div className={styles.eventsGrid}>
          {events.map((event) => (
            <EventCard key={event.id} {...getEventCardProps(event)} />
          ))}
        </div>

        {/* Pagination */}
        <div className={styles.pagination}>
          <button
            type="button"
            className={styles.secondaryButton}
            disabled={page === 0 || loading}
            onClick={() => setPage((current) => current - 1)}
          >
            Previous
          </button>

          <span>
            Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
          </span>

          <button
            type="button"
            className={styles.secondaryButton}
            disabled={page + 1 >= totalPages || loading}
            onClick={() => setPage((current) => current + 1)}
          >
            Next
          </button>
        </div>
      </section>

      {/* Event Details Modal */}
      <EventDetailsModal
        event={selectedEvent}
        onClose={() => setSelectedEvent(null)}
        onSave={handleSaveEvent}
        onBook={(event) => handleBooking(event.id, event)}
        isSaved={
          selectedEvent ? savedEventIds.includes(selectedEvent.id) : false
        }
        isSaving={selectedEvent ? savingEventId === selectedEvent.id : false}
      />
    </div>
  );
}

export default Events;
