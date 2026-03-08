package com.takonline.takonline.room.model;

import java.util.UUID;

public class RoomPlayer {

    private String playerId;
    private String playerName;
    private boolean host;
    private String color;
    private boolean bot;

    public RoomPlayer() {
    }

    public RoomPlayer(String playerName, boolean host, String color) {
        this.playerId = UUID.randomUUID().toString();
        this.playerName = playerName;
        this.host = host;
        this.color = color;
        this.bot = false;
    }

    public RoomPlayer(String playerName, boolean host, String color, boolean bot) {
        this.playerId = UUID.randomUUID().toString();
        this.playerName = playerName;
        this.host = host;
        this.color = color;
        this.bot = bot;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isHost() {
        return host;
    }

    public String getColor() {
        return color;
    }

    public boolean isBot() {
        return bot;
    }
}
