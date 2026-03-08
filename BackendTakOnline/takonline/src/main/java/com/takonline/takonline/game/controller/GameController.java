package com.takonline.takonline.game.controller;

import com.takonline.takonline.game.dto.GameResponse;
import com.takonline.takonline.game.dto.PlacePieceRequest;
import com.takonline.takonline.game.dto.RematchRequest;
import com.takonline.takonline.game.service.GameService;
import org.springframework.web.bind.annotation.*;
import com.takonline.takonline.game.dto.MoveStackRequest;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/from-room/{roomCode}")
    public GameResponse createGameFromRoom(@PathVariable String roomCode) {
        return gameService.createGameFromRoom(roomCode);
    }

    @GetMapping("/room/{roomCode}")
    public GameResponse getGameByRoomCode(@PathVariable String roomCode) {
        return gameService.getGameByRoomCode(roomCode);
    }

@PostMapping("/{gameId}/moves/place")
public GameResponse placePiece(@PathVariable String gameId, @RequestBody PlacePieceRequest request) {
    return gameService.placePiece(
            gameId,
            request.getPlayerId(),
            request.getRow(),
            request.getCol(),
            request.getPieceType()
    );
}

@PostMapping("/{gameId}/moves/stack")
public GameResponse moveStack(@PathVariable String gameId, @RequestBody MoveStackRequest request) {
    return gameService.moveStack(
            gameId,
            request.getPlayerId(),
            request.getFromRow(),
            request.getFromCol(),
            request.getDirection(),
            request.getPickupCount(),
            request.getDrops()
    );
}

@PostMapping("/{gameId}/rematch")
public GameResponse requestRematch(@PathVariable String gameId, @RequestBody RematchRequest request) {
    return gameService.requestRematch(gameId, request.getPlayerId());
}
    
}
