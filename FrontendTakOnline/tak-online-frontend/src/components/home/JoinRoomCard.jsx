import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { joinRoom } from "../../services/roomService";

function JoinRoomCard({ isActive, onOpen, onClose }) {
  const [nickname, setNickname] = useState("");
  const [roomCode, setRoomCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      setLoading(true);

      const room = await joinRoom(roomCode, nickname);
      console.log("Joined room:", room);
      const player = room?.players?.[room.players.length - 1] || null;

      navigate(`/room/${room.roomCode}`, {
        state: {
          room,
          playerName: nickname,
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
          Join room
        </button>
      ) : (
        <div className="menu-card-content">
          <div className="menu-card-header">
            <h2>Join room</h2>
            <button className="close-button" onClick={onClose} type="button">
              ×
            </button>
          </div>

          <form onSubmit={handleSubmit} className="menu-form">
            <label htmlFor="join-nickname">Nickname</label>
            <input
              id="join-nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="Enter your nickname"
              required
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
