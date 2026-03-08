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
    private int rematchVotes;
    private List<String> rematchPlayerIds;

    public GameResponse() {
    }

    public GameResponse(String gameId,
                        String roomCode,
                        List<GamePlayer> players,
                        String currentTurnColor,
                        GameState state,
                        int boardSize,
                        Board board,
                        String winnerColor,
                        int rematchVotes,
                        List<String> rematchPlayerIds) {
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.players = players;
        this.currentTurnColor = currentTurnColor;
        this.state = state;
        this.boardSize = boardSize;
        this.board = board;
        this.winnerColor = winnerColor;
        this.rematchVotes = rematchVotes;
        this.rematchPlayerIds = rematchPlayerIds;
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

    public int getRematchVotes() {
        return rematchVotes;
    }

    public List<String> getRematchPlayerIds() {
        return rematchPlayerIds;
    }
}
