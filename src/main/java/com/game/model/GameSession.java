package com.game.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int score;

    @Column(length = 200, nullable = false)
    private String boardState;

    private int size;
    private int target;

    @Column(nullable = false)
    private boolean finished;

    @ManyToOne
    private Player player;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GameSession() {}

    // Getters & Setters
    public Long getId() { return id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}