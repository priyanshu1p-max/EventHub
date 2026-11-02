import { useEffect, useState } from "react";
import api from "../api/axiosInstance";
import Loader from "../components/Loader";
import { getErrorMessage } from "../api/apiUtils";

function AdminDashboard() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // NEW: Organiser management states
  const [activeTab, setActiveTab] = useState("events");
  const [organisers, setOrganisers] = useState([]);
  const [organiserLoading, setOrganiserLoading] = useState(false);
  const [organiserError, setOrganiserError] = useState("");

  // Fetch pending events
  const fetchPendingEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/findEventByStatusPending");

      const data = response.data;

      setEvents(Array.isArray(data) ? data : data.content || data.events || []);
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load pending events."));
    } finally {
      setLoading(false);
    }
  };

  // NEW: Fetch all registered organisers
  const fetchOrganisers = async () => {
    try {
      setOrganiserLoading(true);
      setOrganiserError("");

      const response = await api.get("/admin/organisers");

      setOrganisers(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setOrganiserError(getErrorMessage(err, "Unable to load organisers."));
    } finally {
      setOrganiserLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingEvents();
  }, []);

  // Approve or reject an event
  const handleStatusChange = async (eventId, status) => {
    try {
      setProcessingId(eventId);
      setError("");
      setMessage("");

      await api.post("/admin/changeEventStatus", null, {
        params: {
          eventId,
          status,
        },
      });

      setMessage(`Event ${eventId} has been ${status.toLowerCase()}.`);

      await fetchPendingEvents();
    } catch (err) {
      setError(getErrorMessage(err, "Unable to update event status."));
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="admin-page">
      <div className="dashboard-hero admin-hero">
        <div>
          <p className="hero-kicker">Review & oversight</p>
          <h1>Admin Dashboard</h1>
        </div>

        <div className="hero-pills">
          <span>Pending</span>
          <span>Organisers</span>
          <span>Approvals</span>
        </div>
      </div>

      {/* Admin Header */}
      <div className="admin-header">
        <div>
          <p className="dashboard-subtitle">
            Manage events and registered organisers.
          </p>
        </div>

        <button
          type="button"
          className="refresh-button"
          onClick={
            activeTab === "events" ? fetchPendingEvents : fetchOrganisers
          }
          disabled={activeTab === "events" ? loading : organiserLoading}
        >
          Refresh
        </button>
      </div>

      {/* NEW: Admin Dashboard Tabs */}
      <div className="admin-tabs">
        <button
          type="button"
          className={
            activeTab === "events" ? "active tab-button" : "tab-button"
          }
          onClick={() => setActiveTab("events")}
        >
          Pending Events
        </button>

        <button
          type="button"
          className={
            activeTab === "organisers" ? "active tab-button" : "tab-button"
          }
          onClick={() => {
            setActiveTab("organisers");
            fetchOrganisers();
          }}
        >
          Organisers
        </button>
      </div>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {message && activeTab === "events" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}

      {/* ================================== */}
      {/* EXISTING: PENDING EVENTS SECTION */}
      {/* ================================== */}

      {activeTab === "events" && (
        <>
          {loading ? (
            <Loader message="Loading pending events..." />
          ) : events.length === 0 ? (
            <div className="admin-empty">
              <h2>No Pending Events</h2>
              <p>There are no events waiting for approval.</p>
            </div>
          ) : (
            <>
              <h2>Pending Events ({events.length})</h2>

              <div className="admin-events-grid">
                {events.map((event) => (
                  <article className="admin-event-card" key={event.id}>
                    {event.imageUrl && (
                      <img
                        src={
                          event.imageUrl.startsWith("http")
                            ? event.imageUrl
                            : `http://localhost:8080${event.imageUrl}`
                        }
                        alt={event.name || "Event"}
                      />
                    )}

                    <div className="admin-event-content">
                      <h3>{event.name}</h3>

                      <p>{event.description}</p>

                      <p>
                        <strong>Location:</strong> {event.location}
                      </p>

                      <p>
                        <strong>Date:</strong>{" "}
                        {event.eventDateTime
                          ? new Date(event.eventDateTime).toLocaleString(
                              "en-IN",
                            )
                          : "Not available"}
                      </p>

                      <p>
                        <strong>Ticket price:</strong> ₹{event.ticketPrice}
                      </p>

                      <p>
                        <strong>Available tickets:</strong>{" "}
                        {event.availableTickets}
                      </p>

                      <p>
                        <strong>Status:</strong> {event.status || "PENDING"}
                      </p>

                      <div className="admin-event-actions">
                        <button
                          type="button"
                          className="approve-btn"
                          disabled={processingId !== null}
                          onClick={() =>
                            handleStatusChange(event.id, "APPROVED")
                          }
                        >
                          {processingId === event.id
                            ? "Processing..."
                            : "Approve"}
                        </button>

                        <button
                          type="button"
                          className="reject-btn"
                          disabled={processingId !== null}
                          onClick={() =>
                            handleStatusChange(event.id, "REJECTED")
                          }
                        >
                          {processingId === event.id
                            ? "Processing..."
                            : "Reject"}
                        </button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </>
          )}
        </>
      )}

      {/* ================================== */}
      {/* NEW: ORGANISER MANAGEMENT SECTION */}
      {/* ================================== */}

      {activeTab === "organisers" && (
        <section className="organiser-management">
          <div className="organiser-heading">
            <div>
              <h2>Registered Organisers</h2>
              <p>View organiser contact information.</p>
            </div>

            <button
              type="button"
              onClick={fetchOrganisers}
              disabled={organiserLoading}
            >
              {organiserLoading ? "Refreshing..." : "Refresh Organisers"}
            </button>
          </div>

          {organiserError && (
            <p className="error-message" role="alert">
              {organiserError}
            </p>
          )}

          {organiserLoading ? (
            <Loader message="Loading organisers..." />
          ) : organisers.length === 0 ? (
            <div className="admin-empty">
              <h2>No Organisers Found</h2>
              <p>No organisers have registered yet.</p>
            </div>
          ) : (
            <>
              <h3>Total Organisers: {organisers.length}</h3>

              <div className="organiser-table-container">
                <table className="organiser-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Phone Number</th>
                    </tr>
                  </thead>

                  <tbody>
                    {organisers.map((organiser) => (
                      <tr key={organiser.id}>
                        <td>{organiser.id}</td>
                        <td>{organiser.name}</td>
                        <td>{organiser.email}</td>
                        <td>{organiser.phone || "Not provided"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </section>
      )}
    </div>
  );
}

export default AdminDashboard;
