package com.takonline.takonline.room.dto;

import java.util.List;

import com.takonline.takonline.room.model.Room;

public class RoomResponse {

    private String roomCode;
    private String status;
    private List<String> players;

    public RoomResponse() {
    }

    public RoomResponse(String roomCode, String status, List<String> players) {
        this.roomCode = roomCode;
        this.status = status;
        this.players = players;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getPlayers() {
        return players;
    }

    public RoomResponse joinRoom(String code, String playerName) {
    validatePlayerName(playerName);

    Room room = roomRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));

    if (room.isFull()) {
        throw new IllegalStateException("Room is full");
    }

    room.addPlayer(playerName);
    roomRepository.save(room);

    RoomResponse response = mapToResponse(room);

    messagingTemplate.convertAndSend("/topic/rooms/" + code, response);

    return response;
}
}