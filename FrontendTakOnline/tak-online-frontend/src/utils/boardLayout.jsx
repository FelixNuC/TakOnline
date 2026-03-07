export function getNodePositions(boardSize, width = 600, height = 600, padding = 60) {
  const positions = [];
  const usableWidth = width - padding * 2;
  const usableHeight = height - padding * 2;

  const stepX = boardSize > 1 ? usableWidth / (boardSize - 1) : 0;
  const stepY = boardSize > 1 ? usableHeight / (boardSize - 1) : 0;

  for (let row = 0; row < boardSize; row++) {
    for (let col = 0; col < boardSize; col++) {
      positions.push({
        row,
        col,
        x: padding + col * stepX,
        y: padding + row * stepY,
      });
    }
  }

  return positions;
}

export function getNodeKey(row, col) {
  return `${row}-${col}`;
}