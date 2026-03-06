package com.takonline.takonline.room.dto;

public class CreateRoomResponse {

    private String roomCode;
    private String hostPlayerName;
    private String status;

    public CreateRoomResponse() {
    }

    public CreateRoomResponse(String roomCode, String hostPlayerName, String status) {
        this.roomCode = roomCode;
        this.hostPlayerName = hostPlayerName;
        this.status = status;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getHostPlayerName() {
        return hostPlayerName;
    }

    public String getStatus() {
        return status;
    }
}