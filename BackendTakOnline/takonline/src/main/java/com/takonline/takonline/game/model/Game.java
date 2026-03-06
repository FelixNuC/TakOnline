package com.takonline.takonline.game.model;

import java.time.LocalDateTime;
import java.util.List;
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
}