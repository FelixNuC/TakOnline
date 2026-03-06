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
    public Room() {
    }

public Room(String code, String hostPlayerName, int boardSize) {
    this.id = UUID.randomUUID().toString();
    this.code = code;
    this.players = new ArrayList<>();
    this.players.add(new RoomPlayer(hostPlayerName, true, "WHITE"));
    this.status = RoomStatus.WAITING;
    this.createdAt = LocalDateTime.now();
    this.boardSize = boardSize;
}

    public void addPlayer(String playerName) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Room is already full");
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
}