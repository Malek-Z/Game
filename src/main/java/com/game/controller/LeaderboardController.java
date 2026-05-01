package com.game.controller;

import com.game.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeaderboardController {

    private final PlayerService playerService;

    public LeaderboardController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // ── GET /leaderboard ──────────────────────────────────────────────────

    @GetMapping("/leaderboard")
    public String leaderboard(Model model, HttpSession session) {
        if (session.getAttribute("player") == null) {
            return "redirect:/";
        }

        model.addAttribute("players", playerService.getLeaderboard());
        model.addAttribute("currentPlayer", session.getAttribute("player"));

        return "leaderboard";
    }
}