
import { useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";
import api from "../../api/axiosInstance";
import "./QRScanner.css";

const QRScanner = ({ eventId }) => {
  const processingRef = useRef(false);

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [scanSession, setScanSession] = useState(0);

  useEffect(() => {
    let scanner = null;
    let cancelled = false;
    let scannerStarted = false;
    let stopPromise = null;

    // Stop the scanner only once.
    const stopScanner = async () => {
      if (stopPromise) {
        return stopPromise;
      }

      if (!scanner || !scannerStarted) {
        return;
      }

      // Mark stopped immediately to prevent duplicate calls.
      scannerStarted = false;

      stopPromise = (async () => {
        try {
          await scanner.stop();
        } catch (error) {
          console.warn("Scanner stop warning:", error);
        }
      })();

      return stopPromise;
    };

    const startCamera = async () => {
      try {
        scanner = new Html5Qrcode("organiser-qr-reader");

        await scanner.start(
          { facingMode: "environment" },
          {
            fps: 10,
            qrbox: { width: 250, height: 250 },
          },
          async (decodedText) => {
            // Prevent multiple scans of the same QR code.
            if (cancelled || processingRef.current) {
              return;
            }

            processingRef.current = true;
            setLoading(true);

            try {
              // Stop camera before sending ticket for verification.
              await stopScanner();

              if (cancelled) return;

              const response = await api.post(
                "/organiser/tickets/scan",
                { qrToken: decodedText }
              );

              if (!cancelled) {
                setResult({
                  valid: response.data.success,
                  message: response.data.message,
                  ticketId: response.data.ticketId,
                  eventName: response.data.eventName,
                  customerName: response.data.customerName,
                  checkedInAt: response.data.checkedInAt,
                });
              }
            } catch (error) {
              console.error("Ticket verification error:", error);

              if (!cancelled) {
                setResult({
                  valid: false,
                  message:
                    error.response?.data?.message ||
                    "Invalid ticket or ticket verification failed.",
                });
              }
            } finally {
              if (!cancelled) {
                setLoading(false);
              }
            }
          },
          () => {
            // Ignore continuous scanning messages.
          }
        );

        scannerStarted = true;

        // Handle component cleanup while camera was starting.
        if (cancelled) {
          await stopScanner();
        }

      } catch (error) {
        console.error("Camera startup error:", error);

        if (!cancelled) {
          setCameraError(
            "Unable to access camera. Please allow camera permission and try again."
          );
        }
      }
    };

    processingRef.current = false;
    setCameraError("");
    setResult(null);

    startCamera();

    return () => {
      cancelled = true;

      const cleanup = async () => {
        await stopScanner();

        if (scanner) {
          try {
            scanner.clear();
          } catch (error) {
            console.warn("Scanner clear warning:", error);
          }
        }
      };

      cleanup();
    };
  }, [scanSession, eventId]);

  const handleScanAgain = () => {
    setResult(null);
    setLoading(false);
    setCameraError("");
    processingRef.current = false;

    setScanSession((previous) => previous + 1);
  };

  return (
    <div className="qr-scanner-container">
      <h2>Ticket QR Scanner</h2>

      <p>Scan the QR code displayed on the customer's ticket.</p>

      <div id="organiser-qr-reader"></div>

      {cameraError && (
        <div className="camera-error">
          {cameraError}
        </div>
      )}

      {loading && (
        <p className="scanner-loading">
          Verifying ticket...
        </p>
      )}

      {result && (
        <div className="qr-popup-overlay">
          <div
            className={`qr-popup ${
              result.valid ? "valid-popup" : "invalid-popup"
            }`}
          >
            <div className="popup-icon">
              {result.valid ? "✓" : "✕"}
            </div>

            <h2>
              {result.valid ? "VALID TICKET" : "INVALID TICKET"}
            </h2>

            <p>{result.message}</p>

            {result.valid && (
              <div className="ticket-details">
                <p>
                  <strong>Ticket ID:</strong> {result.ticketId}
                </p>

                <p>
                  <strong>Customer:</strong> {result.customerName}
                </p>

                <p>
                  <strong>Event:</strong> {result.eventName}
                </p>

                <p>
                  <strong>Checked in:</strong> {result.checkedInAt}
                </p>
              </div>
            )}

            <button onClick={handleScanAgain}>
              Scan Next Ticket
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default QRScanner;
