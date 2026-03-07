import HomeMenu from "../components/home/HomeMenu";
import "../styles/home.css";

function HomePage() {
  return (
    <div className="home-page">
      <div className="home-container">
        <h1 className="home-title">Tak Online</h1>
        <p className="home-subtitle">Create a room or join an existing match</p>
        <HomeMenu />
      </div>
    </div>
  );
}

export default HomePage;