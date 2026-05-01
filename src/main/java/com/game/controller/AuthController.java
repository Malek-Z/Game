package com.game.controller;

import com.game.model.Player;
import com.game.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final PlayerService playerService;

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // ── GET / → show login/register page ─────────────────────────────────

    @GetMapping("/")
    public String indexPage(HttpSession session) {
        // If already logged in, go straight to menu
        if (session.getAttribute("player") != null) {
            return "redirect:/menu";
        }
        return "index";
    }

    // ── POST /register ────────────────────────────────────────────────────

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        try {
            playerService.register(username, password);
            redirectAttributes.addFlashAttribute("success", "Account created! Please log in.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    // ── POST /login ───────────────────────────────────────────────────────

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Player player = playerService.login(username, password);
            session.setAttribute("player", player);
            return "redirect:/menu";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // ── POST /logout ──────────────────────────────────────────────────────

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}