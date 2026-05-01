package com.game.service;

import com.game.model.GameSession;
import com.game.repository.GameSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameSessionService {

    private final GameSessionRepository repository;

    public GameSessionService(GameSessionRepository repository) {
        this.repository = repository;
    }

    public GameSession save(GameSession game) {
        return repository.save(game);
    }

    public GameSession findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    public List<GameSession> findAll() {
        return repository.findAll();
    }

    public List<GameSession> findActiveGames() {
        return repository.findByFinishedFalse();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}