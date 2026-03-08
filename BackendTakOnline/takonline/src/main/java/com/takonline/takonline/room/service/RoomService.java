package com.takonline.takonline.room.service;

import com.takonline.takonline.room.dto.CreateRoomResponse;
import com.takonline.takonline.room.dto.RoomPlayerResponse;
import com.takonline.takonline.room.dto.RoomResponse;
import com.takonline.takonline.room.model.Room;
import com.takonline.takonline.room.repository.RoomRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    public RoomService(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

public RoomResponse createRoom(String playerName, int boardSize, String gameMode, String aiDifficulty) {
    validatePlayerName(playerName);
    validateBoardSize(boardSize);
    boolean vsAi = "AI".equalsIgnoreCase(gameMode);
    String normalizedDifficulty = null;
    if (vsAi) {
        normalizedDifficulty = normalizeAiDifficulty(aiDifficulty);
    }

    String code = generateUniqueCode();
    Room room = new Room(code, playerName, boardSize, vsAi, normalizedDifficulty);
    roomRepository.save(room);

    RoomResponse roomResponse = mapToResponse(room);
    messagingTemplate.convertAndSend("/topic/rooms/" + code, roomResponse);

    return roomResponse;
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

    public RoomResponse getRoom(String code) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return mapToResponse(room);
    }

private RoomResponse mapToResponse(Room room) {
    List<RoomPlayerResponse> players = room.getPlayers().stream()
            .map(player -> new RoomPlayerResponse(
                    player.getPlayerId(),
                    player.getPlayerName(),
                    player.isHost(),
                    player.getColor(),
                    player.isBot()
            ))
            .toList();

    return new RoomResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getBoardSize(),
            players,
            room.isVsAi(),
            room.getAiDifficulty()
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


private void validateBoardSize(int boardSize) {
    if (boardSize != 3 && boardSize != 4 && boardSize != 5
            && boardSize != 6 && boardSize != 7 && boardSize != 8) {
        throw new IllegalArgumentException("Board size must be 3, 4, 5, 6, 7 or 8");
    }
}

private String normalizeAiDifficulty(String aiDifficulty) {
    if (aiDifficulty == null || aiDifficulty.isBlank()) {
        return "NORMAL";
    }

    String normalized = aiDifficulty.trim().toUpperCase();
    if (!"EASY".equals(normalized) && !"NORMAL".equals(normalized) && !"HARD".equals(normalized)) {
        throw new IllegalArgumentException("AI difficulty must be EASY, NORMAL or HARD");
    }

    return normalized;
}
}
