package com.takonline.takonline.game.model;

public class GamePlayer {

    private String playerId;
    private String playerName;
    private String color;

    public GamePlayer() {
    }

    public GamePlayer(String playerId, String playerName, String color) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.color = color;
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
}