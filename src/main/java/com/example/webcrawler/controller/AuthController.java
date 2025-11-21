package com.example.webcrawler.controller;

import com.example.webcrawler.entity.User;
import com.example.webcrawler.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    protected AuthService authService;

    /**
     * Afișează pagina de login.
     * @param error Parametru opțional pentru a indica o eroare de login.
     * @param logout Parametru opțional pentru a indica un logout reușit.
     * @param model Modelul Thymeleaf.
     * @return Numele template-ului HTML (login.html).
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Nume de utilizator sau parolă incorecte.");
        }
        if (logout != null) {
            model.addAttribute("message", "Ai fost deconectat cu succes.");
        }
        return "login";
    }

    /**
     * Afișează pagina de înregistrare.
     * @param model Modelul Thymeleaf.
     * @return Numele template-ului HTML (register.html).
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Procesează cererea de înregistrare a unui nou utilizator.
     * @param user Obiectul User trimis din formular.
     * @param redirectAttributes Atribute pentru redirecționare.
     * @return Redirecționează către pagina de login cu un mesaj.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        Optional<User> registeredUser = authService.registerNewUser(user.getUsername(), user.getPassword(), user.getEmail());

        if (registeredUser.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Înregistrare reușită! Te poți autentifica acum.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Înregistrarea a eșuat. Numele de utilizator sau email-ul există deja.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }
    }
}