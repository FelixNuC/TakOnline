package com.takonline.takonline.room.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {

    private String id;
    private String code;
    private String hostPlayerName;
    private List<String> players;
    private RoomStatus status;
    private LocalDateTime createdAt;

    public Room() {
    }

    public Room(String code, String hostPlayerName) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.hostPlayerName = hostPlayerName;
        this.players = new ArrayList<>();
        this.players.add(hostPlayerName);
        this.status = RoomStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    public void addPlayer(String playerName) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Room is already full");
        }
        players.add(playerName);
        if (players.size() == 2) {
            this.status = RoomStatus.FULL;
        }
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public boolean hasPlayer(String playerName) {
        return players.contains(playerName);
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getHostPlayerName() {
        return hostPlayerName;
    }

    public List<String> getPlayers() {
        return players;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}