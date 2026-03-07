import { useState } from "react";
import CreateRoomCard from "./CreateRoomCard";
import JoinRoomCard from "./JoinRoomCard";

function HomeMenu() {
  const [activeCard, setActiveCard] = useState(null);

  return (
    <div className="home-menu">
      <CreateRoomCard
        isActive={activeCard === "create"}
        onOpen={() => setActiveCard("create")}
        onClose={() => setActiveCard(null)}
      />

      <JoinRoomCard
        isActive={activeCard === "join"}
        onOpen={() => setActiveCard("join")}
        onClose={() => setActiveCard(null)}
      />
    </div>
  );
}

export default HomeMenu;