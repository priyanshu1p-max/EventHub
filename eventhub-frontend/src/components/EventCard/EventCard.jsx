
import styles from "./EventCard.module.css";

function EventCard({
  event,
  distanceKm,
  isSaved = false,
  isSaving = false,
  onSave,
  onViewDetails,
  onBook,
}) {
  const getImageUrl = (imageUrl) => {
    if (!imageUrl) return null;

    if (
      imageUrl.startsWith("http://") ||
      imageUrl.startsWith("https://")
    ) {
      return imageUrl;
    }

    return `http://localhost:8080${
      imageUrl.startsWith("/") ? "" : "/"
    }${imageUrl}`;
  };

  const formatDate = (date) => {
    if (!date) return "Date not available";

    return new Date(date).toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  // Check whether event booking is closed
  const isBookingClosed = () => {
    if (!event.eventDateTime) return true;

    const eventTime = new Date(event.eventDateTime).getTime();

    if (Number.isNaN(eventTime)) return true;

    return eventTime <= Date.now();
  };

  const bookingClosed = isBookingClosed();
  const soldOut = Number(event.availableTickets) < 1;

  const imageUrl = getImageUrl(
    event.imageUrls?.[0] || event.imageUrl
  );

  return (
    <article className={styles.eventCard}>
      {imageUrl ? (
        <img
          className={styles.eventImage}
          src={imageUrl}
          alt={event.name || "Event"}
        />
      ) : (
        <div className={styles.imagePlaceholder}>
          No image available
        </div>
      )}

      <div className={styles.eventContent}>
        <h2>{event.name}</h2>

        <p className={styles.description}>
          {event.description}
        </p>

        <p>📂 {event.category || "Other"}</p>
        <p>📍 {event.location}</p>

        {distanceKm !== undefined && (
          <p>📏 {Number(distanceKm).toFixed(1)} km away</p>
        )}

        <p>📅 {formatDate(event.eventDateTime)}</p>
        <p>🎟️ ₹{event.ticketPrice} per ticket</p>

        <p>Available tickets: {event.availableTickets}</p>

        {bookingClosed && (
          <p className={styles.description}>
            Booking is closed because this event has started or is completed.
          </p>
        )}

        <div className={styles.actions}>
          <button
            type="button"
            onClick={() => onViewDetails?.(event)}
          >
            View Details
          </button>

          <button
            type="button"
            onClick={() => onBook?.(event)}
            disabled={bookingClosed || soldOut}
          >
            {bookingClosed
              ? "Booking Closed"
              : soldOut
                ? "Sold Out"
                : "Book Tickets"}
          </button>

          <button
            type="button"
            onClick={() => onSave?.(event.id)}
            disabled={isSaving}
          >
            {isSaving
              ? "Please wait..."
              : isSaved
                ? "❤️ Saved"
                : "♡ Save Event"}
          </button>
        </div>
      </div>
    </article>
  );
}

export default EventCard;
