package controller;

import dto.LoginRequestDTO;
import dto.LoginResponseDTO;
import dto.SignUpDto;
import entity.SignUpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import serviceInterface.SignUpService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SignUpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SignUpService signUpService;

    @InjectMocks
    private SignUpController signUpController;

    private SignUpDto signUpDto;
    private LoginRequestDTO loginRequestDTO;
    private SignUpEntity signUpEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(signUpController).build();

        // Set up a mock user DTO for registration
        signUpDto = new SignUpDto();
        signUpDto.setName("John Doe");
        signUpDto.setEmail("john.doe@example.com");
        signUpDto.setPassword("password123");

        // Set up a mock user entity
        signUpEntity = new SignUpEntity();
        signUpEntity.setName("John Doe");
        signUpEntity.setEmail("john.doe@example.com");
        signUpEntity.setPassword("password123");

        // Set up a mock login DTO
        loginRequestDTO = new LoginRequestDTO("john.doe@example.com", "password123");
    }

    @Test
    void testSaveSignUpDetails() throws Exception {
        // Mock service behavior
        when(signUpService.saveSignUpDetails(signUpDto)).thenReturn(ResponseEntity.ok("OTP sent to your email."));

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\" }"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email."));

        verify(signUpService, times(1)).saveSignUpDetails(signUpDto);
    }

    @Test
    void testVerifyOtp() throws Exception {
        // Mock service behavior
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "john.doe@example.com");
        payload.put("otp", "123456");

        when(signUpService.verifyOtp("john.doe@example.com", "123456")).thenReturn(ResponseEntity.ok("Registered successfully."));

        mockMvc.perform(post("/api/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john.doe@example.com\", \"otp\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Registered successfully."));

        verify(signUpService, times(1)).verifyOtp("john.doe@example.com", "123456");
    }

    @Test
    void testLogin() throws Exception {
        // Mock service behavior
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("Login successful", true, "jwt_token");
        when(signUpService.login(loginRequestDTO)).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john.doe@example.com\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("jwt_token"));

        verify(signUpService, times(1)).login(loginRequestDTO);
    }

    @Test
    void testForgotPasswordRequest() throws Exception {
        // Mock service behavior
        when(signUpService.forgotPasswordRequest("john.doe@example.com")).thenReturn(ResponseEntity.ok("OTP sent to your email for password reset."));

        mockMvc.perform(post("/api/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john.doe@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email for password reset."));

        verify(signUpService, times(1)).forgotPasswordRequest("john.doe@example.com");
    }

    @Test
    void testVerifyForgotPasswordOtp() throws Exception {
        // Mock service behavior
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "john.doe@example.com");
        payload.put("otp", "654321");

        when(signUpService.verifyForgotPasswordOtp("john.doe@example.com", "654321")).thenReturn(ResponseEntity.ok("OTP verified. You may now reset your password."));

        mockMvc.perform(post("/api/forgot-password/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john.doe@example.com\", \"otp\": \"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verified. You may now reset your password."));

        verify(signUpService, times(1)).verifyForgotPasswordOtp("john.doe@example.com", "654321");
    }

    @Test
    void testResetPassword() throws Exception {
        // Mock service behavior
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "john.doe@example.com");
        payload.put("newPassword", "newPassword123");

        when(signUpService.resetPassword("john.doe@example.com", "newPassword123")).thenReturn(ResponseEntity.ok("Password reset successfully."));

        mockMvc.perform(post("/api/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john.doe@example.com\", \"newPassword\": \"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully."));

        verify(signUpService, times(1)).resetPassword("john.doe@example.com", "newPassword123");
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Mock service behavior
        when(signUpService.getAllUsers()).thenReturn(ResponseEntity.ok(List.of(signUpEntity)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(signUpService, times(1)).getAllUsers();
    }
}
