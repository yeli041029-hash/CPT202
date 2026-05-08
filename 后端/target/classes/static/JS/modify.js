const SESSION_KEY = "heritage-platform-session-user";
const DEFAULT_AVATAR =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 180 180">
      <rect width="180" height="180" fill="#e5e7eb"/>
      <circle cx="90" cy="65" r="28" fill="#94a3b8"/>
      <path d="M40 145c10-27 29-40 50-40s40 13 50 40" fill="#94a3b8"/>
    </svg>
  `);

const defaultProfile = {
  username: "",
  identity: "Normal User",
  email: "",
  avatar: DEFAULT_AVATAR,
  phone: "",
  bio: "",
};

const form = document.getElementById("profileForm");
const avatarInput = document.getElementById("avatarInput");
const avatarPreview = document.getElementById("avatarPreview");
const backButton = document.getElementById("backButton");
const storageStatus = document.getElementById("storageStatus");
const resetButton = document.getElementById("resetButton");
const toast = document.getElementById("toast");
const sidebarUsername = document.getElementById("sidebarUsername");
const sidebarIdentity = document.getElementById("sidebarIdentity");

const BACK_TARGET = "../HTML/home.html";

let toastTimer = null;
let currentSessionUser = null;
let baselineProfile = null;
let currentAvatar = DEFAULT_AVATAR;

function normalizeIdentity(identity) {
  const identityMap = {
    USER: "Normal User",
    CONTRIBUTOR: "Contributor",
    ADMIN: "Admin",
    Admin: "Admin",
    "Normal User": "Normal User",
    "Pending Applicant": "Pending Applicant",
    Contributor: "Contributor",
  };

  return identityMap[identity] || identity || defaultProfile.identity;
}

function getCurrentSessionUser() {
  if (window.HeritageSession && typeof window.HeritageSession.getCurrentUser === "function") {
    return window.HeritageSession.getCurrentUser();
  }
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

function mapSessionRoleToIdentity(role) {
  const roleMap = {
    USER: "Normal User",
    CONTRIBUTOR: "Contributor",
    ADMIN: "Admin",
  };
  return roleMap[role] || defaultProfile.identity;
}

function hasOwnField(source, fieldName) {
  return Boolean(source) && Object.prototype.hasOwnProperty.call(source, fieldName);
}

function mergeProfileWithSession(profile, sessionUser) {
  return {
    ...defaultProfile,
    ...profile,
    username: hasOwnField(profile, "username") ? (profile.username || "") : (sessionUser?.username || defaultProfile.username),
    email: hasOwnField(profile, "email") ? (profile.email || "") : (sessionUser?.email || defaultProfile.email),
    phone: hasOwnField(profile, "phone") ? (profile.phone || "") : (sessionUser?.phone || defaultProfile.phone),
    bio: hasOwnField(profile, "bio") ? (profile.bio || "") : (sessionUser?.bio || defaultProfile.bio),
    identity: hasOwnField(profile, "role")
      ? normalizeIdentity(mapSessionRoleToIdentity(profile.role))
      : (hasOwnField(sessionUser, "role")
          ? normalizeIdentity(mapSessionRoleToIdentity(sessionUser.role))
          : defaultProfile.identity),
    avatar: hasOwnField(profile, "avatarUrl")
      ? (profile.avatarUrl || DEFAULT_AVATAR)
      : (hasOwnField(profile, "avatar")
          ? (profile.avatar || DEFAULT_AVATAR)
          : (sessionUser?.avatarUrl || defaultProfile.avatar)),
  };
}

function buildFallbackProfile(sessionUser) {
  return mergeProfileWithSession({}, sessionUser);
}

function updateSidebar(profile) {
  sidebarUsername.textContent = profile.username;
  sidebarIdentity.textContent = profile.identity;
}

function fillForm(profile) {
  document.getElementById("username").value = profile.username;
  document.getElementById("identity").value = profile.identity;
  document.getElementById("email").value = profile.email;
  document.getElementById("phone").value = profile.phone;
  document.getElementById("bio").value = profile.bio;
  currentAvatar = profile.avatar || DEFAULT_AVATAR;
  avatarPreview.src = currentAvatar;
  updateSidebar(profile);
}

function collectProfile() {
  return {
    username: document.getElementById("username").value.trim(),
    identity: normalizeIdentity(document.getElementById("identity").value),
    email: document.getElementById("email").value.trim(),
    phone: document.getElementById("phone").value.trim(),
    bio: document.getElementById("bio").value.trim(),
    avatar: currentAvatar || DEFAULT_AVATAR,
  };
}

function setStatus(message) {
  storageStatus.textContent = message;
}

function markUnsaved() {
  setStatus("Unsaved changes");
}

function updateSessionUser(user) {
  if (!user) {
    return;
  }
  const existing = currentSessionUser || getCurrentSessionUser() || {};
  const merged = { ...existing, ...user };
  currentSessionUser = merged;
  if (window.HeritageSession && typeof window.HeritageSession.saveCurrentUser === "function") {
    window.HeritageSession.saveCurrentUser(merged);
    return;
  }
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(merged));
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = typeof payload === "string"
      ? payload
      : (payload && payload.message) || "Request failed.";
    throw new Error(message);
  }

  return payload;
}

async function loadProfileFromDatabase() {
  currentSessionUser = getCurrentSessionUser();
  if (!currentSessionUser || !currentSessionUser.id) {
    baselineProfile = buildFallbackProfile(currentSessionUser);
    fillForm(baselineProfile);
    setStatus("Please log in again");
    return;
  }

  const profileDto = await fetchJson(`/api/zyl/profile/${currentSessionUser.id}`);
  updateSessionUser(profileDto);
  baselineProfile = mergeProfileWithSession(profileDto, currentSessionUser);
  fillForm(baselineProfile);
  setStatus("Synced with database");
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add("is-visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.remove("is-visible");
  }, 2200);
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!form.reportValidity()) {
    return;
  }

  const profile = collectProfile();
  if (!currentSessionUser || !currentSessionUser.id) {
    showToast("Please log in again before saving.");
    setStatus("Please log in again");
    return;
  }

  try {
    setStatus("Saving...");
    const savedUser = await fetchJson(`/api/zyl/profile/${currentSessionUser.id}`, {
      method: "PUT",
      body: JSON.stringify({
        username: profile.username,
        email: profile.email,
        phone: profile.phone,
        bio: profile.bio,
        avatarUrl: profile.avatar,
      }),
    });
    updateSessionUser(savedUser);
    baselineProfile = mergeProfileWithSession(savedUser, currentSessionUser);
    fillForm(baselineProfile);
    setStatus("Saved to database");
    showToast("Profile saved successfully.");
  } catch (error) {
    setStatus("Save failed");
    showToast(error.message || "Failed to save profile.");
  }
});

avatarInput.addEventListener("change", (event) => {
  const file = event.target.files && event.target.files[0];
  if (!file) {
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    currentAvatar = typeof reader.result === "string" ? reader.result : DEFAULT_AVATAR;
    avatarPreview.src = currentAvatar;
    markUnsaved();
  };
  reader.readAsDataURL(file);
});

resetButton.addEventListener("click", () => {
  fillForm(baselineProfile || buildFallbackProfile(currentSessionUser));
  setStatus("Default information restored");
  showToast("Default information restored.");
});

backButton.addEventListener("click", () => {
  if (BACK_TARGET) {
    window.location.href = BACK_TARGET;
    return;
  }

  window.alert("Back target is not configured yet. Update BACK_TARGET in script.js to enable navigation.");
});

[form.elements.username, form.elements.email, form.elements.phone, form.elements.bio, form.elements.identity].forEach((field) => {
  if (!field) {
    return;
  }
  field.addEventListener("input", markUnsaved);
  field.addEventListener("change", markUnsaved);
});

fillForm(buildFallbackProfile(getCurrentSessionUser()));

loadProfileFromDatabase().catch(() => {
  baselineProfile = buildFallbackProfile(getCurrentSessionUser());
  fillForm(baselineProfile);
  setStatus("Failed to sync profile");
});
