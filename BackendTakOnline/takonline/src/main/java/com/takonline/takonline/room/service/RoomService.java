package com.takonline.takonline.room.service;

import com.takonline.takonline.room.dto.CreateRoomResponse;
import com.takonline.takonline.room.dto.RoomResponse;
import com.takonline.takonline.room.model.Room;
import com.takonline.takonline.room.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Random;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public CreateRoomResponse createRoom(String playerName) {
        validatePlayerName(playerName);

        String code = generateUniqueCode();
        Room room = new Room(code, playerName);
        roomRepository.save(room);

        return new CreateRoomResponse(
                room.getCode(),
                room.getHostPlayerName(),
                room.getStatus().name()
        );
    }

    public RoomResponse joinRoom(String code, String playerName) {
        return connectPlayer(code, playerName);
    }

    public RoomResponse connectPlayer(String code, String playerName) {
        validatePlayerName(playerName);

        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.hasPlayer(playerName)) {
            if (room.isFull()) {
                throw new IllegalStateException("Room is full");
            }
            room.addPlayer(playerName);
            roomRepository.save(room);
        }

        return mapToResponse(room);
    }

    public RoomResponse getRoom(String code) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return mapToResponse(room);
    }

    private RoomResponse mapToResponse(Room room) {
        return new RoomResponse(
                room.getCode(),
                room.getStatus().name(),
                room.getPlayers()
        );
    }

    private void validatePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name is required");
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (roomRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
}