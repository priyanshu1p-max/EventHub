import { useCallback, useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";

import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import "./TicketDashboard.css";

const TicketDashboard = ({ eventId }) => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [isConnected, setIsConnected] = useState(false);

  // Fetch dashboard data
  const fetchDashboard = useCallback(async () => {
    if (!eventId) return;

    try {
      setLoading(true);
      setError("");

      const response = await axiosInstance.get(
        `/organiser/events/${eventId}/dashboard`
      );

      setDashboard(response.data);
    } catch (err) {
      console.error("Dashboard error:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load event dashboard."
      );
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  // Initial dashboard loading
  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  // WebSocket: refresh dashboard after every check-in
  useEffect(() => {
    if (!eventId) return;

    const client = new Client({
      webSocketFactory: () =>
        new SockJS("http://localhost:8080/ws"),

      reconnectDelay: 5000,

      onConnect: () => {
        console.log("Dashboard WebSocket connected");
        setIsConnected(true);

        client.subscribe(
          `/topic/events/${eventId}/checkins`,
          () => {
            console.log("New check-in received. Updating dashboard...");
            fetchDashboard();
          }
        );
      },

      onDisconnect: () => {
        setIsConnected(false);
      },

      onWebSocketClose: () => {
        setIsConnected(false);
      },

      onStompError: (frame) => {
        console.error("STOMP error:", frame);
        setIsConnected(false);
      },
    });

    client.activate();

    return () => {
      setIsConnected(false);
      client.deactivate();
    };
  }, [eventId, fetchDashboard]);

  if (!eventId) {
    return (
      <p className="dashboard-message">
        Please select an event.
      </p>
    );
  }

  if (loading && !dashboard) {
    return (
      <p className="dashboard-message">
        Loading dashboard...
      </p>
    );
  }

  if (error && !dashboard) {
    return (
      <div className="dashboard-error">
        <p>{error}</p>
        <button onClick={fetchDashboard}>
          Try Again
        </button>
      </div>
    );
  }

  if (!dashboard) {
    return (
      <p className="dashboard-message">
        No dashboard data found.
      </p>
    );
  }

  const chartData = [
    {
      name: "Checked In",
      value: dashboard.attendeesCheckedIn,
    },
    {
      name: "Not Checked In",
      value: dashboard.attendeesNotCheckedIn,
    },
  ];

  const chartColors = ["#16a34a", "#dbeafe"];

  return (
    <div className="ticket-dashboard">

      {/* Dashboard Header */}
      <div className="dashboard-header">
        <div>
          <h2>Event Operations</h2>
          <p>{dashboard.eventName}</p>

          <span
            className={
              isConnected
                ? "websocket-status connected"
                : "websocket-status disconnected"
            }
          >
            {isConnected ? "● Live Updates" : "○ Connecting..."}
          </span>
        </div>

        <button
          onClick={fetchDashboard}
          className="refresh-button"
          disabled={loading}
        >
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {error && (
        <p className="dashboard-error">
          {error}
        </p>
      )}

      {/* Dashboard Statistics */}
      <div className="dashboard-stats">

        <div className="stat-card">
          <h3>Total Tickets Sold</h3>
          <p>{dashboard.totalTicketsSold}</p>
        </div>

        <div className="stat-card">
          <h3>Tickets Remaining</h3>
          <p>{dashboard.ticketsRemaining}</p>
        </div>

        <div className="stat-card">
          <h3>Attendees Checked In</h3>
          <p>{dashboard.attendeesCheckedIn}</p>
        </div>

        <div className="stat-card">
          <h3>Not Checked In</h3>
          <p>{dashboard.attendeesNotCheckedIn}</p>
        </div>

      </div>

      {/* Check-in Progress */}
      <div className="checkin-section">
        <h3>Check-in Progress</h3>

        <div className="progress-info">
          <span>Attendance</span>
          <strong>
            {dashboard.checkInPercentage}%
          </strong>
        </div>

        <div className="progress-track">
          <div
            className="progress-fill"
            style={{
              width: `${Math.min(
                Math.max(dashboard.checkInPercentage, 0),
                100
              )}%`,
            }}
          />
        </div>
      </div>

      {/* Check-in Pie Chart */}
      <div className="chart-section">
        <h3>Attendee Overview</h3>

        {dashboard.totalTicketsSold === 0 ? (
          <p>No tickets have been sold yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                outerRadius={100}
                label
              >
                {chartData.map((entry, index) => (
                  <Cell
                    key={entry.name}
                    fill={chartColors[index]}
                  />
                ))}
              </Pie>

              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>

    </div>
  );
};

export default TicketDashboard;