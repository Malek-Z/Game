package com.game.service;

import com.game.model.Player;
import com.game.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // ── Register ──────────────────────────────────────────────────────────

    public Player register(String username, String password) {
        if (playerRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken: " + username);
        }
        Player player = new Player(username, password);
        return playerRepository.save(player);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    public Player login(String username, String password) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Player not found: " + username));

        if (!player.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        return player;
    }

    // ── High Score ────────────────────────────────────────────────────────

    public void updateHighScore(Player player, int newScore) {
        if (newScore > player.getHighScore()) {
            player.setHighScore(newScore);
            playerRepository.save(player);
        }
    }

    // ── Leaderboard ───────────────────────────────────────────────────────

    public List<Player> getLeaderboard() {
        return playerRepository.findTop10ByOrderByHighScoreDesc();
    }

    // ── General CRUD ──────────────────────────────────────────────────────

    public Optional<Player> findByUsername(String username) {
        return playerRepository.findByUsername(username);
    }

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }
}