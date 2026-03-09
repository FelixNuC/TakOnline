import { useEffect, useMemo, useState } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import RoomPage from "./pages/RoomPage";
import ThemeToggle from "./components/common/ThemeToggle";
import GlobalNav from "./components/common/GlobalNav";

function App() {
  const getInitialTheme = useMemo(
    () => () => {
      const savedTheme = localStorage.getItem("tak.theme");
      if (savedTheme === "light" || savedTheme === "dark") {
        return savedTheme;
      }

      return window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light";
    },
    []
  );

  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("tak.theme", theme);
  }, [theme]);

  const isDarkTheme = theme === "dark";

  const handleThemeToggle = () => {
    setTheme((currentTheme) => (currentTheme === "dark" ? "light" : "dark"));
  };

  return (
    <BrowserRouter>
      <GlobalNav />
      <ThemeToggle isDark={isDarkTheme} onToggle={handleThemeToggle} />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/room/:roomCode" element={<RoomPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
