package com.hospital.appointment.controller;

import com.hospital.appointment.dto.DoctorDto;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.UserRepository;
import com.hospital.appointment.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for doctor search, specialty discovery, and profile management.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management and search endpoints")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all doctors or search by name/specialty")
    public ResponseEntity<List<DoctorDto>> getAllOrSearchDoctors(
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(doctorService.searchDoctors(search));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor profile by ID")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/specialty/{specialty}")
    @Operation(summary = "Filter doctors by exact specialty")
    public ResponseEntity<List<DoctorDto>> getDoctorsBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialty));
    }

    @GetMapping("/specialties")
    @Operation(summary = "List all distinct specialties")
    public ResponseEntity<List<String>> getAllSpecialties() {
        return ResponseEntity.ok(doctorService.getAllSpecialties());
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Create or update doctor profile (Doctor or Admin only)")
    public ResponseEntity<DoctorDto> updateDoctorProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorDto doctorDto) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        return ResponseEntity.ok(doctorService.createOrUpdateProfile(user.getId(), doctorDto));
    }
}
