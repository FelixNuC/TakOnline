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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class GameService {

private final GameRepository gameRepository;
private final RoomRepository roomRepository;
private final SimpMessagingTemplate messagingTemplate;
private final Random random = new Random();

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

        Game game = new Game(roomCode, players, room.getBoardSize(), room.isVsAi(), room.getAiDifficulty());
        gameRepository.save(game);

        GameResponse response = mapToResponse(game);
        publishGameUpdate(response);

        return response;
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
            capstones,
            roomPlayer.isBot()
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
            game.getWinnerColor(),
            game.getRematchVotes(),
            new ArrayList<>(game.getRematchPlayerIds())
    );
}

public GameResponse requestRematch(String gameId, String playerId) {
    Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    if (game.getState() != GameState.FINISHED) {
        throw new IllegalStateException("Rematch is only available when game is finished");
    }

    GamePlayer requester = game.getPlayers().stream()
            .filter(player -> player.getPlayerId().equals(playerId))
            .findFirst()
            .orElse(null);

    if (requester == null) {
        throw new IllegalArgumentException("Player not found in game");
    }

    if (requester.isBot()) {
        throw new IllegalArgumentException("Bot player cannot request rematch");
    }

    game.addRematchVote(playerId);

    long requiredVotes = game.getPlayers().stream().filter(player -> !player.isBot()).count();
    if (requiredVotes <= 0) {
        requiredVotes = game.getPlayers().size();
    }

    if (game.getRematchVotes() >= requiredVotes) {
        int flats = TakPieceRules.flatsForBoard(game.getBoardSize());
        int capstones = TakPieceRules.capstonesForBoard(game.getBoardSize());

        List<GamePlayer> rematchPlayers = game.getPlayers().stream()
                .map(player -> new GamePlayer(
                        player.getPlayerId(),
                        player.getPlayerName(),
                        player.getColor(),
                        flats,
                        capstones,
                        player.isBot()
                ))
                .toList();

        game.resetForRematch(rematchPlayers);
    }

    gameRepository.save(game);

    GameResponse response = mapToResponse(game);
    publishGameUpdate(response);

    return response;
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

    if (!game.getBoard().getStack(row, col).isEmpty()) {
        throw new IllegalStateException("Cell is not empty");
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
} else if (hasAnyPlayerRunOutOfPlaceablePieces(game)) {
    resolveOutOfPiecesWin(game);
} else {
    String nextTurn = game.getCurrentTurnColor().equals("WHITE") ? "BLACK" : "WHITE";
    game.setCurrentTurnColor(nextTurn);
}
return savePublishAndMaybeRunAi(game);
}
private boolean hasRoadWin(Game game, String color) {
    int size = game.getBoard().getSize();

    if ("WHITE".equals(color)) {
        for (int row = 0; row < size; row++) {
            if (isRoadPieceForColor(game, row, 0, color)) {
                Set<String> visited = new HashSet<>();
                if (dfs(game, row, 0, color, visited)) {
                    return true;
                }
            }
        }
    } else if ("BLACK".equals(color)) {
        for (int col = 0; col < size; col++) {
            if (isRoadPieceForColor(game, 0, col, color)) {
                Set<String> visited = new HashSet<>();
                if (dfs(game, 0, col, color, visited)) {
                    return true;
                }
            }
        }
    }

    return false;
}
private void resolveOutOfPiecesWin(Game game) {
    List<GamePlayer> exhaustedPlayers = game.getPlayers().stream()
            .filter(player -> player.getRemainingFlats() == 0 && player.getRemainingCapstones() == 0)
            .toList();

    game.setState(GameState.FINISHED);

    if (exhaustedPlayers.size() == 1) {
        game.setWinnerColor(exhaustedPlayers.get(0).getColor());
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

    if ("WHITE".equals(color) && col == size - 1) {
        return true;
    }

    if ("BLACK".equals(color) && row == size - 1) {
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

    // Validate full move before mutating board state to avoid partial/losing moves on errors.
    validateMoveTargetsBeforeApply(game, fromRow, fromCol, parsedDirection, pickupCount, drops, originStack);

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
    }  else if (hasAnyPlayerRunOutOfPlaceablePieces(game)) {
    resolveOutOfPiecesWin(game);
} else {
        String nextTurn = game.getCurrentTurnColor().equals("WHITE") ? "BLACK" : "WHITE";
        game.setCurrentTurnColor(nextTurn);
    }

    return savePublishAndMaybeRunAi(game);
}

private void validateMoveTargetsBeforeApply(Game game,
                                            int fromRow,
                                            int fromCol,
                                            Direction direction,
                                            int pickupCount,
                                            List<Integer> drops,
                                            Stack originStack) {
    List<Piece> originPieces = originStack.getPieces();
    int previewStart = originPieces.size() - pickupCount;
    List<Piece> carriedPreview = new ArrayList<>(originPieces.subList(previewStart, originPieces.size()));

    int currentRow = fromRow;
    int currentCol = fromCol;
    int currentIndex = 0;

    for (int i = 0; i < drops.size(); i++) {
        Integer dropCount = drops.get(i);

        if (dropCount == null || dropCount <= 0) {
            throw new IllegalArgumentException("Each drop must be greater than 0");
        }

        if (currentIndex + dropCount > carriedPreview.size()) {
            throw new IllegalArgumentException("Invalid drop distribution for pickupCount");
        }

        currentRow = nextRow(currentRow, direction);
        currentCol = nextCol(currentCol, direction);

        Stack targetStack = game.getBoard().getStack(currentRow, currentCol);
        List<Piece> toDrop = takeNextDropGroup(carriedPreview, currentIndex, dropCount);
        boolean isLastDrop = (i == drops.size() - 1);

        validateTargetStackForMove(targetStack, toDrop, isLastDrop);

        currentIndex += dropCount;
    }
}

private void publishGameUpdate(GameResponse response) {
    messagingTemplate.convertAndSend("/topic/games/" + response.getGameId(), response);
    messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomCode() + "/game", response);
}

private GameResponse savePublishAndMaybeRunAi(Game game) {
    gameRepository.save(game);

    GameResponse response = mapToResponse(game);
    publishGameUpdate(response);

    if (game.getState() != GameState.IN_PROGRESS) {
        return response;
    }

    GamePlayer aiPlayer = game.getPlayers().stream()
            .filter(player -> player.isBot() && player.getColor().equals(game.getCurrentTurnColor()))
            .findFirst()
            .orElse(null);

    if (aiPlayer == null) {
        return response;
    }

    runAiTurn(game, aiPlayer);

    Game updatedGame = gameRepository.findById(game.getGameId()).orElse(game);
    return mapToResponse(updatedGame);
}

private void runAiTurn(Game game, GamePlayer aiPlayer) {
    String difficulty = game.getAiDifficulty() == null ? "NORMAL" : game.getAiDifficulty().toUpperCase();

    if (tryAiPlacePiece(game, aiPlayer, difficulty)) {
        return;
    }

    if (tryAiMoveStack(game, aiPlayer, difficulty)) {
        return;
    }

    throw new IllegalStateException("AI could not find a legal move");
}

private boolean tryAiPlacePiece(Game game, GamePlayer aiPlayer, String difficulty) {
    List<int[]> emptyCells = new ArrayList<>();
    for (int row = 0; row < game.getBoardSize(); row++) {
        for (int col = 0; col < game.getBoardSize(); col++) {
            if (game.getBoard().getStack(row, col).isEmpty()) {
                emptyCells.add(new int[]{row, col});
            }
        }
    }

    if (emptyCells.isEmpty()) {
        return false;
    }

    orderCellsByDifficulty(emptyCells, game, aiPlayer, difficulty);
    List<String> pieceTypes = buildAiPiecePriority(aiPlayer, difficulty);

    for (int[] cell : emptyCells) {
        for (String pieceType : pieceTypes) {
            try {
                placePiece(game.getGameId(), aiPlayer.getPlayerId(), cell[0], cell[1], pieceType);
                return true;
            } catch (Exception ignored) {
            }
        }
    }

    return false;
}

private boolean tryAiMoveStack(Game game, GamePlayer aiPlayer, String difficulty) {
    List<AiMoveCandidate> candidates = new ArrayList<>();

    for (int row = 0; row < game.getBoardSize(); row++) {
        for (int col = 0; col < game.getBoardSize(); col++) {
            Stack stack = game.getBoard().getStack(row, col);
            if (stack.isEmpty() || stack.getTopPiece() == null) {
                continue;
            }

            if (!aiPlayer.getColor().equals(stack.getTopPiece().getColor().name())) {
                continue;
            }

            int maxPickup = Math.min(stack.size(), game.getBoardSize());
            int pickupLimit = switch (difficulty) {
                case "EASY" -> 1;
                case "NORMAL" -> Math.min(2, maxPickup);
                default -> maxPickup;
            };
            for (int pickup = 1; pickup <= pickupLimit; pickup++) {
                addMoveCandidateIfInside(candidates, game, aiPlayer, row, col, "UP", row - 1, col, pickup, difficulty);
                addMoveCandidateIfInside(candidates, game, aiPlayer, row, col, "DOWN", row + 1, col, pickup, difficulty);
                addMoveCandidateIfInside(candidates, game, aiPlayer, row, col, "LEFT", row, col - 1, pickup, difficulty);
                addMoveCandidateIfInside(candidates, game, aiPlayer, row, col, "RIGHT", row, col + 1, pickup, difficulty);
            }
        }
    }

    if (candidates.isEmpty()) {
        return false;
    }

    if ("EASY".equals(difficulty)) {
        Collections.shuffle(candidates, random);
    } else {
        candidates.sort((a, b) -> Integer.compare(b.score, a.score));
    }

    for (AiMoveCandidate candidate : candidates) {
        try {
            moveStack(
                    game.getGameId(),
                    aiPlayer.getPlayerId(),
                    candidate.fromRow,
                    candidate.fromCol,
                    candidate.direction,
                    candidate.pickupCount,
                    List.of(candidate.pickupCount)
            );
            return true;
        } catch (Exception ignored) {
        }
    }

    return false;
}

private void addMoveCandidateIfInside(List<AiMoveCandidate> candidates,
                                      Game game,
                                      GamePlayer aiPlayer,
                                      int fromRow,
                                      int fromCol,
                                      String direction,
                                      int toRow,
                                      int toCol,
                                      int pickupCount,
                                      String difficulty) {
    if (!isInsideBoard(game, toRow, toCol)) {
        return;
    }

    int score = scoreCellForAi(game, aiPlayer, toRow, toCol);
    if ("HARD".equals(difficulty)) {
        score += pickupCount;
    }

    candidates.add(new AiMoveCandidate(
            fromRow,
            fromCol,
            direction,
            pickupCount,
            List.of(pickupCount),
            score
    ));
}

private List<String> buildAiPiecePriority(GamePlayer aiPlayer, String difficulty) {
    List<String> pieceTypes = new ArrayList<>();

    if (aiPlayer.getRemainingFlats() > 0) {
        pieceTypes.add("FLAT");
        pieceTypes.add("STANDING");
    }

    if (aiPlayer.getRemainingCapstones() > 0) {
        pieceTypes.add("CAPSTONE");
    }

    if ("EASY".equals(difficulty)) {
        Collections.shuffle(pieceTypes, random);
    } else if ("HARD".equals(difficulty) && pieceTypes.contains("CAPSTONE")) {
        pieceTypes.remove("CAPSTONE");
        pieceTypes.add(0, "CAPSTONE");
    }

    return pieceTypes;
}

private void orderCellsByDifficulty(List<int[]> cells, Game game, GamePlayer aiPlayer, String difficulty) {
    if ("EASY".equals(difficulty)) {
        Collections.shuffle(cells, random);
        return;
    }

    cells.sort((a, b) -> Integer.compare(
            scoreCellForAi(game, aiPlayer, b[0], b[1]),
            scoreCellForAi(game, aiPlayer, a[0], a[1])
    ));
}

private int scoreCellForAi(Game game, GamePlayer aiPlayer, int row, int col) {
    int sameColorAdj = 0;
    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    for (int[] dir : dirs) {
        int nr = row + dir[0];
        int nc = col + dir[1];
        if (!isInsideBoard(game, nr, nc)) {
            continue;
        }
        Stack stack = game.getBoard().getStack(nr, nc);
        if (stack.isEmpty() || stack.getTopPiece() == null) {
            continue;
        }
        if (aiPlayer.getColor().equals(stack.getTopPiece().getColor().name())) {
            sameColorAdj++;
        }
    }

    int center = game.getBoardSize() / 2;
    int distanceToCenter = Math.abs(row - center) + Math.abs(col - center);
    return sameColorAdj * 10 - distanceToCenter;
}

private static class AiMoveCandidate {
    private final int fromRow;
    private final int fromCol;
    private final String direction;
    private final int pickupCount;
    private final List<Integer> drops;
    private final int score;

    private AiMoveCandidate(int fromRow,
                            int fromCol,
                            String direction,
                            int pickupCount,
                            List<Integer> drops,
                            int score) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.direction = direction;
        this.pickupCount = pickupCount;
        this.drops = drops;
        this.score = score;
    }
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

    PieceType targetTopType = targetStack.getTopPiece().getType();

    if (targetTopType == PieceType.CAPSTONE) {
        throw new IllegalStateException("Cannot move onto a capstone");
    }

    if (targetTopType != PieceType.STANDING) {
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

private boolean hasAnyPlayerRunOutOfPlaceablePieces(Game game) {
    return game.getPlayers().stream()
            .anyMatch(player ->
                    player.getRemainingFlats() == 0 && player.getRemainingCapstones() == 0
            );
}
}
