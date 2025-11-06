package service;

import dto.LoginRequestDTO;
import dto.LoginResponseDTO;
import dto.SignUpDto;
import entity.SignUpEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.SignUpRepository;
import security.JwtUtil;
import serviceInterface.SignUpService;
import util.OtpUtil;

import java.util.*;

@Service
public class SignUpServiceImpl implements SignUpService {

    @Autowired
    SignUpRepository repo;
    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    // ===================== SIGN UP ===================== //

    @Override
    public ResponseEntity<String> saveSignUpDetails(SignUpDto details) {
        String email = details.getEmail().trim().toLowerCase();

        if (repo.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }

        String otp = OtpUtil.generateOtp(email);

        SignUpDto tempSignUp = new SignUpDto();
        tempSignUp.setEmail(email);
        tempSignUp.setName(details.getName());
        tempSignUp.setPassword(details.getPassword());
        tempSignUp.setOtp(otp);

        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok("OTP sent to your email.");
    }

    @Override
    public ResponseEntity<String> verifyOtp(String email, String userOtp) {
        email = email.trim().toLowerCase();

        if (!OtpUtil.isOtpValid(email, userOtp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        SignUpDto storedDetails = new SignUpDto();
        storedDetails.setEmail(email); // Assuming the SignUpDto is populated with the email data

        SignUpEntity newUser = new SignUpEntity();
        newUser.setEmail(email);
        newUser.setName(storedDetails.getName());
        newUser.setPassword(passwordEncoder.encode(storedDetails.getPassword()));

        repo.save(newUser);

        return ResponseEntity.ok("Registered successfully.");
    }

    // ===================== LOGIN ===================== //

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        return repo.findByEmail(loginRequestDTO.getEmail())
                .map(user -> {
                    if (passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getEmail());
                        return new LoginResponseDTO("Login successful", true, token);
                    } else {
                        return new LoginResponseDTO("Invalid password", false, null);
                    }
                })
                .orElse(new LoginResponseDTO("User not found", false, null));
    }

    // ============ FORGOT PASSWORD FLOW =============== //

    @Override
    public ResponseEntity<String> forgotPasswordRequest(String email) {
        email = email.trim().toLowerCase();

        if (repo.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered.");
        }

        String otp = OtpUtil.generateOtp(email);

        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok("OTP sent to your email for password reset.");
    }

    @Override
    public ResponseEntity<String> verifyForgotPasswordOtp(String email, String otp) {
        email = email.trim().toLowerCase();

        if (repo.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered.");
        }

        if (!OtpUtil.isOtpValid(email, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        return ResponseEntity.ok("OTP verified. You may now reset your password.");
    }

    @Override
    public ResponseEntity<String> resetPassword(String email, String newPassword) {
        email = email.trim().toLowerCase();

        Optional<SignUpEntity> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered.");
        }

        SignUpEntity user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }

    // ===================== ADMIN/UTIL ===================== //

    @Override
    public ResponseEntity<List<SignUpEntity>> getAllUsers() {
        return ResponseEntity.ok(repo.findAll());
    }
}