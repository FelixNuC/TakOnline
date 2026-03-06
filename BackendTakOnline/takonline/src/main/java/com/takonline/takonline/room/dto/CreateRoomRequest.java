package com.takonline.takonline.room.dto;

public class CreateRoomRequest {

    private String playerName;
    private int boardSize;

    public CreateRoomRequest() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}