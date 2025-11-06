package controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.LoginRequestDTO;
import dto.LoginResponseDTO;
import dto.SignUpDto;
import entity.SignUpEntity;
import serviceInterface.SignUpService;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
@RequestMapping("/api")
public class SignUpController {
	@Autowired
	SignUpService service;
	@PostMapping("/signup")
public ResponseEntity<String> SaveSignUpDetails(@RequestBody SignUpDto details){
	return service.saveSignUpDetails(details);
}
	@PostMapping("/otp/verify")
	public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> payload) {
	    String email = payload.get("email");
	    String otp = payload.get("otp");
	    return service.verifyOtp(email, otp);
	}

	
	 @PostMapping("/login")
	    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO) {
	        return service.login(loginRequestDTO);
	    }
	// 1. Request OTP for Forgot Password
	 @PostMapping("/forgot-password")
	 public ResponseEntity<String> forgotPasswordRequest(@RequestBody Map<String, String> body) {
	     String email = body.get("email");
	     return service.forgotPasswordRequest(email);
	 }


	 // 2. Verify OTP
	 @PostMapping("/forgot-password/verify-otp")
	 public ResponseEntity<String> verifyForgotOtp(@RequestBody Map<String, String> payload) {
	     String email = payload.get("email");
	     String otp = payload.get("otp");
	     return service.verifyForgotPasswordOtp(email, otp);
	 }

	 // 3. Reset Password
	 @PostMapping("/forgot-password/reset")
	 public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> payload) {
	     String email = payload.get("email");
	     String newPassword = payload.get("newPassword");  // Ensure this matches the JSON field name

	     if (newPassword == null || newPassword.isEmpty()) {
	         return ResponseEntity.badRequest().body("New password cannot be empty.");
	     }

	     return service.resetPassword(email, newPassword);
	 }


@GetMapping("/users")
public ResponseEntity<List<SignUpEntity>> getAllUsers() {
    return service.getAllUsers();
}
}
