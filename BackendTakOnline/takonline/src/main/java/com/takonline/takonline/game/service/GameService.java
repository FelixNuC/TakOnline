package com.takonline.takonline.game.service;

import com.takonline.takonline.game.dto.GameResponse;
import com.takonline.takonline.game.model.Game;
import com.takonline.takonline.game.model.GamePlayer;
import com.takonline.takonline.game.model.board.Piece;
import com.takonline.takonline.game.model.board.PieceColor;
import com.takonline.takonline.game.model.board.PieceType;
import com.takonline.takonline.game.repository.GameRepository;
import com.takonline.takonline.room.model.Room;
import com.takonline.takonline.room.model.RoomPlayer;
import com.takonline.takonline.room.model.RoomStatus;
import com.takonline.takonline.room.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RoomRepository roomRepository;

    public GameService(GameRepository gameRepository, RoomRepository roomRepository) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
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

        Game game = new Game(roomCode, players, 5);
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
            game.getBoard()
    );
}

public GameResponse placePiece(String gameId, String playerId, int row, int col) {
    Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    GamePlayer player = game.getPlayers().stream()
            .filter(p -> p.getPlayerId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not found in game"));

    if (!game.getCurrentTurnColor().equals(player.getColor())) {
        throw new IllegalStateException("It is not this player's turn");
    }

    PieceColor pieceColor = PieceColor.valueOf(player.getColor());
    Piece piece = new Piece(pieceColor, PieceType.FLAT);

    game.getBoard().placePiece(row, col, piece);

    String nextTurn = game.getCurrentTurnColor().equals("WHITE") ? "BLACK" : "WHITE";
    game.setCurrentTurnColor(nextTurn);

    gameRepository.save(game);

    return mapToResponse(game);
}
}