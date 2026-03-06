package com.takonline.takonline.websocket.dto;

public class RoomConnectionRequest {

    private String playerName;

    public RoomConnectionRequest() {
    }

    public RoomConnectionRequest(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
