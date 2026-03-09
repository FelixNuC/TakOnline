import { useEffect, useMemo, useRef, useState } from "react";

function CapstoneShape({ color }) {
  const isBlack = color === "BLACK";
  const fill = isBlack ? "#1f1f1f" : "#ffffff";
  const stroke = isBlack ? "#ffffff":"#1f1f1f"  ;

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
  const [justUpdated, setJustUpdated] = useState(false);
  const [sparkleBurstId, setSparkleBurstId] = useState(0);
  const previousSignatureRef = useRef(null);
  const previousLengthRef = useRef(0);
  const updateAnimationTimeoutRef = useRef(null);

  const hasPieces = stack.length > 0;
  const topPiece = hasPieces ? stack[stack.length - 1] : null;
  const topColor = topPiece?.color || "WHITE";
  const topType = topPiece?.type || "FLAT";
  const stackSignature = useMemo(
    () => stack.map((piece) => `${piece.color || "WHITE"}:${piece.type || "FLAT"}`).join("|"),
    [stack]
  );

  useEffect(() => {
    if (!previousSignatureRef.current) {
      previousSignatureRef.current = stackSignature;
      previousLengthRef.current = stack.length;
      return;
    }

    if (previousSignatureRef.current !== stackSignature) {
      setJustUpdated(true);
      if (stack.length > previousLengthRef.current && stack.length > 1) {
        setSparkleBurstId((current) => current + 1);
      }

      if (updateAnimationTimeoutRef.current) {
        clearTimeout(updateAnimationTimeoutRef.current);
      }

      updateAnimationTimeoutRef.current = setTimeout(() => {
        setJustUpdated(false);
        updateAnimationTimeoutRef.current = null;
      }, 220);
    }

    previousSignatureRef.current = stackSignature;
    previousLengthRef.current = stack.length;
  }, [stack.length, stackSignature]);

  useEffect(() => {
    return () => {
      if (updateAnimationTimeoutRef.current) {
        clearTimeout(updateAnimationTimeoutRef.current);
      }
    };
  }, []);

  if (!hasPieces) return null;

  return (
    <button
      type="button"
      className={`piece-stack ${selected ? "selected" : ""} ${stack.length > 1 ? "has-stack-ring" : ""} ${dragging ? "dragging" : ""} ${justUpdated ? "just-updated" : ""}`}
      onClick={onClick}
      draggable={draggable}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
    >
      {stack.length > 1 && <span className="stack-ring" aria-hidden="true" />}
      <PieceShape color={topColor} type={topType} />
      {stack.length > 1 && <span className="stack-count">{stack.length}</span>}
      {sparkleBurstId > 0 && (
        <span className="stack-sparkles" aria-hidden="true" key={sparkleBurstId}>
          <span className="stack-sparkle s1" />
          <span className="stack-sparkle s2" />
          <span className="stack-sparkle s3" />
          <span className="stack-sparkle s4" />
          <span className="stack-sparkle s5" />
        </span>
      )}
    </button>
  );
}

export default PieceStack;
