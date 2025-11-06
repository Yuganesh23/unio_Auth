package security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class JwtUtil {

    // Injecting jwt.secret from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Token validity time in milliseconds (1 hour)
    private final long jwtExpirationMs = 3600000; // 1 hour
    private final long refreshTokenExpirationMs = 86400000; // 24 hours (refresh token expiration)

    private Key key;

    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());

    @PostConstruct
    public void init() {
        // Initialize the key after jwtSecret has been injected
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate JWT token using the subject (usually username or email)
    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)                   // who is the token for (email, username, etc.)
                .setIssuedAt(new Date())               // issued time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // expiry time
                .signWith(key, SignatureAlgorithm.HS256) 
                .compact();
    }

    // Generate refresh token
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs)) // 24-hour expiration for refresh token
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract the subject (username/email) from a JWT token
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)        // set the signing key
                .build()
                .parseClaimsJws(token)     // parse the token
                .getBody()
                .getSubject();             // extract the subject claim (email/username)
    }

    // Validate the token: checks signature, expiration, structure
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token); // Will throw if the token is invalid/expired/malformed
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.severe("JWT token validation failed: " + e.getMessage());
            return false;
        }
    }

    // Extract token expiration date (for checking if expired)
    public Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    // Check if the token is expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Refresh token validation logic
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken) && !isTokenExpired(refreshToken);
    }
}
