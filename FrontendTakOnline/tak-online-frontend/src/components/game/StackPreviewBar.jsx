function StackPreviewBar({ selectedCell }) {
  if (!selectedCell || !selectedCell.stack?.length) {
    return (
      <div className="stack-preview-bar empty">
        No stack selected
      </div>
    );
  }

  return (
    <div className="stack-preview-bar">
      <div className="stack-preview-header">
        Selected stack at ({selectedCell.row}, {selectedCell.col})
      </div>

      <div className="stack-preview-pieces">
        {selectedCell.stack.map((piece, index) => (
          <div key={index} className="stack-preview-piece">
            <span className={`mini-piece ${piece.color === "BLACK" ? "black" : "white"}`} />
            <span>
              {piece.color} {piece.type || "FLAT"}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default StackPreviewBar;