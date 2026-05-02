package com.game.service;

import com.game.dto.LeaderboardDTO;
import com.game.model.Player;
import com.game.repository.PlayerRepository;
import com.game.repository.GameSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GameSessionRepository GameSessionRepository;

    public PlayerService(PlayerRepository playerRepository, GameSessionRepository GameSessionRepository) {
        this.playerRepository = playerRepository;
        this.GameSessionRepository = GameSessionRepository;
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
        Player dbPlayer = playerRepository.findById(player.getId())
                .orElseThrow();

        if (newScore > dbPlayer.getHighScore()) {
            dbPlayer.setHighScore(newScore);
            playerRepository.save(dbPlayer);
        }
    }

    // ── Leaderboard ───────────────────────────────────────────────────────

    public List<LeaderboardDTO> getLeaderboard() {
        return GameSessionRepository.getBestScoresPerPlayer()
                .stream()
                .map(r -> new LeaderboardDTO(
                        (Player) r[0],
                        (Integer) r[1]))
                .collect(Collectors.toList());
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