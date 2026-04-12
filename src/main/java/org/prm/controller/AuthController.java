package org.prm.controller;

import org.prm.dto.AuthRequest;
import org.prm.dto.AuthResponse;
import org.prm.dto.RefreshRequest;
import org.prm.dto.UserRequest;
import org.prm.entity.RefreshToken;
import org.prm.entity.User;
import org.prm.repository.UserRepository;
import org.prm.security.JwtUtil;
import org.prm.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserRequest userRequest) {

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setUsername(userRequest.getUsername());
        user.setPassword(encoder.encode(userRequest.getPassword()));
        userRepo.save(user);

        return new ResponseEntity<>(Map.of("message", "User registration successful"), HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            String accessToken = jwtUtil.generateAccessToken(request.getUsername());

            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(request.getUsername());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));

        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {

        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        System.out.println(request.getRefreshToken());
        return ResponseEntity.ok(refreshTokenService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest request) {
        System.out.println("Logout executed");

        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("refreshToken is required");
        }

        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok("Logged out");
    }

}
