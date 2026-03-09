const API_BASE_URL = "http://localhost:8080";

async function toServiceError(response, fallbackMessage) {
  const rawText = await response.text();
  let message = rawText || fallbackMessage;

  try {
    const parsed = JSON.parse(rawText);
    if (parsed?.error) {
      message = parsed.error;
    } else if (parsed?.message) {
      message = parsed.message;
    }
  } catch {
    // Ignore non-JSON error payloads.
  }

  const error = new Error(message || fallbackMessage);
  error.status = response.status;
  throw error;
}

export async function createRoom(playerName, boardSize = 5, gameMode = "PVP", aiDifficulty = "NORMAL") {
  const response = await fetch(`${API_BASE_URL}/api/rooms`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      playerName,
      boardSize,
      gameMode,
      aiDifficulty,
    }),
  });

  if (!response.ok) {
    await toServiceError(response, "Error creating room");
  }

  return await response.json();
}

export async function joinRoom(code, playerName) {
  const response = await fetch(`${API_BASE_URL}/api/rooms/${code}/join`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      playerName,
    }),
  });

  if (!response.ok) {
    await toServiceError(response, "Error joining room");
  }

  return await response.json();
}

export async function getRoom(code) {
  const response = await fetch(`${API_BASE_URL}/api/rooms/${code}`);

  if (!response.ok) {
    await toServiceError(response, "Error loading room");
  }

  return await response.json();
}
