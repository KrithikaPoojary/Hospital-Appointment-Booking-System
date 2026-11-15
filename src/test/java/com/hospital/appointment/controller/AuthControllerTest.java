package com.hospital.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.dto.LoginRequest;
import com.hospital.appointment.dto.RegisterRequest;
import com.hospital.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register should create new user account")
    void testRegisterUser() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "testuser",
                "password123",
                "Test User",
                "test@example.com",
                "1234567890",
                "PATIENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful!"));
    }

    @Test
    @DisplayName("POST /api/auth/register should fail when username already exists")
    void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "duplicate",
                "password123",
                "User One",
                "one@example.com",
                "1234567890",
                "PATIENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        RegisterRequest req2 = new RegisterRequest(
                "duplicate",
                "password123",
                "User Two",
                "two@example.com",
                "9876543210",
                "PATIENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    @DisplayName("POST /api/auth/login should authenticate and return JWT token")
    void testLoginSuccess() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "loginuser",
                "secret123",
                "Login User",
                "login@example.com",
                "1234567890",
                "PATIENT"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest("loginuser", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }
}
