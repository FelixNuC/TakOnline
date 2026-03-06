package com.takonline.takonline.game.controller;

import com.takonline.takonline.game.dto.GameResponse;
import com.takonline.takonline.game.dto.PlacePieceRequest;
import com.takonline.takonline.game.service.GameService;
import org.springframework.web.bind.annotation.*;

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
    
}