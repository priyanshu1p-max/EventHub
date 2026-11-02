
import { useEffect, useState } from "react";
import api from "../../api/axiosInstance";
import { getErrorMessage } from "../../api/apiUtils";
import styles from "./ProfileForm.module.css";

const emptyProfile = {
  name: "",
  email: "",
  phone: "",
  gender: "",
  dateOfBirth: "",
  alternatePhone: "",
  address: "",
  country: "",
  state: "",
  city: "",
  postalCode: "",
  bio: "",
  organisationName: "",
  organisationType: "",
  organisationDescription: "",
  website: "",
  businessEmail: "",
  businessPhone: "",
  linkedinUrl: "",
  instagramUrl: "",
  facebookUrl: "",
  profileImageUrl: "",
  roleName: "",
  createdAt: "",
};

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function getImageUrl(path) {
  if (!path) return "";

  try {
    return new URL(path, API_BASE_URL).href;
  } catch {
    return path;
  }
}

function ProfileForm({ organiser = false }) {
  const [profile, setProfile] = useState(emptyProfile);
  const [originalProfile, setOriginalProfile] = useState(emptyProfile);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [editing, setEditing] = useState(false);

  const [selectedImage, setSelectedImage] = useState(null);
  const [previewImage, setPreviewImage] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Load profile
  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/profile/me");

      setProfile({
        ...emptyProfile,
        ...response.data,
        phone: response.data.phone ?? "",
        alternatePhone: response.data.alternatePhone ?? "",
      });

      setOriginalProfile({
        ...emptyProfile,
        ...response.data,
        phone: response.data.phone ?? "",
        alternatePhone: response.data.alternatePhone ?? "",
      });
    } catch (err) {
      setError(getErrorMessage(err, "Unable to load profile."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  // Input changes
  const handleChange = (e) => {
    const { name, value } = e.target;

    setProfile((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  // Save profile
  const handleSave = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (!profile.name.trim()) {
      setError("Full name is required.");
      return;
    }

    if (
      profile.phone &&
      !/^\d{10,15}$/.test(String(profile.phone))
    ) {
      setError("Enter a valid phone number.");
      return;
    }

    try {
      setSaving(true);

      const payload = {
        name: profile.name.trim(),
        phone: profile.phone ? Number(profile.phone) : null,
        gender: profile.gender || null,
        dateOfBirth: profile.dateOfBirth || null,
        alternatePhone: profile.alternatePhone
          ? Number(profile.alternatePhone)
          : null,
        address: profile.address,
        country: profile.country,
        state: profile.state,
        city: profile.city,
        postalCode: profile.postalCode,
        bio: profile.bio,
      };

      if (organiser) {
        Object.assign(payload, {
          organisationName: profile.organisationName,
          organisationType: profile.organisationType,
          organisationDescription: profile.organisationDescription,
          website: profile.website,
          businessEmail: profile.businessEmail,
          businessPhone: profile.businessPhone,
          linkedinUrl: profile.linkedinUrl,
          instagramUrl: profile.instagramUrl,
          facebookUrl: profile.facebookUrl,
        });
      }

      const response = await api.put("/profile/me", payload);

      const updated = {
        ...emptyProfile,
        ...response.data,
        phone: response.data.phone ?? "",
        alternatePhone: response.data.alternatePhone ?? "",
      };

      setProfile(updated);
      setOriginalProfile(updated);
      setEditing(false);
      setMessage("Profile updated successfully.");
    } catch (err) {
      setError(getErrorMessage(err, "Unable to update profile."));
    } finally {
      setSaving(false);
    }
  };

  // Select profile picture and show preview
  const handleImageChange = (e) => {
    const file = e.target.files?.[0];

    if (!file) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      setError("Please select a JPG, PNG or WebP image.");
      e.target.value = "";
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setError("Image size must not exceed 5 MB.");
      e.target.value = "";
      return;
    }

    setError("");
    setSelectedImage(file);

    const previewUrl = URL.createObjectURL(file);
    setPreviewImage(previewUrl);
  };

  // Upload profile picture
  const handleImageUpload = async () => {
    if (!selectedImage) {
      setError("Please select an image first.");
      return;
    }

    try {
      setUploading(true);
      setError("");
      setMessage("");

      const formData = new FormData();
      formData.append("image", selectedImage);

      const response = await api.post(
        "/profile/me/image",
        formData
      );

      const updated = {
        ...profile,
        ...response.data,
      };

      setProfile(updated);
      setOriginalProfile(updated);

      setSelectedImage(null);
      setPreviewImage("");
      setMessage("Profile picture updated successfully.");
    } catch (err) {
      setError(getErrorMessage(err, "Profile picture upload failed."));
    } finally {
      setUploading(false);
    }
  };

  // Cancel editing
  const handleCancel = () => {
    setProfile(originalProfile);
    setEditing(false);
    setSelectedImage(null);
    setPreviewImage("");
    setError("");
    setMessage("");
  };

  useEffect(() => {
    return () => {
      if (previewImage) {
        URL.revokeObjectURL(previewImage);
      }
    };
  }, [previewImage]);

  if (loading) {
    return <div className={styles.status}>Loading profile...</div>;
  }

  if (error && !profile.email) {
    return (
      <div className={styles.status}>
        <p className={styles.error}>{error}</p>
        <button onClick={fetchProfile}>Try Again</button>
      </div>
    );
  }

  const imageSrc =
    previewImage || getImageUrl(profile.profileImageUrl);

  return (
    <div className={styles.profilePage}>
      <div className={styles.heading}>
        <div>
          <h1>My Profile</h1>
          <p>Manage your personal and account information.</p>
        </div>

        {!editing && (
          <button
            type="button"
            className={styles.primaryButton}
            onClick={() => {
              setEditing(true);
              setMessage("");
              setError("");
            }}
          >
            Edit Profile
          </button>
        )}
      </div>

      {message && <div className={styles.success}>{message}</div>}
      {error && <div className={styles.error}>{error}</div>}

      <section className={styles.profileCard}>
        <div className={styles.profileHeader}>
          <div className={styles.avatar}>
            {imageSrc ? (
              <img src={imageSrc} alt="Profile" />
            ) : (
              <span>
                {profile.name?.charAt(0)?.toUpperCase() || "U"}
              </span>
            )}
          </div>

          <div className={styles.profileIdentity}>
            <h2>{profile.name || "Your Name"}</h2>
            <p>{profile.email}</p>
            <span className={styles.role}>
              {organiser ? "Event Organiser" : "Customer"}
            </span>
          </div>
        </div>

        {editing && (
          <div className={styles.photoUpload}>
            <label htmlFor="profileImage">Change profile picture</label>

            <input
              id="profileImage"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={handleImageChange}
            />

            {selectedImage && (
              <button
                type="button"
                onClick={handleImageUpload}
                disabled={uploading}
                className={styles.secondaryButton}
              >
                {uploading ? "Uploading..." : "Upload Picture"}
              </button>
            )}
          </div>
        )}

        <div className={styles.accountInfo}>
          <span>Account ID: {profile.id}</span>
          <span>
            Joined:{" "}
            {profile.createdAt
              ? new Date(profile.createdAt).toLocaleDateString("en-IN")
              : "Not available"}
          </span>
        </div>
      </section>

      <form onSubmit={handleSave}>
        <section className={styles.section}>
          <h2>Personal Information</h2>

          <div className={styles.formGrid}>
            <Field
              label="Full Name"
              name="name"
              value={profile.name}
              editing={editing}
              onChange={handleChange}
              required
            />

            <Field
              label="Email Address"
              name="email"
              value={profile.email}
              editing={false}
            />

            <Field
              label="Phone Number"
              name="phone"
              value={profile.phone}
              editing={editing}
              onChange={handleChange}
            />

            <Field
              label="Alternate Phone"
              name="alternatePhone"
              value={profile.alternatePhone}
              editing={editing}
              onChange={handleChange}
            />

            <div className={styles.field}>
              <label>Gender</label>
              <select
                name="gender"
                value={profile.gender || ""}
                onChange={handleChange}
                disabled={!editing}
              >
                <option value="">Select gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
                <option value="Prefer not to say">
                  Prefer not to say
                </option>
              </select>
            </div>

            <div className={styles.field}>
              <label>Date of Birth</label>
              <input
                type="date"
                name="dateOfBirth"
                value={profile.dateOfBirth || ""}
                onChange={handleChange}
                disabled={!editing}
                max={new Date().toISOString().split("T")[0]}
              />
            </div>

            <Field
              label="About You"
              name="bio"
              value={profile.bio}
              editing={editing}
              onChange={handleChange}
            />
          </div>
        </section>

        <section className={styles.section}>
          <h2>Address Information</h2>

          <div className={styles.formGrid}>
            <Field
              label="Address"
              name="address"
              value={profile.address}
              editing={editing}
              onChange={handleChange}
            />

            <Field
              label="Country"
              name="country"
              value={profile.country}
              editing={editing}
              onChange={handleChange}
            />

            <Field
              label="State"
              name="state"
              value={profile.state}
              editing={editing}
              onChange={handleChange}
            />

            <Field
              label="City"
              name="city"
              value={profile.city}
              editing={editing}
              onChange={handleChange}
            />

            <Field
              label="Postal Code"
              name="postalCode"
              value={profile.postalCode}
              editing={editing}
              onChange={handleChange}
            />
          </div>
        </section>

        {organiser && (
          <section className={styles.section}>
            <h2>Organisation Information</h2>

            <div className={styles.formGrid}>
              <Field
                label="Organisation Name"
                name="organisationName"
                value={profile.organisationName}
                editing={editing}
                onChange={handleChange}
              />

              <div className={styles.field}>
                <label>Organisation Type</label>
                <select
                  name="organisationType"
                  value={profile.organisationType || ""}
                  onChange={handleChange}
                  disabled={!editing}
                >
                  <option value="">Select type</option>
                  <option value="Event Management">Event Management</option>
                  <option value="Company">Company</option>
                  <option value="Educational Institution">
                    Educational Institution
                  </option>
                  <option value="Individual">Individual</option>
                  <option value="Non-Profit">Non-Profit</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <Field
                label="Business Email"
                name="businessEmail"
                value={profile.businessEmail}
                editing={editing}
                onChange={handleChange}
              />

              <Field
                label="Business Phone"
                name="businessPhone"
                value={profile.businessPhone}
                editing={editing}
                onChange={handleChange}
              />

              <Field
                label="Website"
                name="website"
                value={profile.website}
                editing={editing}
                onChange={handleChange}
              />

              <Field
                label="LinkedIn URL"
                name="linkedinUrl"
                value={profile.linkedinUrl}
                editing={editing}
                onChange={handleChange}
              />

              <Field
                label="Instagram URL"
                name="instagramUrl"
                value={profile.instagramUrl}
                editing={editing}
                onChange={handleChange}
              />

              <Field
                label="Facebook URL"
                name="facebookUrl"
                value={profile.facebookUrl}
                editing={editing}
                onChange={handleChange}
              />

              <div className={styles.field}>
                <label>Organisation Description</label>
                <textarea
                  name="organisationDescription"
                  value={profile.organisationDescription || ""}
                  onChange={handleChange}
                  disabled={!editing}
                  rows="4"
                />
              </div>
            </div>
          </section>
        )}

        {editing && (
          <div className={styles.actions}>
            <button
              type="button"
              className={styles.cancelButton}
              onClick={handleCancel}
              disabled={saving || uploading}
            >
              Cancel
            </button>

            <button
              type="submit"
              className={styles.primaryButton}
              disabled={saving || uploading}
            >
              {saving ? "Saving..." : "Save Changes"}
            </button>
          </div>
        )}
      </form>
    </div>
  );
}

function Field({
  label,
  name,
  value,
  editing,
  onChange,
  required = false,
}) {
  return (
    <div className={styles.field}>
      <label htmlFor={name}>{label}</label>
      <input
        id={name}
        name={name}
        value={value ?? ""}
        onChange={onChange}
        disabled={!editing}
        required={required}
      />
    </div>
  );
}

export default ProfileForm;
