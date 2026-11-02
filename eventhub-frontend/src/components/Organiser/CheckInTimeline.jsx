import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import api from "../../api/axiosInstance";

import "./CheckInTimeline.css";

const CheckInTimeline = ({ eventId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [connected, setConnected] = useState(false);

  // Load previous check-in activities.
  useEffect(() => {
    if (!eventId) return;

    let cancelled = false;

    const fetchActivities = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await api.get(
          `/organiser/events/${eventId}/checkin-activities`
        );

        if (!cancelled) {
          setActivities(response.data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ||
            "Unable to load check-in activities."
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchActivities();

    return () => {
      cancelled = true;
    };
  }, [eventId]);

  // Subscribe to new check-ins.
  useEffect(() => {
    if (!eventId) return;

    const client = new Client({
      webSocketFactory: () =>
        new SockJS("http://localhost:8080/ws"),

      reconnectDelay: 5000,

      onConnect: () => {
        setConnected(true);

        client.subscribe(
          `/topic/events/${eventId}/checkins`,
          (message) => {
            const newActivity = JSON.parse(message.body);

            setActivities((previous) => {
              // Prevent duplicate entries if an update is received twice.
              if (
                previous.some(
                  (activity) =>
                    activity.ticketId === newActivity.ticketId
                )
              ) {
                return previous;
              }

              return [newActivity, ...previous].slice(0, 20);
            });
          }
        );
      },

      onDisconnect: () => {
        setConnected(false);
      },

      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers.message);
      },
    });

    client.activate();

    return () => {
      setConnected(false);
      client.deactivate();
    };
  }, [eventId]);

  return (
    <section className="checkin-timeline">
      <div className="timeline-header">
        <div>
          <h3>Check-in Activity</h3>
          <p>Recent attendee entries</p>
        </div>

        <span className={connected ? "live-status" : "offline-status"}>
          {connected ? "● Live" : "● Connecting..."}
        </span>
      </div>

      {loading && <p>Loading activities...</p>}

      {error && <p className="timeline-error">{error}</p>}

      {!loading && !error && activities.length === 0 && (
        <p>No attendees have checked in yet.</p>
      )}

      <div className="timeline-list">
        {activities.map((activity) => (
          <article
            className="timeline-item"
            key={activity.ticketId}
          >
            <div className="timeline-dot">✓</div>

            <div className="timeline-content">
              <strong>{activity.customerName}</strong>

              <p>{activity.message}</p>

              <small>
                Ticket #{activity.ticketId} · {activity.checkedInAt}
              </small>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
};

export default CheckInTimeline;