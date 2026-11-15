package com.hospital.appointment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.LoginRequest;
import com.hospital.appointment.dto.RegisterRequest;
import com.hospital.appointment.dto.SlotDto;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;
import com.hospital.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patientToken;
    private Slot testSlot;

    @BeforeEach
    void setUp() throws Exception {
        appointmentRepository.deleteAll();
        slotRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create doctor
        User docUser = User.builder()
                .username("doctorwho")
                .password(passwordEncoder.encode("docpass123"))
                .fullName("Dr. Who")
                .email("doctor@tardis.com")
                .role(Role.DOCTOR)
                .build();
        userRepository.save(docUser);

        Doctor doctor = Doctor.builder()
                .user(docUser)
                .specialty("Cardiology")
                .qualification("MD")
                .experienceYears(10)
                .build();
        doctorRepository.save(doctor);

        // 2. Create slot for doctor
        testSlot = Slot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .available(true)
                .build();
        slotRepository.save(testSlot);

        // 3. Register & Login patient to acquire JWT
        RegisterRequest patientReg = new RegisterRequest(
                "patientjoe",
                "patient123",
                "Joe Patient",
                "joe@example.com",
                "5551234567",
                "PATIENT"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientReg)));

        LoginRequest login = new LoginRequest("patientjoe", "patient123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        patientToken = root.get("token").asText();
    }

    @Test
    @DisplayName("End-to-End: Patient books an available slot, verifies in my-appointments, and cancels it")
    void testFullAppointmentLifecycle() throws Exception {
        // Step A: Book appointment
        AppointmentRequest bookReq = new AppointmentRequest(testSlot.getId(), "Heart checkup");

        MvcResult bookResult = mockMvc.perform(post("/api/appointments/book")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Who"))
                .andExpect(jsonPath("$.specialty").value("Cardiology"))
                .andReturn();

        JsonNode bookedJson = objectMapper.readTree(bookResult.getResponse().getContentAsString());
        long appointmentId = bookedJson.get("id").asLong();

        // Step B: Check my-appointments
        mockMvc.perform(get("/api/appointments/my-appointments")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(appointmentId))
                .andExpect(jsonPath("$[0].status").value("BOOKED"));

        // Step C: Cancel appointment
        mockMvc.perform(put("/api/appointments/" + appointmentId + "/cancel")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
