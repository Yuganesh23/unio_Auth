package serviceInterface;


import java.util.List;
import org.springframework.http.ResponseEntity;
import dto.LoginRequestDTO;
import dto.LoginResponseDTO;
import dto.SignUpDto;
import entity.SignUpEntity;

public interface SignUpService {

    // Sign-up
    ResponseEntity<String> saveSignUpDetails(SignUpDto details);
    ResponseEntity<String> verifyOtp(String email, String otp);

    // Login
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    // Forgot Password
    ResponseEntity<String> forgotPasswordRequest(String email);
    ResponseEntity<String> verifyForgotPasswordOtp(String email, String otp);
    ResponseEntity<String> resetPassword(String email, String newPassword);

    // Admin / Debug
    ResponseEntity<List<SignUpEntity>> getAllUsers();
}


