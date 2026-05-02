package com.game.repository;

import com.game.model.GameSession;
import com.game.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByPlayer(Player player);

    List<GameSession> findByFinishedFalse();

    @Query("SELECT g.player, MAX(g.score) FROM GameSession g GROUP BY g.player ORDER BY MAX(g.score) DESC")
    List<Object[]> getBestScoresPerPlayer();

    @Query("""
                SELECT MAX(g.score)
                FROM GameSession g
                WHERE g.player = :player
            """)
    Integer findBestScoreByPlayer(@Param("player") Player player);

}