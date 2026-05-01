package com.game.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    private final Random random = new Random();

    // 🎮 MAIN ENTRY
    public MoveResult move(String boardState, int size, int target, String direction, int currentScore) {
        int[][] grid = stringToGrid(boardState, size);

        if (isGameOver(grid) || hasWon(grid, target)) {
            return new MoveResult(
                boardState,
                currentScore,
                false,
                true,
                hasWon(grid, target)
            );
        }

        boolean moved;
        int gainedScore;

        MoveLineResult result;

        switch (direction.toLowerCase()) {
            case "left":
                result = moveLeft(grid);
                break;
            case "right":
                reverse(grid);
                result = moveLeft(grid);
                reverse(grid);
                break;
            case "up":
                transpose(grid);
                result = moveLeft(grid);
                transpose(grid);
                break;
            case "down":
                transpose(grid);
                reverse(grid);
                result = moveLeft(grid);
                reverse(grid);
                transpose(grid);
                break;
            default:
                result = new MoveLineResult(false, 0);
        }

        moved = result.moved;
        gainedScore = result.score;

        if (moved) {
            addRandomTile(grid);
        }

        return new MoveResult(
            gridToString(grid),
            currentScore + gainedScore,
            moved,
            isGameOver(grid),
            hasWon(grid, target)
        );
    }

    // ⬅️ MOVE LEFT
    private MoveLineResult moveLeft(int[][] grid) {
        boolean moved = false;
        int totalScore = 0;

        for (int i = 0; i < grid.length; i++) {
            int[] original = grid[i].clone();

            int[] compressed = compress(grid[i]);
            MergeResult mergeResult = merge(compressed);
            int[] finalRow = padRight(mergeResult.row, grid.length);

            grid[i] = finalRow;
            totalScore += mergeResult.score;

            if (!Arrays.equals(original, finalRow)) {
                moved = true;
            }
        }

        return new MoveLineResult(moved, totalScore);
    }

    private static class MoveLineResult {
        boolean moved;
        int score;

        MoveLineResult(boolean moved, int score) {
            this.moved = moved;
            this.score = score;
        }
    }

    // 🔹 Remove zeros
    private int[] compress(int[] row) {
        return Arrays.stream(row).filter(x -> x != 0).toArray();
    }

    // 🔹 Merge equal tiles
    private MergeResult merge(int[] row) {
        List<Integer> result = new ArrayList<>();
        int score = 0;

        for (int i = 0; i < row.length; i++) {
            if (i < row.length - 1 && row[i] == row[i + 1]) {
                int merged = row[i] * 2;
                result.add(merged);
                score += merged;
                i++;
            } else {
                result.add(row[i]);
            }
        }

        return new MergeResult(
            result.stream().mapToInt(i -> i).toArray(),
            score
        );
    }

    private static class MergeResult {
        int[] row;
        int score;

        MergeResult(int[] row, int score) {
            this.row = row;
            this.score = score;
        }
    }

    // 🔄 Reverse rows
    private void reverse(int[][] grid) {
        for (int[] row : grid) {
            for (int i = 0; i < row.length / 2; i++) {
                int temp = row[i];
                row[i] = row[row.length - i - 1];
                row[row.length - i - 1] = temp;
            }
        }
    }

    // 🔁 Transpose matrix
    private void transpose(int[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = i; j < grid.length; j++) {
                int temp = grid[i][j];
                grid[i][j] = grid[j][i];
                grid[j][i] = temp;
            }
        }
    }

    // 🎲 Add random tile
    private void addRandomTile(int[][] grid) {
        List<int[]> empty = new ArrayList<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == 0) {
                    empty.add(new int[]{i, j});
                }
            }
        }

        if (!empty.isEmpty()) {
            int[] pos = empty.get(random.nextInt(empty.size()));
            grid[pos[0]][pos[1]] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }

    // ❌ Game Over
    public boolean isGameOver(int[][] grid) {
        int size = grid.length;

        // check empty
        for (int[] row : grid) {
            for (int val : row) {
                if (val == 0) return false;
            }
        }

        // check possible merges
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j < size - 1 && grid[i][j] == grid[i][j + 1]) return false;
                if (i < size - 1 && grid[i][j] == grid[i + 1][j]) return false;
            }
        }

        return true;
    }

    public boolean canMove(int[][] grid) {
        return !isGameOver(grid);
    }

    // 🏆 Win condition
    public boolean hasWon(int[][] grid, int target) {
        for (int[] row : grid) {
            for (int val : row) {
                if (val == target) return true;
            }
        }
        return false;
    }

    public String initializeBoard(int size) {
        int[][] grid = new int[size][size];

        addRandomTile(grid);
        addRandomTile(grid);

        return gridToString(grid);
    }

    // 🔁 String → Grid
    public int[][] stringToGrid(String state, int size) {
        int[][] grid = new int[size][size];
        String[] values = state.split(",");

        for (int i = 0; i < values.length; i++) {
            grid[i / size][i % size] = Integer.parseInt(values[i]);
        }

        return grid;
    }

    // 🔁 Grid → String
    public String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();

        for (int[] row : grid) {
            for (int val : row) {
                sb.append(val).append(",");
            }
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private int[] padRight(int[] row, int size) {
        int[] result = new int[size];
        System.arraycopy(row, 0, result, 0, row.length);
        return result;
    }

    public static class MoveResult {
        public String boardState;
        public int score;
        public boolean moved;
        public boolean gameOver;
        public boolean win;

        public MoveResult(String boardState, int score, boolean moved, boolean gameOver, boolean win) {
            this.boardState = boardState;
            this.score = score;
            this.moved = moved;
            this.gameOver = gameOver;
            this.win = win;
        }
    }
}