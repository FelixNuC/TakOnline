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

      <div className="stack-preview-labels" aria-hidden="true">
        <span>Base</span>
        <span>Top</span>
      </div>

      <div className="stack-preview-pieces">
        {selectedCell.stack.map((piece, index) => (
          <div
            key={index}
            className={`stack-preview-piece ${index === 0 ? "is-bottom" : ""} ${index === selectedCell.stack.length - 1 ? "is-top" : ""}`}
          >
            <span className={`mini-piece ${piece.color === "BLACK" ? "black" : "white"}`} />
          </div>
        ))}
      </div>
    </div>
  );
}

export default StackPreviewBar;
