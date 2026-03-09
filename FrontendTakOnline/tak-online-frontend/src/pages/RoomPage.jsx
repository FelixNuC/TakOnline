import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getRoom } from "../services/roomService";
import { createGameFromRoom, getGameByRoomCode } from "../services/gameService";
import GameView from "../components/game/GameView";
import "../styles/room.css";

function RoomPage() {
  const { roomCode } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const playerStorageKey = `tak.player.${roomCode}`;
  const statePlayer = location.state?.player;
  const statePlayerId = statePlayer?.playerId || location.state?.playerId || null;
  const statePlayerName = statePlayer?.playerName || location.state?.playerName || null;

  const [room, setRoom] = useState(location.state?.room || null);
  const [game, setGame] = useState(null);
  const [playerContext, setPlayerContext] = useState(() => {
    if (statePlayer?.playerId) return statePlayer;

    if (statePlayerId || statePlayerName) {
      return { playerId: statePlayerId || null, playerName: statePlayerName || null };
    }

    try {
      const saved = sessionStorage.getItem(`tak.player.${roomCode}`);
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(!location.state?.room);
  const [startingGame, setStartingGame] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    async function loadRoom() {
      if (room) return;

      try {
        setLoading(true);
        const roomData = await getRoom(roomCode);
        setRoom(roomData);
      } catch (err) {
        console.error(err);
        setError("Could not load room");
      } finally {
        setLoading(false);
      }
    }

    loadRoom();
  }, [roomCode, room]);

  const refreshRoom = useCallback(async () => {
    const roomData = await getRoom(roomCode);
    setRoom(roomData);
    return roomData;
  }, [roomCode]);

  const refreshGame = useCallback(async () => {
    const gameData = await getGameByRoomCode(roomCode);
    setGame(gameData);
    return gameData;
  }, [roomCode]);

  useEffect(() => {
    async function loadExistingGame() {
      try {
        await refreshGame();
      } catch {
        // Si todavía no existe partida, no hacemos nada
        console.log("No existing game yet for this room");
      }
    }

    loadExistingGame();
  }, [refreshGame]);

  useEffect(() => {
    let isCancelled = false;

    async function syncRoomState() {
      try {
        const roomData = await getRoom(roomCode);
        if (!isCancelled) {
          setRoom(roomData);
        }
      } catch (err) {
        if (!isCancelled) {
          console.error("Could not refresh room in real time", err);
        }
      }

      try {
        const gameData = await getGameByRoomCode(roomCode);
        if (!isCancelled) {
          setGame(gameData);
        }
      } catch {
        // Puede no existir aún, ignoramos
      }
    }

    const intervalId = setInterval(syncRoomState, 1000);

    return () => {
      isCancelled = true;
      clearInterval(intervalId);
    };
  }, [roomCode]);

  const players = room?.players || [];
  const isReady = players.length >= 2;

  useEffect(() => {
    if (!players.length) return;

    let resolvedPlayer = null;

    if (playerContext?.playerId) {
      resolvedPlayer = players.find((p) => p.playerId === playerContext.playerId) || null;
    }

    if (!resolvedPlayer && playerContext?.playerName) {
      const byName = players.filter((p) => p.playerName === playerContext.playerName);
      if (byName.length === 1) {
        resolvedPlayer = byName[0];
      }
    }

    if (!resolvedPlayer && statePlayerId) {
      resolvedPlayer = players.find((p) => p.playerId === statePlayerId) || null;
    }

    if (!resolvedPlayer && statePlayerName) {
      const byName = players.filter((p) => p.playerName === statePlayerName);
      if (byName.length === 1) {
        resolvedPlayer = byName[0];
      }
    }

    if (!resolvedPlayer) return;

    const nextContext = {
      playerId: resolvedPlayer.playerId,
      playerName: resolvedPlayer.playerName,
    };

    if (
      playerContext?.playerId === nextContext.playerId &&
      playerContext?.playerName === nextContext.playerName
    ) {
      return;
    }

    setPlayerContext(nextContext);
    sessionStorage.setItem(playerStorageKey, JSON.stringify(nextContext));
  }, [
    players,
    playerContext?.playerId,
    playerContext?.playerName,
    statePlayerId,
    statePlayerName,
    playerStorageKey,
  ]);

  const statusLabel = useMemo(() => {
    if (!room?.status) return "Unknown";
    return room.status;
  }, [room]);

  const handleLeaveRoom = () => {
    navigate("/");
  };

  const handleCopyCode = async () => {
    try {
      await navigator.clipboard.writeText(room?.roomCode || "");
      setCopied(true);

      setTimeout(() => {
        setCopied(false);
      }, 1500);
    } catch (err) {
      console.error("Could not copy room code", err);
    }
  };

  const handleStartGame = async () => {
    try {
      setError("");
      setStartingGame(true);

      const gameData = await createGameFromRoom(roomCode);
      setGame(gameData);
      await refreshRoom();
    } catch (err) {
      console.error(err);
      setError("Could not start game");
    } finally {
      setStartingGame(false);
    }
  };

  const lobbyPageClassName = "room-page in-lobby";
  const gamePageClassName = "room-page in-game";

  if (loading) {
    return (
      <div className={lobbyPageClassName}>
        <button
          className="room-close-button"
          onClick={handleLeaveRoom}
          type="button"
        >
          ×
        </button>

        <div className="room-shell">
          <h1>Loading room...</h1>
        </div>
      </div>
    );
  }

  if (error && !room) {
    return (
      <div className={lobbyPageClassName}>
        <button
          className="room-close-button"
          onClick={handleLeaveRoom}
          type="button"
        >
          ×
        </button>

        <div className="room-shell">
          <h1>Error</h1>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (game) {
    return (
      <div className={gamePageClassName}>
        <button
          className="room-close-button"
          onClick={handleLeaveRoom}
          type="button"
        >
          ×
        </button>

        <div className="room-shell">
          <GameView
            game={game}
            currentPlayer={playerContext}
            onGameUpdate={setGame}
          />
        </div>
      </div>
    );
  }

  return (
    <div className={lobbyPageClassName}>
      <button
        className="room-close-button"
        onClick={handleLeaveRoom}
        type="button"
      >
        ×
      </button>

      <div className="room-shell">
        <section className="room-hero">
          <p className="room-eyebrow">Lobby</p>
          <h1 className="room-title">Room {room?.roomCode}</h1>
          <p className="room-subtitle">
            Share this code with your opponent to join the match.
          </p>

          <div className="room-code-box">
            <span className="room-code">{room?.roomCode}</span>
            <button
              className="copy-button"
              type="button"
              onClick={handleCopyCode}
            >
              {copied ? "Copied!" : "Copy code"}
            </button>
          </div>
        </section>

        <section className="room-info-grid">
          <div className="info-card">
            <span className="info-label">Status</span>
            <span className={`status-badge ${isReady ? "ready" : "waiting"}`}>
              {statusLabel}
            </span>
          </div>

          <div className="info-card">
            <span className="info-label">Board size</span>
            <span className="info-value">
              {room?.boardSize} x {room?.boardSize}
            </span>
          </div>

          <div className="info-card">
            <span className="info-label">Players</span>
            <span className="info-value">{players.length} / 2</span>
          </div>
        </section>

        <section className="players-card">
          <div className="section-header">
            <h2>Players</h2>
          </div>

          <div className="players-list">
            {players.map((player) => (
              <div className="player-row" key={player.playerId}>
                <div className="player-main">
                  <span className="player-name">{player.playerName}</span>
                  <div className="player-tags">
                    {player.host && <span className="player-tag">Host</span>}
                    <span className="player-tag">{player.color}</span>
                  </div>
                </div>
              </div>
            ))}

            {players.length < 2 && (
              <div className="empty-player-slot">
                Waiting for another player to join...
              </div>
            )}
          </div>
        </section>

        <section className="room-state-card">
          {!isReady ? (
            <>
              <h2>Waiting for opponent</h2>
              <p>
                Share the room code and keep this lobby open. When the second
                player joins, this room will be ready to start.
              </p>
            </>
          ) : (
            <>
              <h2>Match ready</h2>
              <p>
                Both players are in the room. You can start the game now and
                move to the board.
              </p>

              <button
                className="start-game-button"
                type="button"
                onClick={handleStartGame}
                disabled={startingGame}
              >
                {startingGame ? "Starting..." : "Start game"}
              </button>

              {error && <p className="form-error">{error}</p>}
            </>
          )}
        </section>
      </div>
    </div>
  );
}

export default RoomPage;
