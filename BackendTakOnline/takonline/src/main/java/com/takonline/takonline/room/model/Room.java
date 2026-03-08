package com.takonline.takonline.room.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {

    private String id;
    private String code;
    private List<RoomPlayer> players;
    private RoomStatus status;
    private LocalDateTime createdAt;
    private int boardSize;
    private boolean vsAi;
    private String aiDifficulty;
    public Room() {
    }

public Room(String code, String hostPlayerName, int boardSize) {
    this(code, hostPlayerName, boardSize, false, null);
}

public Room(String code, String hostPlayerName, int boardSize, boolean vsAi, String aiDifficulty) {
    this.id = UUID.randomUUID().toString();
    this.code = code;
    this.players = new ArrayList<>();
    this.players.add(new RoomPlayer(hostPlayerName, true, "WHITE"));
    this.vsAi = vsAi;
    this.aiDifficulty = aiDifficulty;
    if (vsAi) {
        this.players.add(new RoomPlayer("TakBot (" + aiDifficulty + ")", false, "BLACK", true));
        this.status = RoomStatus.FULL;
    } else {
        this.status = RoomStatus.WAITING;
    }
    this.createdAt = LocalDateTime.now();
    this.boardSize = boardSize;
}

    public void addPlayer(String playerName) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Room is already full");
        }
        boolean duplicateName = players.stream()
                .anyMatch(player -> player.getPlayerName().equalsIgnoreCase(playerName));
        if (duplicateName) {
            throw new IllegalStateException("Player name already exists in this room");
        }

        players.add(new RoomPlayer(playerName, false, "BLACK"));

        if (players.size() == 2) {
            this.status = RoomStatus.FULL;
        }
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public List<RoomPlayer> getPlayers() {
        return players;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public int getBoardSize() {
    return boardSize;
}

    public boolean isVsAi() {
        return vsAi;
    }

    public String getAiDifficulty() {
        return aiDifficulty;
    }
}
