import { useMemo, useState } from "react";
import { getNodeKey } from "../../utils/boardLayout";
import GameBoard from "./GameBoard";
import StackPreviewBar from "./StackPreviewBar";

function normalizeBoard(game) {
  // Fallback temporal para poder pintar aunque el board real aún no esté adaptado
  // Cuando me pegues el JSON real de GET /api/games/room/{roomCode}, ajustamos esto.
  const boardSize = game?.boardSize || 5;

  const cells = [];

  for (let row = 0; row < boardSize; row++) {
    for (let col = 0; col < boardSize; col++) {
      cells.push({ row, col, stack: [] });
    }
  }

  return cells;
}

function GameView({ game }) {
  const [selectedCell, setSelectedCell] = useState(null);

  const cells = useMemo(() => normalizeBoard(game), [game]);
  const selectedCellKey = selectedCell ? getNodeKey(selectedCell.row, selectedCell.col) : null;

  return (
    <div className="game-view">
      <div className="game-hud">
        <div className="game-hud-card">
          <span className="hud-label">Room</span>
          <span className="hud-value">{game?.roomCode}</span>
        </div>

        <div className="game-hud-card">
          <span className="hud-label">Turn</span>
          <span className="hud-value">{game?.currentTurnColor}</span>
        </div>

        <div className="game-hud-card">
          <span className="hud-label">State</span>
          <span className="hud-value">{game?.state}</span>
        </div>
      </div>

      <StackPreviewBar selectedCell={selectedCell} />

      <GameBoard
        boardSize={game?.boardSize || 5}
        cells={cells}
        selectedCellKey={selectedCellKey}
        onSelectCell={setSelectedCell}
      />
    </div>
  );
}

export default GameView;