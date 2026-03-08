function CapstoneShape({ color }) {
  const isBlack = color === "BLACK";
  const fill = isBlack ? "#1f1f1f" : "#ffffff";
  const stroke = "#1f1f1f";

  return (
    <svg
      className="piece-svg capstone-svg"
      viewBox="0 0 56 56"
      aria-hidden="true"
    >
      <polygon
        points="28,8 43,16 43,40 28,48 13,40 13,16"
        fill={fill}
        stroke={stroke}
        strokeWidth="2"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function PieceShape({ color, type }) {
  const isStanding = type === "STANDING";
  const isCapstone = type === "CAPSTONE";

  if (color === "BLACK") {
    return (
      <div className="piece-shape black-piece">
        {isCapstone ? (
          <CapstoneShape color={color} />
        ) : (
          <div className={`piece-inner ${isStanding ? "wall-black" : "diamond"}`} />
        )}
      </div>
    );
  }

  return (
    <div className="piece-shape white-piece">
      {isCapstone ? (
        <CapstoneShape color={color} />
      ) : (
        <div className={`piece-inner ${isStanding ? "wall-white" : "square"}`} />
      )}
    </div>
  );
}

function PieceStack({
  stack = [],
  selected = false,
  dragging = false,
  onClick,
  draggable = false,
  onDragStart,
  onDragEnd,
}) {
  if (!stack.length) return null;

  const topPiece = stack[stack.length - 1];
  const topColor = topPiece.color || "WHITE";
  const topType = topPiece.type || "FLAT";

  return (
    <button
      type="button"
      className={`piece-stack ${selected ? "selected" : ""} ${stack.length > 1 ? "has-stack-ring" : ""} ${dragging ? "dragging" : ""}`}
      onClick={onClick}
      draggable={draggable}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
    >
      {stack.length > 1 && <span className="stack-ring" aria-hidden="true" />}
      <PieceShape color={topColor} type={topType} />
      {stack.length > 1 && <span className="stack-count">{stack.length}</span>}
    </button>
  );
}

export default PieceStack;
