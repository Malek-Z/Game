package com.game.repository;

import com.game.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    boolean existsByUsername(String username);
    List<Player> findTop10ByOrderByHighScoreDesc();
}