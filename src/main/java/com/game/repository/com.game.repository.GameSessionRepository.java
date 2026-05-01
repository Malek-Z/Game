package com.game.repository;

import com.game.model.GameSession;
import com.game.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByPlayer(Player player);

    List<GameSession> findByFinishedFalse();

}