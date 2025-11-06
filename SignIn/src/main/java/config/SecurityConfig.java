package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disabling CSRF protection (adjust if needed)
            .authorizeHttpRequests(auth -> auth
                // Explicitly permit access to these public APIs
                .antMatchers("/api/signup", "/api/login", "/api/otp/verify", "/api/forgot-password", "/api/forgot-password/verify-otp", "/api/forgot-password/reset").permitAll()

                // Secure all other requests that don't match the above patterns
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .defaultSuccessUrl("/api/oauth2/success", true)  // On success, redirect here
                .failureUrl("/api/oauth2/failure")  // On failure, redirect here
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Password encoder for traditional login
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        return authenticationManagerBuilder.build();
    }
}


