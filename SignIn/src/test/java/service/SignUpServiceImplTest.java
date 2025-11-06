package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import dto.LoginRequestDTO;
import dto.LoginResponseDTO;
import dto.SignUpDto;
import entity.SignUpEntity;
import repository.SignUpRepository;
import security.JwtUtil;
import util.OtpUtil;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)

public class SignUpServiceImplTest {

    @InjectMocks
    private SignUpServiceImpl signUpService;  // Service class to be tested

    @Mock
    private SignUpRepository signUpRepository;  // Mocked SignUpRepository

    @Mock
    private EmailService emailService;  // Mocked EmailService

    @Mock
    private PasswordEncoder passwordEncoder;  // Mocked PasswordEncoder

    @Mock
    private JwtUtil jwtUtil;  // Mocked JwtUtil

    @Mock
    private OtpUtil otpUtil;  // Mocked OtpUtil

    @Autowired
     SignUpDto signUpDto;
    @Autowired 
    SignUpEntity signUpEntity;

    @BeforeEach
    public void setUp() {
        // Initialize a SignUpDto object for testing
        signUpDto = new SignUpDto();
        signUpDto.setName("John Doe");
        signUpDto.setEmail("john.doe@example.com");
        signUpDto.setPassword("password123");

        // Initialize a SignUpEntity for saving to DB
        signUpEntity = new SignUpEntity();
        signUpEntity.setEmail("john.doe@example.com");
        signUpEntity.setName("John Doe");
        signUpEntity.setPassword("encodedPassword");
    }

    @Test
    public void testSaveSignUpDetails_EmailAlreadyExists() {
        // Mocking the repository method to return a user if email exists
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(signUpEntity));

        // Calling the service method
        ResponseEntity<String> response = signUpService.saveSignUpDetails(signUpDto);

        // Asserting the response
        assertEquals("Email already exists.", response.getBody());
    }

    @Test
    public void testSaveSignUpDetails_Success() {
        // Mocking repository and OTP generation
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.empty());
        when(OtpUtil.generateOtp(signUpDto.getEmail())).thenReturn("123456");

        // Calling the service method
        ResponseEntity<String> response = signUpService.saveSignUpDetails(signUpDto);

        // Verifying that the email was sent
        verify(emailService, times(1)).sendOtpEmail(signUpDto.getEmail(), "123456");

        // Asserting the response
        assertEquals("OTP sent to your email.", response.getBody());
    }

    @Test
    public void testVerifyOtp_InvalidOtp() {
        // Mocking OTP validation to return false
        when(OtpUtil.isOtpValid(signUpDto.getEmail(), "123456")).thenReturn(false);

        // Calling the service method
        ResponseEntity<String> response = signUpService.verifyOtp(signUpDto.getEmail(), "123456");

        // Asserting the response
        assertEquals("Invalid or expired OTP.", response.getBody());
    }

    @Test
    public void testVerifyOtp_Success() {
        // Mocking OTP validation to return true
        when(OtpUtil.isOtpValid(signUpDto.getEmail(), "123456")).thenReturn(true);

        // Mocking password encoding and saving the user
        when(passwordEncoder.encode(signUpDto.getPassword())).thenReturn("encodedPassword");

        // Mocking save method to return the entity
        when(signUpRepository.save(any(SignUpEntity.class))).thenReturn(signUpEntity);

        // Calling the service method
        ResponseEntity<String> response = signUpService.verifyOtp(signUpDto.getEmail(), "123456");

        // Asserting the response
        assertEquals("Registered successfully.", response.getBody());
    }

    @Test
    public void testLogin_UserNotFound() {
        // Mocking repository to return empty if user does not exist
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.empty());

        // Creating a login request
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(signUpDto.getEmail(), "password123");

        // Calling the service method
        LoginResponseDTO response = signUpService.login(loginRequestDTO);

        // Asserting the response
        assertEquals("User not found", response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    public void testLogin_InvalidPassword() {
        // Mocking the repository to return an existing user
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(signUpEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Creating a login request
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(signUpDto.getEmail(), "wrongPassword");

        // Calling the service method
        LoginResponseDTO response = signUpService.login(loginRequestDTO);

        // Asserting the response
        assertEquals("Invalid password", response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    public void testLogin_Success() {
        // Mocking the repository to return an existing user
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(signUpEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(signUpDto.getEmail())).thenReturn("sampleToken");

        // Creating a login request
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(signUpDto.getEmail(), "password123");

        // Calling the service method
        LoginResponseDTO response = signUpService.login(loginRequestDTO);

        // Asserting the response
        assertEquals("Login successful", response.getMessage());
        assertTrue(response.isSuccess());
        assertNotNull(response.getToken());
    }

    @Test
    public void testForgotPasswordRequest_EmailNotRegistered() {
        // Mocking the repository to return empty if email is not registered
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.empty());

        // Calling the service method
        ResponseEntity<String> response = signUpService.forgotPasswordRequest(signUpDto.getEmail());

        // Asserting the response
        assertEquals("Email not registered.", response.getBody());
    }

    @Test
    public void testForgotPasswordRequest_Success() {
        // Mocking the repository to return an existing user
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(signUpEntity));
        when(OtpUtil.generateOtp(signUpDto.getEmail())).thenReturn("123456");

        // Calling the service method
        ResponseEntity<String> response = signUpService.forgotPasswordRequest(signUpDto.getEmail());

        // Verifying OTP email sent
        verify(emailService, times(1)).sendOtpEmail(signUpDto.getEmail(), "123456");

        // Asserting the response
        assertEquals("OTP sent to your email for password reset.", response.getBody());
    }

    @Test
    public void testResetPassword_EmailNotRegistered() {
        // Mocking the repository to return empty if email is not registered
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.empty());

        // Calling the service method
        ResponseEntity<String> response = signUpService.resetPassword(signUpDto.getEmail(), "newPassword123");

        // Asserting the response
        assertEquals("Email not registered.", response.getBody());
    }

    @Test
    public void testResetPassword_Success() {
        // Mocking the repository to return an existing user
        when(signUpRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(signUpEntity));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // Calling the service method
        ResponseEntity<String> response = signUpService.resetPassword(signUpDto.getEmail(), "newPassword123");

        // Asserting the response
        assertEquals("Password reset successfully.", response.getBody());
    }
}
