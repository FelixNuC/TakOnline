import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createRoom } from "../../services/roomService";
import { getStoredNickname, resolveNickname, saveNickname } from "../../utils/playerIdentity";
import { CreateRoomIcon } from "../common/ArtDecoIcons";

function CreateRoomCard({ isActive, onOpen, onClose }) {
  const [nickname, setNickname] = useState(() => getStoredNickname());
  const [boardSize, setBoardSize] = useState(5);
  const [gameMode, setGameMode] = useState("PVP");
  const [aiDifficulty, setAiDifficulty] = useState("NORMAL");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const effectiveNickname = resolveNickname(nickname);

    try {
      setLoading(true);

      const room = await createRoom(
        effectiveNickname,
        Number(boardSize),
        gameMode,
        aiDifficulty
      );
      console.log("Room created:", room);
      const player = room?.players?.find((p) => p.playerName === effectiveNickname) || null;

      saveNickname(effectiveNickname);
      setNickname(effectiveNickname);

      navigate(`/room/${room.roomCode}`, {
        state: {
          room,
          playerName: effectiveNickname,
          playerId: player?.playerId || null,
          player,
        },
      });
    } catch (err) {
      console.error(err);
      setError("Could not create room");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`menu-card ${isActive ? "active" : ""}`}>
      {!isActive ? (
        <button className="menu-button" onClick={onOpen}>
          <CreateRoomIcon className="menu-card-icon" />
          Create room
        </button>
      ) : (
        <div className="menu-card-content">
          <div className="menu-card-header">
            <h2>
              <CreateRoomIcon className="menu-card-header-icon" />
              <span>Create room</span>
            </h2>
            <button className="close-button" onClick={onClose} type="button">
              &times;
            </button>
          </div>

          <form onSubmit={handleSubmit} className="menu-form">
            <label htmlFor="create-nickname">Nickname</label>
            <input
              id="create-nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="Opcional"
            />

            <label htmlFor="board-size">Board size</label>
            <select
              id="board-size"
              value={boardSize}
              onChange={(e) => setBoardSize(e.target.value)}
            >
              <option value={3}>3 x 3</option>
              <option value={4}>4 x 4</option>
              <option value={5}>5 x 5</option>
              <option value={6}>6 x 6</option>
            </select>

            <label htmlFor="game-mode">Game mode</label>
            <select
              id="game-mode"
              value={gameMode}
              onChange={(e) => setGameMode(e.target.value)}
            >
              <option value="PVP">Player vs Player</option>
              <option value="AI">Player vs AI</option>
            </select>

            {gameMode === "AI" && (
              <>
                <label htmlFor="ai-difficulty">AI difficulty</label>
                <select
                  id="ai-difficulty"
                  value={aiDifficulty}
                  onChange={(e) => setAiDifficulty(e.target.value)}
                >
                  <option value="EASY">Easy</option>
                  <option value="NORMAL">Normal</option>
                  <option value="HARD">Hard</option>
                </select>
              </>
            )}

            {error && <p className="form-error">{error}</p>}

            <button type="submit" className="submit-button" disabled={loading}>
              {loading ? "Creating..." : "Create"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default CreateRoomCard;
