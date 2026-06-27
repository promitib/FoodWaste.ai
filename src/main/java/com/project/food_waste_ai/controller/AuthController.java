package com.project.food_waste_ai.controller;

import com.project.food_waste_ai.entity.User;
import com.project.food_waste_ai.repository.UserRepository;
import com.project.food_waste_ai.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // Creates a new user, hashes the password, returns a JWT immediately.

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        if (userRepository.findByUsername(body.get("username")).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setUsername(body.get("username"));
        user.setEmail(body.get("email"));
        user.setPassword(passwordEncoder.encode(body.get("password")));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // Validates credentials, returns a JWT on success.

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        body.get("username"),
                        body.get("password")
                )
        );

        String token = jwtService.generateToken(body.get("username"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}