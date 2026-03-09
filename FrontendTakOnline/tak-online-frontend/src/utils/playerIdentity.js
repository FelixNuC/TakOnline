const NICKNAME_STORAGE_KEY = "tak.nickname";

function getStoredNickname() {
  try {
    return localStorage.getItem(NICKNAME_STORAGE_KEY) || "";
  } catch {
    return "";
  }
}

function saveNickname(nickname) {
  try {
    localStorage.setItem(NICKNAME_STORAGE_KEY, nickname);
  } catch {
    // Ignore localStorage errors and continue with runtime nickname
  }
}

function generateGuestNickname() {
  const rawId =
    typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
      ? crypto.randomUUID().replace(/-/g, "")
      : Math.random().toString(36).slice(2);

  return `Guest-${rawId.slice(0, 6).toUpperCase()}`;
}

function resolveNickname(input) {
  const trimmed = (input || "").trim();
  return trimmed || generateGuestNickname();
}

export { getStoredNickname, saveNickname, resolveNickname };
