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
            // Disable CSRF for APIs (optional if you're using JWT)
            .csrf(csrf -> csrf.disable())

            // Configure which endpoints are public
            .authorizeHttpRequests(auth -> auth
                .antMatchers(
                    "/api/signup",
                    "/api/login",
                    "/api/otp/verify",
                    "/api/forgot-password",
                    "/api/forgot-password/verify-otp",
                    "/api/forgot-password/reset",
                    "/api/oauth2/success",
                    "/api/oauth2/failure",
                    "/oauth2/**",                // allow Spring's OAuth2 flow endpoints
                    "/login/oauth2/**"      ,
                    "/api/auth/google" // allow Google callback endpoint
                ).permitAll()
                .anyRequest().authenticated()
            )

            // Configure OAuth2 Login flow
            .oauth2Login(oauth -> oauth
                // Let Spring Security handle redirect flow properly
                .loginPage("/oauth2/authorization/google")
                .defaultSuccessUrl("/api/oauth2/success", true)
                .failureUrl("/api/oauth2/failure")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        return authenticationManagerBuilder.build();
    }
}
