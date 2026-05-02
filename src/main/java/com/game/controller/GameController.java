package com.game.controller;

import com.game.dto.GameState;
import com.game.model.GameSession;
import com.game.model.Player;
import com.game.service.GameService;
import com.game.service.GameSessionService;
import com.game.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GameController {

    private final GameService gameService;
    private final GameSessionService gameSessionService;
    private final PlayerService playerService;

    @Autowired
    public GameController(
            GameService gameService,
            GameSessionService gameSessionService,
            PlayerService playerService) {
        this.gameService = gameService;
        this.gameSessionService = gameSessionService;
        this.playerService = playerService;
    }

    // ── Guard helper ──────────────────────────────────────────────────────

    private Player getPlayerOrNull(HttpSession session) {
        return (Player) session.getAttribute("player");
    }

    // ── GET /menu → level selection + saved games ─────────────────────────

    @GetMapping("/menu")
    public String menu(Model model, HttpSession session) {
        Player player = getPlayerOrNull(session);
        if (player == null)
            return "redirect:/";

        Player freshPlayer = playerService.findById(player.getId());
        session.setAttribute("player", freshPlayer);

        List<GameSession> savedGames = gameSessionService.findByPlayer(freshPlayer)
                .stream()
                .filter(g -> !g.isFinished())
                .collect(Collectors.toList());

        int bestScore = gameSessionService.getBestScore(player);

        model.addAttribute("player", freshPlayer);
        model.addAttribute("savedGames", savedGames);
        model.addAttribute("bestScore", bestScore);
        return "menu";
    }

    // ── POST /start → create new GameSession ─────────────────────────────

    @PostMapping("/start")
    public String startGame(@RequestParam("size") int size, HttpSession session) {

        Player player = getPlayerOrNull(session);
        if (player == null)
            return "redirect:/";

        int target;
        switch (size) {
            case 3:
                target = 1024;
                break;
            case 5:
                target = 4096;
                break;
            default: // الحجم 4
                target = 2048;
                break;
        }

        GameSession game = new GameSession();
        game.setSize(size);
        game.setTarget(target);
        game.setScore(0);
        game.setFinished(false);
        game.setBoardState(gameService.initializeBoard(size));
        game.setPlayer(player);

        GameSession saved = gameSessionService.save(game);
        session.setAttribute("gameId", saved.getId());
        return "redirect:/game";
    }

    // ── GET /game → render board via Thymeleaf ────────────────────────────

    @GetMapping("/game")
    public String gamePage(Model model, HttpSession session) {
        Player player = getPlayerOrNull(session);
        if (player == null)
            return "redirect:/";

        Long gameId = (Long) session.getAttribute("gameId");
        if (gameId == null)
            return "redirect:/menu";

        GameSession game = gameSessionService.findById(gameId);
        model.addAttribute("game", game);
        model.addAttribute("player", player);
        return "game";
    }

    // ── POST /move → AJAX, returns JSON ──────────────────────────────────

    @PostMapping(value = "/move", produces = "application/json")
    @ResponseBody
    public GameState move(@RequestParam("direction") String direction, HttpSession session) {
        Long gameId = (Long) session.getAttribute("gameId");
        if (gameId == null) {
            return new GameState("", 0, true, false);
        }

        GameSession game = gameSessionService.findById(gameId);
        GameService.MoveResult result = gameService.move(
                game.getBoardState(),
                game.getSize(),
                game.getTarget(),
                direction,
                game.getScore());

        game.setBoardState(result.boardState);
        game.setScore(result.score);
        boolean ended = result.gameOver || result.win;
        game.setFinished(ended);
        gameSessionService.save(game);

        if (ended) {
            Player player = getPlayerOrNull(session);
            if (player != null) {
                playerService.updateHighScore(player, result.score);
                session.setAttribute("player", playerService.findById(player.getId()));
            }
        }

        return new GameState(result.boardState, result.score, result.gameOver, result.win);
    }

    // ── POST /save → persist and go back to menu ─────────────────────────

    @PostMapping("/save")
    public String save(HttpSession session) {
        Long gameId = (Long) session.getAttribute("gameId");
        if (gameId != null) {
            GameSession game = gameSessionService.findById(gameId);
            game.setFinished(false);
            gameSessionService.save(game);
        }
        session.removeAttribute("gameId");
        return "redirect:/menu";
    }

    // ── GET /resume/{id} → load game and redirect to /game ─────────────────

    @GetMapping("/resume/{id}")
    public String resume(@PathVariable("id") Long id, HttpSession session) {
        Player player = getPlayerOrNull(session);
        if (player == null)
            return "redirect:/";

        GameSession game = gameSessionService.findById(id);

        if (!game.getPlayer().getId().equals(player.getId())) {
            return "redirect:/menu";
        }

        session.setAttribute("gameId", game.getId());
        return "redirect:/game";
    }

    // ── POST /delete/{id} → delete game and go back to menu ─────────────────

    @PostMapping("/delete/{id}")
    public String deleteGame(@PathVariable("id") Long id, HttpSession session) {
        Player player = getPlayerOrNull(session);
        if (player == null) {
            return "redirect:/";
        }

        GameSession game = gameSessionService.findById(id);

        if (game.getPlayer().getId().equals(player.getId())) {
            gameSessionService.delete(id);
        }

        Long currentGameId = (Long) session.getAttribute("gameId");
        if (id.equals(currentGameId)) {
            session.removeAttribute("gameId");
        }

        return "redirect:/menu";
    }
}