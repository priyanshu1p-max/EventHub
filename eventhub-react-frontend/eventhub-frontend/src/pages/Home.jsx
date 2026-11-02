import { Link } from "react-router-dom";

function Home() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("userRole");
  const isLoggedIn = Boolean(token && role);

  return (
    <main className="home-page relative overflow-hidden bg-slate-950 text-white">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(96,165,250,0.18),transparent_26%),radial-gradient(circle_at_bottom_right,_rgba(45,212,191,0.10),transparent_24%)]" />

      <section className="hero relative z-10">
        <div className="hero-copy rounded-[28px] border border-white/10 bg-white/5 p-3 backdrop-blur-sm">
          <p className="eyebrow">YOUR NEXT EXPERIENCE STARTS HERE</p>

          <h1 className="max-w-xl text-white">
            Discover Events.
            <br />
            Create Memories.
          </h1>

          <p className="hero-description text-slate-300">
            Find exciting events, connect with people, book your tickets, and
            make every moment memorable with EventHub.
          </p>

          <div className="hero-actions">
            {isLoggedIn ? (
              <>
                {role === "CUSTOMER" && (
                  <Link to="/events" className="primary-button">
                    Explore Events
                  </Link>
                )}

                {role === "ORGANISER" && (
                  <Link to="/organiser" className="primary-button">
                    Organiser Dashboard
                  </Link>
                )}

                {role === "ADMIN" && (
                  <Link to="/admin" className="primary-button">
                    Admin Dashboard
                  </Link>
                )}
              </>
            ) : (
              <>
                <Link to="/register" className="primary-button">
                  Get Started
                </Link>

                <Link to="/login" className="secondary-button">
                  Login
                </Link>
              </>
            )}
          </div>

          <div className="hero-metrics">
            <div className="metric-pill">
              <strong>25k+</strong>
              <span>Attendees</span>
            </div>
            <div className="metric-pill">
              <strong>1.4k</strong>
              <span>Events</span>
            </div>
            <div className="metric-pill">
              <strong>4.9/5</strong>
              <span>Ratings</span>
            </div>
          </div>

          <div className="search-panel" aria-label="Event search suggestions">
            <div className="search-bar">
              <span className="search-icon">⌕</span>
              <span className="search-placeholder">
                Search concerts, workshops, food trails...
              </span>
              <button type="button" className="small-button">
                Search
              </button>
            </div>
            <div className="chip-row" aria-label="Popular event categories">
              <span className="chip active">All</span>
              <span className="chip">Music</span>
              <span className="chip">Tech</span>
              <span className="chip">Food</span>
              <span className="chip">Sports</span>
            </div>
          </div>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="visual-panel main-panel">
            <span className="panel-tag">Trending now</span>
            <h3>Sunset Live Festival</h3>
            <div className="panel-row">
              <span>12 Nov 2026</span>
              <span>₹1499</span>
            </div>
            <div className="panel-progress">
              <span></span>
            </div>
          </div>

          <div className="visual-panel mini-panel">
            <p>Bookings this week</p>
            <strong>4.8k</strong>
            <span>+32% versus last week</span>
          </div>

          <div className="floating-badge">
            <span className="badge-dot"></span>
            Live • 240 tickets left
          </div>
        </div>
      </section>

      <section className="features-section relative z-10">
        <p className="eyebrow">WHY EVENTHUB?</p>
        <h2 className="text-white">
          Everything You Need for Great Experiences
        </h2>

        <div className="features-grid">
          <div className="feature-card bg-slate-900/80 ring-1 ring-white/10">
            <div className="feature-icon">🎟️</div>
            <h3>Discover Events</h3>
            <p>
              Explore exciting events, discover nearby experiences, and find
              something that matches your interests.
            </p>
          </div>

          <div className="feature-card bg-slate-900/80 ring-1 ring-white/10">
            <div className="feature-icon">⚡</div>
            <h3>Easy Booking</h3>
            <p>
              Book tickets easily, manage your reservations, and keep track of
              your bookings in one place.
            </p>
          </div>

          <div className="feature-card bg-slate-900/80 ring-1 ring-white/10">
            <div className="feature-icon">🔒</div>
            <h3>Secure Experience</h3>
            <p>
              Enjoy a convenient booking experience with secure account access
              and booking management.
            </p>
          </div>
        </div>
      </section>

      <section className="trust-section relative z-10">
        <p className="eyebrow">WHY PEOPLE LOVE US</p>
        <h2 className="text-white">Designed for memorable experiences</h2>

        <div className="trust-grid">
          <div className="trust-card">
            <div className="feature-icon">✅</div>
            <h3>Verified organisers</h3>
            <p>Every event is hosted by trusted creators and communities.</p>
          </div>

          <div className="trust-card">
            <div className="feature-icon">💳</div>
            <h3>Secure checkout</h3>
            <p>Fast, safe, and straightforward booking for every ticket.</p>
          </div>

          <div className="trust-card">
            <div className="feature-icon">📢</div>
            <h3>Instant updates</h3>
            <p>Stay informed with reminders, changes, and event alerts.</p>
          </div>

          <div className="trust-card">
            <div className="feature-icon">🎉</div>
            <h3>Community energy</h3>
            <p>Join experiences that bring people together in real life.</p>
          </div>
        </div>
      </section>

      <section className="how-section">
        <p className="eyebrow">SIMPLE AND EASY</p>
        <h2>How EventHub Works</h2>

        <div className="steps-grid">
          <div className="step-card">
            <span>01</span>
            <h3>Create Your Account</h3>
            <p>Register as a customer or organiser to get started.</p>
          </div>

          <div className="step-card">
            <span>02</span>
            <h3>Explore or Create</h3>
            <p>
              Discover events as a customer or publish events as an organiser.
            </p>
          </div>

          <div className="step-card">
            <span>03</span>
            <h3>Enjoy the Experience</h3>
            <p>
              Book tickets, manage your events, and enjoy memorable experiences.
            </p>
          </div>
        </div>
      </section>

      {!isLoggedIn && (
        <section className="cta-section">
          <h2>Ready to Discover Something Amazing?</h2>
          <p>Join EventHub and make your next experience unforgettable.</p>

          <Link to="/register" className="primary-button">
            Create Your Account
          </Link>
        </section>
      )}

      <footer className="home-footer">
        <h3>EventHub</h3>
        <p>Discover Events. Create Memories.</p>
        <p>© {new Date().getFullYear()} EventHub. All rights reserved.</p>
      </footer>
    </main>
  );
}

export default Home;
