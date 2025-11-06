package controller;

import java.util.Map;


import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import entity.SignUpEntity;
import repository.SignUpRepository;
import security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

@RestController
public class OAuth2Controller {

    @Autowired
    private SignUpRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;  // Inject JwtUtil to generate the token

    @GetMapping("/api/oauth2/success")
    public Map<String, Object> success(@AuthenticationPrincipal OAuth2User user) {
        // Extract the user's attributes from OAuth2User
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        String picture = user.getAttribute("picture");

        // Check if user already exists in the database
        SignUpEntity existingUser = repo.findByEmail(email).orElse(null);

        // If the user doesn't exist, create a new one
        if (existingUser == null) {
            SignUpEntity newUser = new SignUpEntity();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword(passwordEncoder.encode("GOOGLE_AUTH"));  // Placeholder password
            existingUser = repo.save(newUser);  // Save the new user
        }

        // Generate JWT access token and refresh token after successful login
        String accessToken = jwtUtil.generateToken(existingUser.getEmail()); // Access token
        String refreshToken = jwtUtil.generateRefreshToken(existingUser.getEmail()); // Refresh token

        // Return the tokens and user info
        return Map.of(
            "message", "Google login successful",
            "name", existingUser.getName(),
            "email", existingUser.getEmail(),
            "picture", picture,
            "accessToken", accessToken, // Return the access token
            "refreshToken", refreshToken // Return the refresh token
        );
    }

    @GetMapping("/api/oauth2/failure")
    public String failure() {
        return "Google login failed";
    }

    @PostMapping("/api/oauth2/refresh")
    public Map<String, Object> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        // Validate the refresh token first
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            // Extract the subject (e.g., email) from the refresh token
            String email = jwtUtil.getSubjectFromToken(refreshToken);
            
            // Generate a new access token using the email (subject)
            String newAccessToken = jwtUtil.generateToken(email);

            return Map.of(
                "message", "Token refreshed successfully",
                "accessToken", newAccessToken
            );
        } else {
            // Return error response if refresh token is invalid
            return Map.of(
                "message", "Invalid or expired refresh token"
            );
        }
    }
}
