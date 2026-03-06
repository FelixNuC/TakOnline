package com.takonline.takonline.game.service;

import com.takonline.takonline.game.dto.GameResponse;
import com.takonline.takonline.game.model.Game;
import com.takonline.takonline.game.model.GamePlayer;
import com.takonline.takonline.game.model.GameState;
import com.takonline.takonline.game.model.board.Piece;
import com.takonline.takonline.game.model.board.PieceColor;
import com.takonline.takonline.game.model.board.PieceType;
import com.takonline.takonline.game.repository.GameRepository;
import com.takonline.takonline.room.model.Room;
import com.takonline.takonline.room.model.RoomPlayer;
import com.takonline.takonline.room.model.RoomStatus;
import com.takonline.takonline.room.repository.RoomRepository;
import com.takonline.takonline.game.model.board.Stack;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

private final GameRepository gameRepository;
private final RoomRepository roomRepository;
private final SimpMessagingTemplate messagingTemplate;

public GameService(GameRepository gameRepository,
                   RoomRepository roomRepository,
                   SimpMessagingTemplate messagingTemplate) {
    this.gameRepository = gameRepository;
    this.roomRepository = roomRepository;
    this.messagingTemplate = messagingTemplate;
}

    public GameResponse createGameFromRoom(String roomCode) {
        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.getStatus() != RoomStatus.FULL) {
            throw new IllegalStateException("Room is not ready to start a game");
        }

        if (gameRepository.findByRoomCode(roomCode).isPresent()) {
            throw new IllegalStateException("Game already exists for this room");
        }

        List<GamePlayer> players = room.getPlayers().stream()
                .map(this::mapRoomPlayerToGamePlayer)
                .toList();

        Game game = new Game(roomCode, players, room.getBoardSize());
        gameRepository.save(game);

        return mapToResponse(game);
    }

    public GameResponse getGameByRoomCode(String roomCode) {
        Game game = gameRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return mapToResponse(game);
    }

    private GamePlayer mapRoomPlayerToGamePlayer(RoomPlayer roomPlayer) {
        return new GamePlayer(
                roomPlayer.getPlayerId(),
                roomPlayer.getPlayerName(),
                roomPlayer.getColor()
        );
    }

private GameResponse mapToResponse(Game game) {
    return new GameResponse(
            game.getGameId(),
            game.getRoomCode(),
            game.getPlayers(),
            game.getCurrentTurnColor(),
            game.getState(),
            game.getBoardSize(),
            game.getBoard(),
            game.getWinnerColor()
    );
}
public GameResponse placePiece(String gameId, String playerId, int row, int col, String pieceType) {
    Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    if (game.getState() == GameState.FINISHED) {
        throw new IllegalStateException("Game is already finished");
    }
    GamePlayer player = game.getPlayers().stream()
            .filter(p -> p.getPlayerId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not found in game"));

    if (!game.getCurrentTurnColor().equals(player.getColor())) {
        throw new IllegalStateException("It is not this player's turn");
    }

    PieceType parsedPieceType;
    try {
        parsedPieceType = PieceType.valueOf(pieceType.toUpperCase());
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid piece type");
    }

    PieceColor pieceColor = PieceColor.valueOf(player.getColor());
    Piece piece = new Piece(pieceColor, parsedPieceType);

game.getBoard().placePiece(row, col, piece);

if (hasRoadWin(game, player.getColor())) {
    game.setState(GameState.FINISHED);
    game.setWinnerColor(player.getColor());
} else {
    String nextTurn = game.getCurrentTurnColor().equals("WHITE") ? "BLACK" : "WHITE";
    game.setCurrentTurnColor(nextTurn);
}

gameRepository.save(game);

GameResponse response = mapToResponse(game);
messagingTemplate.convertAndSend("/topic/games/" + gameId, response);

return response;
}
private boolean hasRoadWin(Game game, String color) {
    int size = game.getBoard().getSize();
    Set<String> visited = new HashSet<>();

    if ("WHITE".equals(color)) {
        for (int col = 0; col < size; col++) {
            if (isRoadPieceForColor(game, 0, col, color)) {
                if (dfs(game, 0, col, color, visited)) {
                    return true;
                }
            }
        }
    } else if ("BLACK".equals(color)) {
        for (int row = 0; row < size; row++) {
            if (isRoadPieceForColor(game, row, 0, color)) {
                if (dfs(game, row, 0, color, visited)) {
                    return true;
                }
            }
        }
    }

    return false;
}

private boolean dfs(Game game, int row, int col, String color, Set<String> visited) {
    int size = game.getBoard().getSize();

    if (!isInsideBoard(game, row, col)) {
        return false;
    }

    if (!isRoadPieceForColor(game, row, col, color)) {
        return false;
    }

    String key = row + "," + col;
    if (visited.contains(key)) {
        return false;
    }

    visited.add(key);

    if ("WHITE".equals(color) && row == size - 1) {
        return true;
    }

    if ("BLACK".equals(color) && col == size - 1) {
        return true;
    }

    return dfs(game, row - 1, col, color, visited)
            || dfs(game, row + 1, col, color, visited)
            || dfs(game, row, col - 1, color, visited)
            || dfs(game, row, col + 1, color, visited);
}
private boolean isRoadPieceForColor(Game game, int row, int col, String color) {
    if (!isInsideBoard(game, row, col)) {
        return false;
    }

    Stack stack = game.getBoard().getStack(row, col);
    if (stack.isEmpty()) {
        return false;
    }

    if (stack.getTopPiece() == null) {
        return false;
    }

    return stack.getTopPiece().getColor().name().equals(color)
            && (stack.getTopPiece().getType() == PieceType.FLAT
            || stack.getTopPiece().getType() == PieceType.CAPSTONE);
}
private boolean isInsideBoard(Game game, int row, int col) {
    int size = game.getBoard().getSize();
    return row >= 0 && row < size && col >= 0 && col < size;
}
}