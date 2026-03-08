package com.takonline.takonline.room.dto;

import java.util.List;

public class RoomResponse {

    private String roomCode;
    private String status;
    private int boardSize;
    private List<RoomPlayerResponse> players;
    private boolean vsAi;
    private String aiDifficulty;

    public RoomResponse() {
    }

    public RoomResponse(String roomCode,
                        String status,
                        int boardSize,
                        List<RoomPlayerResponse> players,
                        boolean vsAi,
                        String aiDifficulty) {
        this.roomCode = roomCode;
        this.status = status;
        this.boardSize = boardSize;
        this.players = players;
        this.vsAi = vsAi;
        this.aiDifficulty = aiDifficulty;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getStatus() {
        return status;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public List<RoomPlayerResponse> getPlayers() {
        return players;
    }

    public boolean isVsAi() {
        return vsAi;
    }

    public String getAiDifficulty() {
        return aiDifficulty;
    }
}
