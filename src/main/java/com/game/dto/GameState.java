package com.game.dto;

public class GameState {
    private String boardState;
    private int score;
    private boolean gameOver;
    private boolean win;

    public GameState(String boardState, int score, boolean gameOver, boolean win) {
        this.boardState = boardState;
        this.score = score;
        this.gameOver = gameOver;
        this.win = win;
    }

    public String getBoardState() { return boardState; }
    public int getScore() { return score; }
    public boolean isGameOver() { return gameOver; }
    public boolean isWin() { return win; }
}