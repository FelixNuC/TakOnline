const API_BASE_URL = "http://localhost:8080";

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
    const errorText = await response.text();
    throw new Error(errorText || "Error creating room");
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
    const errorText = await response.text();
    throw new Error(errorText || "Error joining room");
  }

  return await response.json();
}

export async function getRoom(code) {
  const response = await fetch(`${API_BASE_URL}/api/rooms/${code}`);

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error loading room");
  }

  return await response.json();
}
