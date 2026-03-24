package org.prm.service;

import org.prm.dto.AuthResponse;
import org.prm.entity.RefreshToken;
import org.prm.repository.RefreshTokenRepository;
import org.prm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_EXPIRY = Duration.ofDays(2);

    @Autowired
    private RefreshTokenRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    public RefreshToken createRefreshToken(String username) {

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        token.setExpiryDate(Instant.now().plus(REFRESH_EXPIRY));
        token.setRevoked(false);

        return repo.save(token);
    }

    public AuthResponse refresh(String refreshToken) {

        RefreshToken token = repo.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // 🔥 ROTATION
        token.setRevoked(true);
        repo.save(token);

        RefreshToken newToken = createRefreshToken(token.getUsername());
        String newAccessToken =
                jwtUtil.generateAccessToken(token.getUsername());

        return new AuthResponse(newAccessToken, newToken.getToken());
    }

    public void revoke(String refreshToken) {
        repo.findByToken(refreshToken).ifPresent(t -> {
        												t.setRevoked(true);
        												repo.save(t);
                									});
        System.out.println("Token revoked");
    }
}

/*
* revoked = false → ✅ session is active
  revoked = true → ❌ session is terminated
* */
