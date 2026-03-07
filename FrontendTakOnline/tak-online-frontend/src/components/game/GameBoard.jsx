import { useMemo } from "react";
import { getNodeKey, getNodePositions } from "../../utils/boardLayout";
import PieceStack from "./PieceStack";

function GameBoard({
  boardSize,
  cells,
  selectedCellKey,
  onSelectCell,
}) {
  const width = 600;
  const height = 600;

  const positions = useMemo(
    () => getNodePositions(boardSize, width, height, 70),
    [boardSize]
  );

  const cellMap = useMemo(() => {
    const map = new Map();
    cells.forEach((cell) => {
      map.set(getNodeKey(cell.row, cell.col), cell);
    });
    return map;
  }, [cells]);

  return (
    <div className="game-board-wrapper">
      <svg
        className="board-svg"
        viewBox={`0 0 ${width} ${height}`}
        role="img"
        aria-label="Tak board"
      >
        {positions.map((node) => {
          const right = positions.find((p) => p.row === node.row && p.col === node.col + 1);
          const down = positions.find((p) => p.row === node.row + 1 && p.col === node.col);

          return (
            <g key={`lines-${node.row}-${node.col}`}>
              {right && (
                <line
                  x1={node.x}
                  y1={node.y}
                  x2={right.x}
                  y2={right.y}
                  className="board-line"
                />
              )}
              {down && (
                <line
                  x1={node.x}
                  y1={node.y}
                  x2={down.x}
                  y2={down.y}
                  className="board-line"
                />
              )}
            </g>
          );
        })}

        {positions.map((node) => (
          <circle
            key={`node-${node.row}-${node.col}`}
            cx={node.x}
            cy={node.y}
            r="8"
            className="board-node"
          />
        ))}
      </svg>

      <div className="board-overlay">
        {positions.map((node) => {
          const key = getNodeKey(node.row, node.col);
          const cell = cellMap.get(key) || { row: node.row, col: node.col, stack: [] };
          const selected = selectedCellKey === key;

          return (
            <div
              key={key}
              className="board-overlay-node"
              style={{
                left: `${(node.x / width) * 100}%`,
                top: `${(node.y / height) * 100}%`,
              }}
            >
              <button
                type="button"
                className={`node-hitbox ${selected ? "selected" : ""}`}
                onClick={() => onSelectCell(cell)}
              >
                <span className="sr-only">
                  Cell {node.row}, {node.col}
                </span>
              </button>

              <PieceStack
                stack={cell.stack}
                selected={selected}
                onClick={() => onSelectCell(cell)}
              />
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default GameBoard;