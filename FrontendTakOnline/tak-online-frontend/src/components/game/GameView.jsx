import { useEffect, useMemo, useState } from "react";
import { moveStack, placePiece, requestRematch } from "../../services/gameService";
import { getNodeKey } from "../../utils/boardLayout";
import GameBoard from "./GameBoard";
import StackPreviewBar from "./StackPreviewBar";

function normalizeBoard(game) {
  const boardSize = game?.boardSize || 5;
  const boardCells = game?.board?.cells || [];
  const cells = [];

  for (let row = 0; row < boardSize; row++) {
    for (let col = 0; col < boardSize; col++) {
      const backendCell = boardCells?.[row]?.[col];
      const stack = Array.isArray(backendCell?.pieces) ? backendCell.pieces : [];
      cells.push({ row, col, stack });
    }
  }

  return cells;
}

function getMoveVector(fromCell, toCell) {
  const rowDelta = toCell.row - fromCell.row;
  const colDelta = toCell.col - fromCell.col;

  if (rowDelta === 0 && colDelta === 0) {
    return null;
  }

  if (rowDelta !== 0 && colDelta !== 0) {
    return null;
  }

  if (rowDelta < 0) return { direction: "UP", distance: Math.abs(rowDelta) };
  if (rowDelta > 0) return { direction: "DOWN", distance: rowDelta };
  if (colDelta < 0) return { direction: "LEFT", distance: Math.abs(colDelta) };
  return { direction: "RIGHT", distance: colDelta };
}

function buildAutoDrops(pickupCount, distance) {
  const drops = new Array(distance).fill(1);
  drops[distance - 1] += pickupCount - distance;
  return drops;
}

function GameView({ game, currentPlayer, onGameUpdate }) {
  const [selectedCell, setSelectedCell] = useState(null);
  const [actionMode, setActionMode] = useState("PLACE");
  const [selectedPieceType, setSelectedPieceType] = useState("FLAT");
  const [pickupCount, setPickupCount] = useState(1);
  const [moveOrigin, setMoveOrigin] = useState(null);
  const [dragSourceKey, setDragSourceKey] = useState(null);
  const [dropTargetKey, setDropTargetKey] = useState(null);
  const [actionError, setActionError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [rematchSubmitting, setRematchSubmitting] = useState(false);

  const cells = useMemo(() => normalizeBoard(game), [game]);
  const cellMap = useMemo(() => {
    const map = new Map();
    cells.forEach((cell) => {
      map.set(getNodeKey(cell.row, cell.col), cell);
    });
    return map;
  }, [cells]);

  const me = useMemo(() => {
    if (!game?.players?.length) return null;
    if (currentPlayer?.playerId) {
      const byId = game.players.find((player) => player.playerId === currentPlayer.playerId);
      if (byId) return byId;
    }
    if (currentPlayer?.playerName) {
      return game.players.find((player) => player.playerName === currentPlayer.playerName) || null;
    }
    return null;
  }, [game, currentPlayer]);

  const isMyTurn = !!me && game?.currentTurnColor === me.color;
  const isFinished = game?.state === "FINISHED";
  const rematchVotes = game?.rematchVotes ?? 0;
  const rematchTarget = game?.players?.length || 2;
  const hasRequestedRematch = !!(
    me?.playerId &&
    Array.isArray(game?.rematchPlayerIds) &&
    game.rematchPlayerIds.includes(me.playerId)
  );
  const winnerResultLabel = useMemo(() => {
    if (!isFinished) return "";
    if (game?.winnerColor === "DRAW") return "Draw";
    if (!me?.color || !game?.winnerColor) return "Game finished";
    return game.winnerColor === me.color ? "You win" : "You lose";
  }, [game?.winnerColor, isFinished, me?.color]);

  const availablePieceTypes = useMemo(() => {
    const types = [];

    if ((me?.remainingFlats || 0) > 0) {
      types.push("FLAT");
      types.push("STANDING");
    }

    if ((me?.remainingCapstones || 0) > 0) {
      types.push("CAPSTONE");
    }

    return types;
  }, [me]);

  useEffect(() => {
    if (!availablePieceTypes.length) return;
    if (!availablePieceTypes.includes(selectedPieceType)) {
      setSelectedPieceType(availablePieceTypes[0]);
    }
  }, [availablePieceTypes, selectedPieceType]);

  useEffect(() => {
    if (!selectedCell) return;
    const key = getNodeKey(selectedCell.row, selectedCell.col);
    const nextSelected = cellMap.get(key) || null;
    setSelectedCell(nextSelected);
  }, [cellMap, selectedCell]);

  useEffect(() => {
    if (!moveOrigin) return;
    const key = getNodeKey(moveOrigin.row, moveOrigin.col);
    const nextOrigin = cellMap.get(key) || null;
    if (!nextOrigin || !nextOrigin.stack?.length) {
      setMoveOrigin(null);
      return;
    }
    setMoveOrigin(nextOrigin);
  }, [cellMap, moveOrigin]);

  const parsedPickupCount = Math.max(1, Number(pickupCount) || 1);
  const reachableDropKeys = useMemo(() => {
    if (!moveOrigin || actionMode !== "MOVE" || !isMyTurn || isFinished) {
      return new Set();
    }

    const maxDistance = Math.min(parsedPickupCount, game?.boardSize || parsedPickupCount);
    const keys = new Set();

    for (let step = 1; step <= maxDistance; step++) {
      const upKey = getNodeKey(moveOrigin.row - step, moveOrigin.col);
      const downKey = getNodeKey(moveOrigin.row + step, moveOrigin.col);
      const leftKey = getNodeKey(moveOrigin.row, moveOrigin.col - step);
      const rightKey = getNodeKey(moveOrigin.row, moveOrigin.col + step);

      if (cellMap.has(upKey)) keys.add(upKey);
      if (cellMap.has(downKey)) keys.add(downKey);
      if (cellMap.has(leftKey)) keys.add(leftKey);
      if (cellMap.has(rightKey)) keys.add(rightKey);
    }

    return keys;
  }, [moveOrigin, actionMode, isMyTurn, parsedPickupCount, game, cellMap, isFinished]);

  function isCellControlledByMe(cell) {
    if (!cell.stack?.length || !me) return false;
    const topPiece = cell.stack[cell.stack.length - 1];
    return topPiece?.color === me.color;
  }

  function canDragCell(cell) {
    return actionMode === "MOVE" && isMyTurn && !isFinished && !submitting && isCellControlledByMe(cell);
  }

  function canPlaceOnCell(cell) {
    return actionMode === "PLACE" && isMyTurn && !isFinished && !cell.stack?.length;
  }

  async function handleCellSelection(cell) {
    setSelectedCell(cell);
    setActionError("");

    if (isFinished) {
      setActionError("This game is finished.");
      return;
    }

    if (!me) {
      setActionError("No se pudo identificar tu jugador en esta partida.");
      return;
    }

    if (!isMyTurn) {
      setActionError("No es tu turno.");
      return;
    }

    if (actionMode === "PLACE") {
      if (submitting) return;
      if (!availablePieceTypes.includes(selectedPieceType)) {
        setActionError("No tienes piezas disponibles de ese tipo.");
        return;
      }
      if (cell.stack?.length) {
        setActionError("Solo puedes colocar en una celda vacia.");
        return;
      }

      try {
        setSubmitting(true);
        const updatedGame = await placePiece(game.gameId, {
          playerId: me.playerId,
          row: cell.row,
          col: cell.col,
          pieceType: selectedPieceType,
        });
        onGameUpdate?.(updatedGame);
      } catch (err) {
        setActionError(err.message || "No se pudo colocar la pieza.");
      } finally {
        setSubmitting(false);
      }
      return;
    }

    if (!isCellControlledByMe(cell)) {
      setActionError("Selecciona un stack propio para mover.");
      return;
    }

    setMoveOrigin(cell);
  }

  function handleStartDrag(cell) {
    setActionError("");

    if (isFinished) {
      setActionError("This game is finished.");
      return false;
    }

    if (!canDragCell(cell)) {
      if (!isMyTurn) {
        setActionError("No es tu turno.");
      } else if (!isCellControlledByMe(cell)) {
        setActionError("Solo puedes arrastrar stacks que controlas.");
      }
      return false;
    }

    const originKey = getNodeKey(cell.row, cell.col);
    const maxPickup = Math.min(cell.stack.length, game?.boardSize || cell.stack.length);
    const desiredPickup = Math.max(1, Number(pickupCount) || 1);
    const effectivePickup = Math.min(desiredPickup, maxPickup);

    setPickupCount(effectivePickup);
    setMoveOrigin(cell);
    setSelectedCell(cell);
    setDragSourceKey(originKey);
    setDropTargetKey(null);
    return true;
  }

  function handleDragEnterCell(cell) {
    if (!dragSourceKey) return;
    setDropTargetKey(getNodeKey(cell.row, cell.col));
  }

  async function handleDropCell(targetCell) {
    if (!moveOrigin || !dragSourceKey) return;

    setDropTargetKey(null);
    setDragSourceKey(null);

    const parsedPickup = Number(pickupCount);
    if (!Number.isInteger(parsedPickup) || parsedPickup <= 0) {
      setActionError("pickup debe ser un entero mayor que 0.");
      return;
    }

    const moveVector = getMoveVector(moveOrigin, targetCell);
    if (!moveVector) {
      setActionError("Arrastra en linea recta (sin diagonales).");
      return;
    }

    if (parsedPickup < moveVector.distance) {
      setActionError("pickup debe ser mayor o igual a la distancia del arrastre.");
      return;
    }

    const drops = buildAutoDrops(parsedPickup, moveVector.distance);

    try {
      setSubmitting(true);
      const updatedGame = await moveStack(game.gameId, {
        playerId: me.playerId,
        fromRow: moveOrigin.row,
        fromCol: moveOrigin.col,
        direction: moveVector.direction,
        pickupCount: parsedPickup,
        drops,
      });
      setMoveOrigin(null);
      setSelectedCell(null);
      onGameUpdate?.(updatedGame);
    } catch (err) {
      setActionError(err.message || "No se pudo mover el stack.");
    } finally {
      setSubmitting(false);
    }
  }

  function handleEndDrag() {
    setDragSourceKey(null);
    setDropTargetKey(null);
  }

  async function handleRequestRematch() {
    if (!isFinished || !me?.playerId || hasRequestedRematch || rematchSubmitting) return;

    try {
      setActionError("");
      setRematchSubmitting(true);
      const updatedGame = await requestRematch(game.gameId, {
        playerId: me.playerId,
      });
      onGameUpdate?.(updatedGame);
    } catch (err) {
      setActionError(err.message || "No se pudo solicitar rematch.");
    } finally {
      setRematchSubmitting(false);
    }
  }

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

      <div className="game-main-layout">
        <aside className="game-side-panel">
          <div className="side-panel-block">
            <div className="side-title">Tus piezas</div>
            <div className="piece-bank-row">
              <span className="piece-bank-label">Flat / Stand</span>
              <span className="piece-bank-value">{me?.remainingFlats ?? "-"}</span>
            </div>
            <div className="piece-bank-row">
              <span className="piece-bank-label">Stone</span>
              <span className="piece-bank-value">{me?.remainingCapstones ?? "-"}</span>
            </div>
          </div>

          <div className="side-panel-block">
            <div className="side-title">Accion</div>
            <div className="action-toggle">
              <button
                type="button"
                className={`mode-button ${actionMode === "PLACE" ? "active" : ""}`}
                onClick={() => {
                  setActionMode("PLACE");
                  setActionError("");
                }}
              >
                Colocar
              </button>
              <button
                type="button"
                className={`mode-button ${actionMode === "MOVE" ? "active" : ""}`}
                onClick={() => {
                  setActionMode("MOVE");
                  setActionError("");
                }}
              >
                Mover
              </button>
            </div>
          </div>

          {actionMode === "PLACE" && (
            <div className="side-panel-block">
              <div className="side-title">Tipo de pieza</div>
              <div className="piece-type-buttons">
                <button
                  type="button"
                  disabled={!availablePieceTypes.includes("FLAT")}
                  className={`piece-type-button ${selectedPieceType === "FLAT" ? "active" : ""}`}
                  onClick={() => setSelectedPieceType("FLAT")}
                >
                  Flat
                </button>
                <button
                  type="button"
                  disabled={!availablePieceTypes.includes("STANDING")}
                  className={`piece-type-button ${selectedPieceType === "STANDING" ? "active" : ""}`}
                  onClick={() => setSelectedPieceType("STANDING")}
                >
                  Stand
                </button>
                <button
                  type="button"
                  disabled={!availablePieceTypes.includes("CAPSTONE")}
                  className={`piece-type-button ${selectedPieceType === "CAPSTONE" ? "active" : ""}`}
                  onClick={() => setSelectedPieceType("CAPSTONE")}
                >
                  Stone
                </button>
              </div>
              <p className="side-help-text">Click en una celda vacia para colocar la pieza.</p>
            </div>
          )}

          {actionMode === "MOVE" && (
            <div className="side-panel-block">
              <div className="side-title">Mover stack</div>
              <p className="side-help-text">
                Arrastra un stack propio y sueltalo en una casilla del mismo eje (fila o columna).
              </p>
              {moveOrigin && (
                <p className="side-help-text">
                  Alcance actual: hasta {Math.min(parsedPickupCount, game?.boardSize || parsedPickupCount)} nodos.
                </p>
              )}
              <label className="side-label" htmlFor="move-pickup">
                Pickup
              </label>
              <input
                id="move-pickup"
                className="side-input"
                type="number"
                min="1"
                value={pickupCount}
                onChange={(event) => setPickupCount(event.target.value)}
              />
            </div>
          )}

          {actionError && <p className="form-error">{actionError}</p>}
          {!isMyTurn && <p className="side-help-text">Espera tu turno para jugar.</p>}
        </aside>

        <div className="game-board-column">
          <StackPreviewBar selectedCell={selectedCell} />
          <div className="game-board-stage">
            <GameBoard
              boardSize={game?.boardSize || 5}
              cells={cells}
              dragSourceKey={dragSourceKey}
              dropTargetKey={dropTargetKey}
              reachableDropKeys={reachableDropKeys}
              canDragCell={canDragCell}
              canPlaceOnCell={canPlaceOnCell}
              onSelectCell={handleCellSelection}
              onStartDrag={handleStartDrag}
              onDragEnterCell={handleDragEnterCell}
              onDropCell={handleDropCell}
              onEndDrag={handleEndDrag}
            />
            {isFinished && (
              <div className="game-finish-overlay">
                <div className="game-finish-card">
                  <p className="game-finish-title">{winnerResultLabel}</p>
                  <p className="game-finish-subtitle">
                    Rematch {rematchVotes}/{rematchTarget}
                  </p>
                  <button
                    type="button"
                    className="rematch-button"
                    onClick={handleRequestRematch}
                    disabled={hasRequestedRematch || rematchSubmitting}
                  >
                    {hasRequestedRematch ? "Rematch requested" : rematchSubmitting ? "Requesting..." : "Request rematch"}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameView;
