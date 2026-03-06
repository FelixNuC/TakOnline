package com.takonline.takonline.room.dto;

public class RoomPlayerResponse {

    private String playerId;
    private String playerName;
    private boolean host;
    private String color;

    public RoomPlayerResponse() {
    }

    public RoomPlayerResponse(String playerId, String playerName, boolean host, String color) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.host = host;
        this.color = color;
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
}