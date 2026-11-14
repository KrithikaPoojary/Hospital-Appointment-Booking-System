package com.hospital.appointment.controller;

import com.hospital.appointment.dto.AppointmentDto;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.RescheduleRequest;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.UserRepository;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for booking, rescheduling, cancelling, and viewing appointments.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment lifecycle operations")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book an available appointment slot (Patient only)")
    public ResponseEntity<AppointmentDto> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(user.getId(), request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel an appointment (Patient, Doctor, or Admin)")
    public ResponseEntity<AppointmentDto> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, user.getId()));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reschedule an existing appointment to a new slot (Patient only)")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RescheduleRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, user.getId(), request));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user's appointments")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        if (user.getRole() == Role.PATIENT) {
            return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(user.getId()));
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElse(null);
            if (doctor == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctor.getId()));
        } else {
            return ResponseEntity.ok(appointmentService.getAllAppointments());
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get all appointments for a patient (Doctor or Admin)")
    public ResponseEntity<List<AppointmentDto>> getPatientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get all appointments for a doctor")
    public ResponseEntity<List<AppointmentDto>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
    }
}
