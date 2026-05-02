package com.game.dto;

import com.game.model.Player;

public class LeaderboardDTO {
    private Player player;
    private int score;

    public LeaderboardDTO(Player player, int score) {
        this.player = player;
        this.score = score;
    }

    public Player getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }
}