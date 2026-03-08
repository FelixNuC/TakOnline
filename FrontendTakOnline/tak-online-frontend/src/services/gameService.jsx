const API_BASE_URL = "http://localhost:8080";

export async function createGameFromRoom(roomCode) {
  const response = await fetch(`${API_BASE_URL}/api/games/from-room/${roomCode}`, {
    method: "POST",
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error creating game");
  }

  return await response.json();
}

export async function getGameByRoomCode(roomCode) {
  const response = await fetch(`${API_BASE_URL}/api/games/room/${roomCode}`);

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error loading game");
  }

  return await response.json();
}

export async function placePiece(gameId, payload) {
  const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/moves/place`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error placing piece");
  }

  return await response.json();
}

export async function moveStack(gameId, payload) {
  const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/moves/stack`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error moving stack");
  }

  return await response.json();
}

export async function requestRematch(gameId, payload) {
  const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/rematch`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Error requesting rematch");
  }

  return await response.json();
}
