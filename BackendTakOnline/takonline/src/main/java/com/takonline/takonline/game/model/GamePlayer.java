package com.takonline.takonline.game.model;

public class GamePlayer {

    private String playerId;
    private String playerName;
    private String color;
    private int remainingFlats;
    private int remainingCapstones;
    public GamePlayer() {
    }

    public GamePlayer(String playerId, String playerName, String color, int flats, int capstones) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.color = color;
        this.remainingFlats = flats;
        this.remainingCapstones = capstones;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getColor() {
        return color;
    }

    public int getRemainingFlats() {
    return remainingFlats;
}

public int getRemainingCapstones() {
    return remainingCapstones;
}

public void useFlat() {
    if (remainingFlats <= 0) {
        throw new IllegalStateException("No flat stones remaining");
    }
    remainingFlats--;
}

public void useCapstone() {
    if (remainingCapstones <= 0) {
        throw new IllegalStateException("No capstones remaining");
    }
    remainingCapstones--;
}
}