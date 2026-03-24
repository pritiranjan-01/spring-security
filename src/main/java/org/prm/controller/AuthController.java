	package org.prm.controller;

	import org.prm.dto.AuthRequest;
	import org.prm.dto.AuthResponse;
	import org.prm.dto.RefreshRequest;
	import org.prm.entity.RefreshToken;
	import org.prm.security.JwtUtil;
	import org.prm.service.RefreshTokenService;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.http.ResponseEntity;
	import org.springframework.security.authentication.AuthenticationManager;
	import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RestController;

	@RestController
	public class AuthController {

		@Autowired
		private AuthenticationManager  authenticationManager;

		@Autowired
		private JwtUtil jwtUtil;

		@Autowired
		private RefreshTokenService refreshTokenService;

		@PostMapping("/authenticate")
		public ResponseEntity<AuthResponse> generateToken(@RequestBody AuthRequest request) {
			try {
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

				String accessToken = jwtUtil.generateAccessToken(request.getUsername());

				RefreshToken refreshToken =
						refreshTokenService.createRefreshToken(request.getUsername());

				return ResponseEntity.ok(new AuthResponse(accessToken,refreshToken.getToken()));
			} catch(Exception e) {
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
