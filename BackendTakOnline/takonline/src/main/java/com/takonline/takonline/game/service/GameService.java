package com.takonline.takonline.game.service;

import com.takonline.takonline.game.dto.GameResponse;
import com.takonline.takonline.game.model.Game;
import com.takonline.takonline.game.model.GamePlayer;
import com.takonline.takonline.game.model.GameState;
import com.takonline.takonline.game.model.board.Piece;
import com.takonline.takonline.game.model.board.PieceColor;
import com.takonline.takonline.game.model.board.PieceType;
import com.takonline.takonline.game.repository.GameRepository;
import com.takonline.takonline.game.rules.TakPieceRules;
import com.takonline.takonline.room.model.Room;
import com.takonline.takonline.room.model.RoomPlayer;
import com.takonline.takonline.room.model.RoomStatus;
import com.takonline.takonline.room.repository.RoomRepository;
import com.takonline.takonline.game.model.board.Stack;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.takonline.takonline.game.model.Direction;
import java.util.ArrayList;
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

int boardSize = room.getBoardSize();

List<GamePlayer> players = room.getPlayers().stream()
        .map(p -> mapRoomPlayerToGamePlayer(p, boardSize))
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

private GamePlayer mapRoomPlayerToGamePlayer(RoomPlayer roomPlayer, int boardSize) {

    int flats = TakPieceRules.flatsForBoard(boardSize);
    int capstones = TakPieceRules.capstonesForBoard(boardSize);

    return new GamePlayer(
            roomPlayer.getPlayerId(),
            roomPlayer.getPlayerName(),
            roomPlayer.getColor(),
            flats,
            capstones
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
switch (parsedPieceType) {

    case FLAT -> player.useFlat();

    case STANDING -> player.useFlat();

    case CAPSTONE -> player.useCapstone();
}

    Piece piece = new Piece(pieceColor, parsedPieceType);

game.getBoard().placePiece(row, col, piece);

if (hasRoadWin(game, player.getColor())) {
    game.setState(GameState.FINISHED);
    game.setWinnerColor(player.getColor());
} else if (isBoardFull(game) || hasAnyPlayerRunOutOfPlaceablePieces(game)) {
    resolveFlatWin(game);
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
private int countVisibleFlats(Game game, String color) {
    int count = 0;

    for (int row = 0; row < game.getBoard().getSize(); row++) {
        for (int col = 0; col < game.getBoard().getSize(); col++) {
            Stack stack = game.getBoard().getStack(row, col);

            if (stack.isEmpty() || stack.getTopPiece() == null) {
                continue;
            }

            Piece top = stack.getTopPiece();

            if (top.getColor().name().equals(color) && top.getType() == PieceType.FLAT) {
                count++;
            }
        }
    }

    return count;
}
private void resolveFlatWin(Game game) {
    int whiteFlats = countVisibleFlats(game, "WHITE");
    int blackFlats = countVisibleFlats(game, "BLACK");

    game.setState(GameState.FINISHED);

    if (whiteFlats > blackFlats) {
        game.setWinnerColor("WHITE");
    } else if (blackFlats > whiteFlats) {
        game.setWinnerColor("BLACK");
    } else {
        game.setWinnerColor("DRAW");
    }
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

public GameResponse moveStack(String gameId,
                              String playerId,
                              int fromRow,
                              int fromCol,
                              String direction,
                              int pickupCount,
                              List<Integer> drops) {

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

    Direction parsedDirection;
    try {
        parsedDirection = Direction.valueOf(direction.toUpperCase());
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid direction");
    }

    if (drops == null || drops.isEmpty()) {
        throw new IllegalArgumentException("Drops are required");
    }

    int totalDrops = drops.stream().mapToInt(Integer::intValue).sum();
    if (totalDrops != pickupCount) {
        throw new IllegalArgumentException("Sum of drops must equal pickupCount");
    }

    Stack originStack = game.getBoard().getStack(fromRow, fromCol);

    if (originStack.isEmpty()) {
        throw new IllegalStateException("Origin stack is empty");
    }

    if (originStack.getTopPiece() == null) {
        throw new IllegalStateException("Origin stack has no top piece");
    }

    if (!originStack.getTopPiece().getColor().name().equals(player.getColor())) {
        throw new IllegalStateException("You do not control this stack");
    }

    if (pickupCount <= 0) {
        throw new IllegalArgumentException("pickupCount must be greater than 0");
    }

    if (pickupCount > originStack.size()) {
        throw new IllegalArgumentException("pickupCount cannot be greater than stack size");
    }

    if (pickupCount > game.getBoardSize()) {
        throw new IllegalArgumentException("pickupCount cannot be greater than board size");
    }

    validatePathInsideBoard(game, fromRow, fromCol, parsedDirection, drops.size());

    List<Piece> carriedPieces = originStack.removeTopPieces(pickupCount);

    int currentRow = fromRow;
    int currentCol = fromCol;
    int currentIndex = 0;

for (int i = 0; i < drops.size(); i++) {
    Integer dropCount = drops.get(i);

    if (dropCount == null || dropCount <= 0) {
        throw new IllegalArgumentException("Each drop must be greater than 0");
    }

    currentRow = nextRow(currentRow, parsedDirection);
    currentCol = nextCol(currentCol, parsedDirection);

    Stack targetStack = game.getBoard().getStack(currentRow, currentCol);

    List<Piece> toDrop = takeNextDropGroup(carriedPieces, currentIndex, dropCount);
    boolean isLastDrop = (i == drops.size() - 1);

    validateTargetStackForMove(targetStack, toDrop, isLastDrop);
    flattenStandingIfNeeded(targetStack, toDrop, isLastDrop);

    targetStack.addPieces(toDrop);

    currentIndex += dropCount;
}

    if (hasRoadWin(game, player.getColor())) {
        game.setState(GameState.FINISHED);
        game.setWinnerColor(player.getColor());
    }  else if (isBoardFull(game) || hasAnyPlayerRunOutOfPlaceablePieces(game)) {
    resolveFlatWin(game);
} else {
        String nextTurn = game.getCurrentTurnColor().equals("WHITE") ? "BLACK" : "WHITE";
        game.setCurrentTurnColor(nextTurn);
    }

    gameRepository.save(game);

    GameResponse response = mapToResponse(game);
    messagingTemplate.convertAndSend("/topic/games/" + gameId, response);

    return response;
}
private void flattenStandingIfNeeded(Stack targetStack, List<Piece> toDrop, boolean isLastDrop) {
    if (targetStack.isEmpty()) {
        return;
    }

    if (targetStack.getTopPiece() == null) {
        return;
    }

    if (targetStack.getTopPiece().getType() != PieceType.STANDING) {
        return;
    }

    if (!isLastDrop) {
        return;
    }

    if (toDrop.isEmpty()) {
        return;
    }

    Piece topMovingPiece = toDrop.get(toDrop.size() - 1);

    if (topMovingPiece.getType() == PieceType.CAPSTONE) {
        targetStack.getTopPiece().setType(PieceType.FLAT);
    }
}
private List<Piece> takeNextDropGroup(List<Piece> carriedPieces, int startIndex, int dropCount) {
    return new ArrayList<>(carriedPieces.subList(startIndex, startIndex + dropCount));
}
private void validateTargetStackForMove(Stack targetStack, List<Piece> toDrop, boolean isLastDrop) {
    if (targetStack.isEmpty()) {
        return;
    }

    if (targetStack.getTopPiece() == null) {
        return;
    }

    if (targetStack.getTopPiece().getType() != PieceType.STANDING) {
        return;
    }

    if (!isLastDrop) {
        throw new IllegalStateException("Cannot move onto a standing stone before the last drop");
    }

    if (toDrop.isEmpty()) {
        throw new IllegalStateException("No pieces to drop");
    }

    Piece topMovingPiece = toDrop.get(toDrop.size() - 1);

    if (topMovingPiece.getType() != PieceType.CAPSTONE) {
        throw new IllegalStateException("Only a capstone can flatten a standing stone");
    }
}

private void validatePathInsideBoard(Game game, int fromRow, int fromCol, Direction direction, int steps) {
    int row = fromRow;
    int col = fromCol;

    for (int i = 0; i < steps; i++) {
        row = nextRow(row, direction);
        col = nextCol(col, direction);

        if (!isInsideBoard(game, row, col)) {
            throw new IllegalArgumentException("Move goes out of board");
        }
    }
}
private int nextRow(int currentRow, Direction direction) {
    return switch (direction) {
        case UP -> currentRow - 1;
        case DOWN -> currentRow + 1;
        case LEFT, RIGHT -> currentRow;
    };
}
private int nextCol(int currentCol, Direction direction) {
    return switch (direction) {
        case LEFT -> currentCol - 1;
        case RIGHT -> currentCol + 1;
        case UP, DOWN -> currentCol;
    };
}

private boolean isBoardFull(Game game) {
    for (int row = 0; row < game.getBoard().getSize(); row++) {
        for (int col = 0; col < game.getBoard().getSize(); col++) {
            if (game.getBoard().getStack(row, col).isEmpty()) {
                return false;
            }
        }
    }
    return true;
}
private boolean hasAnyPlayerRunOutOfPlaceablePieces(Game game) {
    return game.getPlayers().stream()
            .anyMatch(player ->
                    player.getRemainingFlats() == 0 && player.getRemainingCapstones() == 0
            );
}
}