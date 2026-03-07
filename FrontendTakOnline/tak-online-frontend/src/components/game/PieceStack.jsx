function PieceShape({ color }) {
  if (color === "BLACK") {
    return (
      <div className="piece-shape black-piece">
        <div className="piece-inner diamond" />
      </div>
    );
  }

  return (
    <div className="piece-shape white-piece">
      <div className="piece-inner square" />
    </div>
  );
}

function PieceStack({ stack = [], selected = false, onClick }) {
  if (!stack.length) return null;

  const topPiece = stack[stack.length - 1];
  const topColor = topPiece.color || "WHITE";

  return (
    <button
      type="button"
      className={`piece-stack ${selected ? "selected" : ""}`}
      onClick={onClick}
    >
      <PieceShape color={topColor} />
      {stack.length > 1 && <span className="stack-count">{stack.length}</span>}
    </button>
  );
}

export default PieceStack;