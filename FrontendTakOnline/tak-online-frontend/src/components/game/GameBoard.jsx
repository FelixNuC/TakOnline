import { useMemo } from "react";
import { getNodeKey, getNodePositions } from "../../utils/boardLayout";
import PieceStack from "./PieceStack";

function GameBoard({
  boardSize,
  cells,
  dragSourceKey,
  dropTargetKey,
  reachableDropKeys,
  canDragCell,
  canPlaceOnCell,
  onSelectCell,
  onStartDrag,
  onDragEnterCell,
  onDropCell,
  onEndDrag,
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
          const draggingFrom = dragSourceKey === key;
          const isDropTarget = dropTargetKey === key;
          const isReachable = !!reachableDropKeys?.has(key);
          const draggable = !!canDragCell?.(cell);
          const placementHover = !!canPlaceOnCell?.(cell);

          return (
            <div
              key={key}
              className="board-overlay-node"
              style={{
                left: `${(node.x / width) * 100}%`,
                top: `${(node.y / height) * 100}%`,
              }}
              onDragOver={(event) => {
                if (!dragSourceKey) return;
                event.preventDefault();
              }}
              onDragEnter={() => onDragEnterCell?.(cell)}
              onDrop={(event) => {
                if (!dragSourceKey) return;
                event.preventDefault();
                onDropCell?.(cell);
              }}
            >
              <button
                type="button"
                className={`node-hitbox ${placementHover ? "placement-hover" : ""} ${isDropTarget ? "drop-target" : ""} ${isReachable ? "reachable-target" : ""}`}
                onClick={() => onSelectCell(cell)}
              >
                <span className="node-hitbox-glow" aria-hidden="true" />
                <span className="sr-only">
                  Cell {node.row}, {node.col}
                </span>
              </button>

              <PieceStack
                stack={cell.stack}
                selected={draggingFrom}
                dragging={draggingFrom}
                onClick={() => onSelectCell(cell)}
                draggable={draggable}
                onDragStart={(event) => {
                  if (!onStartDrag) return;
                  const allowed = onStartDrag(cell);
                  if (allowed === false) {
                    event.preventDefault();
                    return;
                  }
                  event.dataTransfer.effectAllowed = "move";
                  event.dataTransfer.setData("text/plain", key);
                }}
                onDragEnd={() => onEndDrag?.()}
              />
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default GameBoard;
