import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { joinRoom } from "../../services/roomService";
import { getStoredNickname, resolveNickname, saveNickname } from "../../utils/playerIdentity";
import { JoinRoomIcon } from "../common/ArtDecoIcons";

function JoinRoomCard({ isActive, onOpen, onClose }) {
  const [nickname, setNickname] = useState(() => getStoredNickname());
  const [roomCode, setRoomCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const effectiveNickname = resolveNickname(nickname);

    try {
      setLoading(true);

      const room = await joinRoom(roomCode, effectiveNickname);
      console.log("Joined room:", room);
      const player = room?.players?.[room.players.length - 1] || null;

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
      setError("Could not join room");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`menu-card ${isActive ? "active" : ""}`}>
      {!isActive ? (
        <button className="menu-button" onClick={onOpen}>
          <JoinRoomIcon className="menu-card-icon" />
          Join room
        </button>
      ) : (
        <div className="menu-card-content">
          <div className="menu-card-header">
            <h2>
              <JoinRoomIcon className="menu-card-header-icon" />
              <span>Join room</span>
            </h2>
            <button className="close-button" onClick={onClose} type="button">
              &times;
            </button>
          </div>

          <form onSubmit={handleSubmit} className="menu-form">
            <label htmlFor="join-nickname">Nickname</label>
            <input
              id="join-nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="Opcional"
            />

            <label htmlFor="join-room-code">Room code</label>
            <input
              id="join-room-code"
              type="text"
              value={roomCode}
              onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
              placeholder="Enter room code"
              required
            />

            {error && <p className="form-error">{error}</p>}

            <button type="submit" className="submit-button" disabled={loading}>
              {loading ? "Joining..." : "Join"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default JoinRoomCard;
