package com.takonline.takonline.websocket.dto;

public class RoomMessage {

    private String playerName;
    private String content;

    public RoomMessage() {
    }

    public RoomMessage(String playerName, String content) {
        this.playerName = playerName;
        this.content = content;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
