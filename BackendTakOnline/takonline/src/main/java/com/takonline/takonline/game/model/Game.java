package com.takonline.takonline.game.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.takonline.takonline.game.model.board.Board;

public class Game {

private String gameId;
private String roomCode;
private List<GamePlayer> players;
private String currentTurnColor;
private GameState state;
private int boardSize;
private Board board;
private LocalDateTime createdAt;
private String winnerColor;
private Set<String> rematchPlayerIds;
    public Game() {
    }

    public Game(String roomCode, List<GamePlayer> players, int boardSize) {
        this.gameId = UUID.randomUUID().toString();
        this.roomCode = roomCode;
        this.players = players;
        this.currentTurnColor = "WHITE";
        this.state = GameState.IN_PROGRESS;
        this.boardSize = boardSize;
        this.createdAt = LocalDateTime.now();
        this.board = new Board(boardSize);
        this.winnerColor = null;
        this.rematchPlayerIds = new HashSet<>();
    }

    public String getGameId() {
        return gameId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public String getCurrentTurnColor() {
        return currentTurnColor;
    }

    public GameState getState() {
        return state;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public Board getBoard() {
    return board;
    }
    public void setCurrentTurnColor(String currentTurnColor) {
    this.currentTurnColor = currentTurnColor;
}
public String getWinnerColor() {
    return winnerColor;
}

public void setWinnerColor(String winnerColor) {
    this.winnerColor = winnerColor;
}

public void setState(GameState state) {
    this.state = state;
}

public int getRematchVotes() {
    return rematchPlayerIds.size();
}

public Set<String> getRematchPlayerIds() {
    return rematchPlayerIds;
}

public boolean addRematchVote(String playerId) {
    return rematchPlayerIds.add(playerId);
}

public void resetForRematch(List<GamePlayer> players) {
    this.players = players;
    this.currentTurnColor = "WHITE";
    this.state = GameState.IN_PROGRESS;
    this.board = new Board(boardSize);
    this.createdAt = LocalDateTime.now();
    this.winnerColor = null;
    this.rematchPlayerIds = new HashSet<>();
}
}
