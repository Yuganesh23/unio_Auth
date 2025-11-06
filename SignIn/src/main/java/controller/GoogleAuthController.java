package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import dto.AuthResponse;
import dto.TokensRequest;
import entity.SignUpEntity;
import repository.SignUpRepository;
import security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final String CLIENT_ID = "840990873779-enb3bqcqrhvolv6t0ubgcr8k0c0l7itf.apps.googleusercontent.com";

    @Autowired
    private SignUpRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    
    // Return OAuth2 login URLs
    @GetMapping("/login-urls")
    public Map<String, String> getOAuth2LoginUrls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("google", "http://localhost:8080/oauth2/authorization/google");
        urls.put("github", "http://localhost:8080/oauth2/authorization/github");
        urls.put("microsoft", "http://localhost:8080/oauth2/authorization/microsoft");
        return urls;
    }

    // Authenticate multiple Google ID tokens in one request
    @PostMapping("/google/multi")
    public ResponseEntity<?> googleLoginMultiple(@RequestBody TokensRequest tokensRequest) {
        List<AuthResponse> responses = new ArrayList<>();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        for (String tokenString : tokensRequest.getIdTokens()) {
            try {
                GoogleIdToken idToken = verifier.verify(tokenString);
                if (idToken != null) {
                    String email = idToken.getPayload().getEmail();
                    String jwt = generateJwtToken(email);
                    responses.add(new AuthResponse(jwt, "Login successful for " + email));
                } else {
                    responses.add(new AuthResponse(null, "Invalid token"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                responses.add(new AuthResponse(null, "Error verifying token: " + e.getMessage()));
            }
        }
        return ResponseEntity.ok(responses);
    }

    private String generateJwtToken(String email) {
        return jwtUtil.generateToken(email);
    }

    // OAuth2 login success endpoint for Spring Security OAuth2 flow
    @GetMapping("/success")
    public Map<String, Object> success(@AuthenticationPrincipal OAuth2User user) {
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        String picture = user.getAttribute("picture");

        SignUpEntity existingUser = repo.findByEmail(email).orElse(null);

        if (existingUser == null) {
            SignUpEntity newUser = new SignUpEntity();
            newUser.setEmail(email);
            newUser.setName(name);
            // Generate and hash a random password
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String randomPassword = UUID.randomUUID().toString();
            String encodedPassword = encoder.encode(randomPassword);

            newUser.setPassword(encodedPassword); 
            existingUser = repo.save(newUser);
        }

        String accessToken = jwtUtil.generateToken(existingUser.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(existingUser.getEmail());

        return Map.of(
                "message", "Google login successful",
                "name", existingUser.getName(),
                "email", existingUser.getEmail(),
                "picture", picture,
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    // OAuth2 login failure endpoint for Spring Security OAuth2 flow
    @GetMapping("/failure")
    public String failure(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if (exception != null) {
            exception.printStackTrace();
            return "OAuth2 login failed: " + exception.getMessage();
        }
        return "OAuth2 login failed: Unknown error";
    }

    // Refresh JWT token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            String email = jwtUtil.getSubjectFromToken(refreshToken);
            String newAccessToken = jwtUtil.generateToken(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Token refreshed successfully",
                    "accessToken", newAccessToken
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid or expired refresh token"
            ));
        }
    }

    // Authenticate single Google ID token sent from client frontend
    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");

        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "ID token is missing"));
        }

        // ✅ Remove the regex filter (Google tokens can include +, /, =)
        System.out.println("✅ Received Google ID token length: " + idTokenString.length());

        try {
            // ✅ Correct way: Parse directly from the token string
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            // Verify token directly
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, "Invalid or expired Google ID token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();

            if (emailVerified == null || !emailVerified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, "Email not verified"));
            }

            // ✅ Save or update user
            SignUpEntity existingUser = repo.findByEmail(email).orElse(null);
            if (existingUser == null) {
                SignUpEntity newUser = new SignUpEntity();
                newUser.setEmail(email);
                newUser.setName((String) payload.get("name"));
                newUser.setProvider("google"); // Set provider

                // Generate and hash a random password
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String randomPassword = UUID.randomUUID().toString();
                String encodedPassword = encoder.encode(randomPassword);
                
                newUser.setPassword(encodedPassword); // No password for Google users
                existingUser.setProvider("google"); // Update provider if exists
                existingUser = repo.save(newUser);
            }

            // ✅ Generate JWT tokens
            String jwt = jwtUtil.generateToken(existingUser.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(existingUser.getEmail());

            return ResponseEntity.ok(Map.of(
                    "jwt", jwt,
                    "refreshToken", refreshToken,
                    "message", "Google login successful for " + email
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Error verifying token: " + e.getMessage()));
        }
    }

}
