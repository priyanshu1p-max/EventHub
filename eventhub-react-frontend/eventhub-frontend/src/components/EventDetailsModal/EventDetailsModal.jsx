
import styles from "./EventDetailsModal.module.css";

function EventDetailsModal({
  event,
  onClose,
  onSave,
  onBook,
  isSaved = false,
  isSaving = false,
}) {
  if (!event) return null;

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

  const images = event.imageUrls?.length
    ? event.imageUrls
    : event.imageUrl
      ? [event.imageUrl]
      : [];

  return (
    <div
      className={styles.overlay}
      onClick={onClose}
    >
      <section
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="event-modal-title"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className={styles.closeButton}
          onClick={onClose}
          aria-label="Close event details"
        >
          ✕
        </button>

        {/* Event image gallery */}
        <div className={styles.imageGallery}>
          {images.length > 0 ? (
            images.map((url, index) => (
              <img
                key={`${url}-${index}`}
                src={getImageUrl(url)}
                alt={`${event.name} - image ${index + 1}`}
                onError={(e) => {
                  e.currentTarget.style.display = "none";
                }}
              />
            ))
          ) : (
            <div className={styles.noImage}>
              No images available
            </div>
          )}
        </div>

        {/* Event information */}
        <div className={styles.eventInformation}>
          <h2 id="event-modal-title">{event.name}</h2>

          <p className={styles.description}>
            {event.description || "No description available."}
          </p>

          <p>
            <strong>Category:</strong>{" "}
            {event.category || "Other"}
          </p>

          <p>
            <strong>Location:</strong>{" "}
            {event.location || "Not available"}
          </p>

          <p>
            <strong>Date:</strong>{" "}
            {formatDate(event.eventDateTime)}
          </p>

          <p>
            <strong>Ticket price:</strong> ₹{event.ticketPrice}
          </p>

          <p>
            <strong>Available tickets:</strong>{" "}
            {event.availableTickets}
          </p>
        </div>

        {/* Modal actions */}
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.saveButton}
            onClick={() => onSave?.(event.id)}
            disabled={isSaving}
          >
            {isSaving
              ? "Please wait..."
              : isSaved
                ? "♥ Saved"
                : "♡ Save Event"}
          </button>

          <button
            type="button"
            className={styles.bookButton}
            onClick={() => onBook?.(event)}
            disabled={event.availableTickets < 1}
          >
            {event.availableTickets < 1
              ? "Sold Out"
              : "Book Tickets"}
          </button>
        </div>
      </section>
    </div>
  );
}

export default EventDetailsModal;
