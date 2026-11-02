import { useEffect, useState } from "react";
import styles from "./BookingModal.module.css";

function BookingModal({ event, onClose, onConfirm, loading = false }) {
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState("");

  // Reset quantity whenever a different event is selected
  useEffect(() => {
    setQuantity(1);
    setError("");
  }, [event]);

  if (!event) return null;

  const availableTickets = Number(event.availableTickets) || 0;
  const ticketPrice = Number(event.ticketPrice) || 0;
  const totalPrice = ticketPrice * quantity;

  const handleConfirm = () => {
    const parsedQuantity = Number(quantity);

    if (
      !Number.isInteger(parsedQuantity) ||
      parsedQuantity < 1 ||
      parsedQuantity > availableTickets
    ) {
      setError(`Enter a quantity between 1 and ${availableTickets}.`);
      return;
    }

    setError("");
    onConfirm(parsedQuantity);
  };

  return (
    <div
      className={styles.overlay}
      onClick={() => {
        if (!loading) onClose();
      }}
    >
      <div
        className={styles.modal}
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          className={styles.closeButton}
          onClick={onClose}
          disabled={loading}
        >
          ×
        </button>

        <h2>Book Tickets</h2>

        <h3>{event.name}</h3>

        <p>
          <strong>Ticket price:</strong> ₹{ticketPrice.toFixed(2)}
        </p>

        <p>
          <strong>Available tickets:</strong> {availableTickets}
        </p>

        <div className={styles.quantityField}>
          <label htmlFor="ticketQuantity">Number of tickets</label>

          <input
            id="ticketQuantity"
            type="number"
            min="1"
            max={availableTickets}
            value={quantity}
            disabled={loading || availableTickets < 1}
            onChange={(e) => {
              setQuantity(e.target.value);
              setError("");
            }}
          />
        </div>

        <div className={styles.total}>
          Total Amount: ₹{totalPrice.toFixed(2)}
        </div>

        {error && <p className={styles.error}>{error}</p>}

        {availableTickets < 1 && (
          <p className={styles.error}>No tickets are currently available.</p>
        )}

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </button>

          <button
            type="button"
            className={styles.confirmButton}
            onClick={handleConfirm}
            disabled={loading || availableTickets < 1}
          >
            {loading ? "Booking..." : "Confirm Booking"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default BookingModal;