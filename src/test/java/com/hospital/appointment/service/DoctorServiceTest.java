package com.hospital.appointment.service;

import com.hospital.appointment.dto.DoctorDto;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor sampleDoctor;
    private User doctorUser;

    @BeforeEach
    void setUp() {
        doctorUser = User.builder()
                .id(1L)
                .username("drsmith")
                .fullName("John Smith")
                .email("smith@hospital.com")
                .phone("1234567890")
                .role(Role.DOCTOR)
                .build();

        sampleDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialty("Cardiology")
                .qualification("MD, FACC")
                .experienceYears(12)
                .bio("Cardiology specialist")
                .build();
    }

    @Test
    @DisplayName("Should return all doctors mapped to DTOs")
    void testGetAllDoctors() {
        when(doctorRepository.findAll()).thenReturn(List.of(sampleDoctor));

        List<DoctorDto> doctors = doctorService.getAllDoctors();

        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        assertEquals("John Smith", doctors.get(0).getFullName());
        assertEquals("Cardiology", doctors.get(0).getSpecialty());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return doctors filtered by specialty")
    void testGetDoctorsBySpecialty() {
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology"))
                .thenReturn(List.of(sampleDoctor));

        List<DoctorDto> result = doctorService.getDoctorsBySpecialty("Cardiology");

        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getSpecialty());
    }

    @Test
    @DisplayName("Should search doctors by name or specialty keyword")
    void testSearchDoctors() {
        when(doctorRepository.searchByNameOrSpecialty("Smith"))
                .thenReturn(List.of(sampleDoctor));

        List<DoctorDto> result = doctorService.searchDoctors("Smith");

        assertFalse(result.isEmpty());
        assertEquals("John Smith", result.get(0).getFullName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when doctor ID does not exist")
    void testGetDoctorByIdNotFound() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(999L));
    }
}
