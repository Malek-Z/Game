package com.game.controller;

import com.game.model.GameSession;
import com.game.service.GameService;
import com.game.repository.GameSessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GameController {

    private final GameService gameService;
    private final GameSessionRepository gameRepository;

    public GameController(GameService gameService, GameSessionRepository gameRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }

    @PostMapping("/start")
    public String startGame(HttpSession session) {
        GameSession game = new GameSession();
        game.setSize(4);
        game.setTarget(2048);
        game.setScore(0);
        game.setBoardState(gameService.initializeBoard(4)); 
        
        session.setAttribute("game", game);
        return "redirect:/game";
    }

    @PostMapping("/move")
    @ResponseBody
    public GameState move(@RequestParam String direction, HttpSession session) {
        GameSession game = (GameSession) session.getAttribute("game");

        GameService.MoveResult result = gameService.move(
            game.getBoardState(),
            game.getSize(),
            game.getTarget(),
            direction,
            game.getScore()
        );

        game.setBoardState(result.boardState);
        game.setScore(result.score);
        game.setFinished(result.gameOver || result.win);

        session.setAttribute("game", game);
        gameSessionService.save(game);

        return new GameState(result.boardState, result.score, result.gameOver, result.win);
    }
}