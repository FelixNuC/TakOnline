package com.takonline.takonline.game.dto;

import com.takonline.takonline.game.model.GamePlayer;
import com.takonline.takonline.game.model.GameState;
import com.takonline.takonline.game.model.board.Board;

import java.util.List;

public class GameResponse {

    private String gameId;
    private String roomCode;
    private List<GamePlayer> players;
    private String currentTurnColor;
    private GameState state;
    private int boardSize;
    private Board board;
    private String winnerColor;

    public GameResponse() {
    }

    public GameResponse(String gameId,
                        String roomCode,
                        List<GamePlayer> players,
                        String currentTurnColor,
                        GameState state,
                        int boardSize,
                        Board board,
                        String winnerColor) {
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.players = players;
        this.currentTurnColor = currentTurnColor;
        this.state = state;
        this.boardSize = boardSize;
        this.board = board;
        this.winnerColor = winnerColor;
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

    public Board getBoard() {
        return board;
    }

    public String getWinnerColor() {
        return winnerColor;
    }
}