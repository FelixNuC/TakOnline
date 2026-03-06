package com.takonline.takonline.game.repository;

import com.takonline.takonline.game.model.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {

    private final Map<String, Game> gamesById = new ConcurrentHashMap<>();
    private final Map<String, Game> gamesByRoomCode = new ConcurrentHashMap<>();

    public Game save(Game game) {
        gamesById.put(game.getGameId(), game);
        gamesByRoomCode.put(game.getRoomCode(), game);
        return game;
    }

    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(gamesById.get(gameId));
    }

    public Optional<Game> findByRoomCode(String roomCode) {
        return Optional.ofNullable(gamesByRoomCode.get(roomCode));
    }
}