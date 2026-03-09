import { MoonDecoIcon, SunDecoIcon } from "./ArtDecoIcons";

function ThemeToggle({ isDark, onToggle }) {
  const nextModeLabel = isDark ? "Activate light mode" : "Activate dark mode";

  return (
    <button
      className="theme-toggle-button"
      type="button"
      onClick={onToggle}
      aria-label={nextModeLabel}
      title={isDark ? "Modo claro" : "Modo noche"}
    >
      <span className={`theme-toggle-icon-wrap ${isDark ? "is-dark" : "is-light"}`} aria-hidden="true">
        <SunDecoIcon className="theme-icon theme-icon-sun" />
        <MoonDecoIcon className="theme-icon theme-icon-moon" />
      </span>
    </button>
  );
}

export default ThemeToggle;
