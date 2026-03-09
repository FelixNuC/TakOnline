import { useEffect, useRef } from "react";
import HomeMenu from "../components/home/HomeMenu";
import "../styles/home.css";

function HomePage() {
  const pageRef = useRef(null);
  const ambientRef = useRef(null);
  const pieceRefs = useRef([]);

  useEffect(() => {
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (reduceMotion || !ambientRef.current) return;

    const container = ambientRef.current;
    const pieces = pieceRefs.current.filter(Boolean).map((element) => {
      const baseX = element.offsetLeft;
      const baseY = element.offsetTop;
      const width = element.offsetWidth;
      const height = element.offsetHeight;
      const baseRotation =
        parseFloat(getComputedStyle(element).getPropertyValue("--base-rot")) || 0;

      return {
        element,
        baseX,
        baseY,
        width,
        height,
        x: baseX,
        y: baseY,
        vx: (Math.random() * 0.018 + 0.01) * (Math.random() > 0.5 ? 1 : -1),
        vy: (Math.random() * 0.018 + 0.01) * (Math.random() > 0.5 ? 1 : -1),
        angle: baseRotation,
        spin: (Math.random() * 0.004 + 0.0015) * (Math.random() > 0.5 ? 1 : -1),
      };
    });

    if (!pieces.length) return;

    const bounds = {
      width: container.clientWidth,
      height: container.clientHeight,
    };

    function refreshBounds() {
      bounds.width = container.clientWidth;
      bounds.height = container.clientHeight;
      pieces.forEach((piece) => {
        piece.baseX = piece.element.offsetLeft;
        piece.baseY = piece.element.offsetTop;
        piece.width = piece.element.offsetWidth;
        piece.height = piece.element.offsetHeight;
        piece.x = Math.min(Math.max(piece.x, 0), Math.max(bounds.width - piece.width, 0));
        piece.y = Math.min(Math.max(piece.y, 0), Math.max(bounds.height - piece.height, 0));
      });
    }

    let rafId = 0;
    let previousTime = performance.now();

    function frame(now) {
      const dt = Math.min(now - previousTime, 34);
      previousTime = now;

      pieces.forEach((piece) => {
        piece.x += piece.vx * dt;
        piece.y += piece.vy * dt;

        const maxX = Math.max(bounds.width - piece.width, 0);
        const maxY = Math.max(bounds.height - piece.height, 0);

        if (piece.x <= 0) {
          piece.x = 0;
          piece.vx = Math.abs(piece.vx);
        } else if (piece.x >= maxX) {
          piece.x = maxX;
          piece.vx = -Math.abs(piece.vx);
        }

        if (piece.y <= 0) {
          piece.y = 0;
          piece.vy = Math.abs(piece.vy);
        } else if (piece.y >= maxY) {
          piece.y = maxY;
          piece.vy = -Math.abs(piece.vy);
        }

        piece.angle += piece.spin * dt;

        piece.element.style.setProperty("--piece-x", `${(piece.x - piece.baseX).toFixed(2)}px`);
        piece.element.style.setProperty("--piece-y", `${(piece.y - piece.baseY).toFixed(2)}px`);
        piece.element.style.setProperty("--piece-rot", `${piece.angle.toFixed(2)}deg`);
      });

      rafId = requestAnimationFrame(frame);
    }

    rafId = requestAnimationFrame(frame);
    window.addEventListener("resize", refreshBounds);

    return () => {
      cancelAnimationFrame(rafId);
      window.removeEventListener("resize", refreshBounds);
    };
  }, []);

  function handlePointerMove(event) {
    if (!pageRef.current) return;
    const rect = pageRef.current.getBoundingClientRect();
    const rawX = ((event.clientX - rect.left) / rect.width) * 100;
    const rawY = ((event.clientY - rect.top) / rect.height) * 100;
    const x = 50 + (rawX - 50) * 0.22;
    const y = 50 + (rawY - 50) * 0.22;
    pageRef.current.style.setProperty("--mouse-x", `${x.toFixed(2)}%`);
    pageRef.current.style.setProperty("--mouse-y", `${y.toFixed(2)}%`);
  }

  function handlePointerLeave() {
    if (!pageRef.current) return;
    pageRef.current.style.setProperty("--mouse-x", "50%");
    pageRef.current.style.setProperty("--mouse-y", "50%");
  }

  return (
    <div
      className="home-page"
      ref={pageRef}
      onMouseMove={handlePointerMove}
      onMouseLeave={handlePointerLeave}
    >
      <div className="home-ambient" aria-hidden="true" ref={ambientRef}>
        <span className="ambient-piece ambient-piece-square piece-1" ref={(el) => (pieceRefs.current[0] = el)} />
        <span className="ambient-piece ambient-piece-diamond piece-2" ref={(el) => (pieceRefs.current[1] = el)} />
        <span className="ambient-piece ambient-piece-square piece-3" ref={(el) => (pieceRefs.current[2] = el)} />
        <span className="ambient-piece ambient-piece-diamond piece-4" ref={(el) => (pieceRefs.current[3] = el)} />
        <span className="ambient-piece ambient-piece-square piece-5" ref={(el) => (pieceRefs.current[4] = el)} />
        <span className="ambient-piece ambient-piece-diamond piece-6" ref={(el) => (pieceRefs.current[5] = el)} />
      </div>
      <div className="home-container">
        <h1 className="home-title">Tak Online</h1>
        <p className="home-subtitle">Create a room or join an existing match</p>
        <HomeMenu />
      </div>
    </div>
  );
}

export default HomePage;
