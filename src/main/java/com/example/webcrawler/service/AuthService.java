package com.example.webcrawler.service;

import com.example.webcrawler.entity.Role;
import com.example.webcrawler.entity.User;
import com.example.webcrawler.repository.RoleRepository;
import com.example.webcrawler.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Înregistrează un nou utilizator cu rolul implicit ROLE_USER.
     * @param username Numele de utilizator.
     * @param password Parola (necriptată).
     * @param email Adresa de email.
     * @return Utilizatorul înregistrat sau Optional.empty() dacă username-ul/email-ul există deja.
     */
    @Transactional
    public Optional<User> registerNewUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Încercare de înregistrare cu username existent: {}", username);
            return Optional.empty();
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Încercare de înregistrare cu email existent: {}", email);
            return Optional.empty();
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));


        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.info("Rolul ROLE_USER nu există, îl creez.");
                    return roleRepository.save(new Role("ROLE_USER"));
                });

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User savedUser = userRepository.save(user);
        log.info("Utilizator nou înregistrat: {}", savedUser.getUsername());
        return Optional.of(savedUser);
    }

    /**
     * Adaugă un rol unui utilizator existent.
     * @param username Numele de utilizator.
     * @param roleName Numele rolului de adăugat (ex: "ROLE_ADMIN").
     * @return true dacă rolul a fost adăugat, false altfel.
     */
    @Transactional
    public boolean addRoleToUser(String username, String roleName) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Role> roleOpt = roleRepository.findByName(roleName);

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            User user = userOpt.get();
            Role role = roleOpt.get();
            if (user.getRoles().add(role)) {
                userRepository.save(user);
                log.info("Rolul {} adăugat utilizatorului {}", roleName, username);
                return true;
            }
            log.info("Utilizatorul {} are deja rolul {}", username, roleName);
            return false; // Utilizatorul are deja rolul
        }
        log.warn("Nu s-a putut adăuga rolul {} utilizatorului {}. Utilizator sau rol inexistent.", roleName, username);
        return false;
    }
}