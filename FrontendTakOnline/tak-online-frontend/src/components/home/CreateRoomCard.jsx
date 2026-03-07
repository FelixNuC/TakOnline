import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createRoom } from "../../services/roomService";

function CreateRoomCard({ isActive, onOpen, onClose }) {
  const [nickname, setNickname] = useState("");
  const [boardSize, setBoardSize] = useState(5);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      setLoading(true);

      const room = await createRoom(nickname, Number(boardSize));
      console.log("Room created:", room);

     navigate(`/room/${room.roomCode}`, {
        state: {
          room,
          playerName: nickname,
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
          Create room
        </button>
      ) : (
        <div className="menu-card-content">
          <div className="menu-card-header">
            <h2>Create room</h2>
            <button className="close-button" onClick={onClose} type="button">
              ×
            </button>
          </div>

          <form onSubmit={handleSubmit} className="menu-form">
            <label htmlFor="create-nickname">Nickname</label>
            <input
              id="create-nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="Enter your nickname"
              required
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