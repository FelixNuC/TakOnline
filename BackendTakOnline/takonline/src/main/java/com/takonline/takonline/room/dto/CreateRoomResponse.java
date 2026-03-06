package com.takonline.takonline.room.dto;

import com.takonline.takonline.room.model.Room;

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

    public CreateRoomResponse createRoom(String playerName) {
    validatePlayerName(playerName);

    String code = generateUniqueCode();
    Room room = new Room(code, playerName);
    roomRepository.save(room);

    RoomResponse roomResponse = mapToResponse(room);
    messagingTemplate.convertAndSend("/topic/rooms/" + code, roomResponse);

    return new CreateRoomResponse(
            room.getCode(),
            room.getHostPlayerName(),
            room.getStatus().name()
    );
}
}