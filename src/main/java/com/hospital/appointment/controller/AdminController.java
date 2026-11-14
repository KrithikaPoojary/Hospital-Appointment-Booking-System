package com.hospital.appointment.controller;

import com.hospital.appointment.dto.AppointmentDto;
import com.hospital.appointment.dto.DashboardStatsDto;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.UserRepository;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for hospital administration, reporting metrics, and user management.
 * All endpoints strictly restricted to users with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administration and reporting endpoints")
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get hospital appointment statistics and operational metrics")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        long totalPatients = userRepository.findByRole(Role.PATIENT).size();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        long activeBookings = appointmentRepository.countByStatus(AppointmentStatus.BOOKED);
        long cancelled = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        long completed = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long rescheduled = appointmentRepository.countByStatus(AppointmentStatus.RESCHEDULED);

        // Aggregate appointments by specialty
        Map<String, Long> specialtyCounts = new HashMap<>();
        appointmentRepository.findAll().forEach(app -> {
            String specialty = app.getDoctor().getSpecialty();
            specialtyCounts.put(specialty, specialtyCounts.getOrDefault(specialty, 0L) + 1);
        });

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointments(totalAppointments)
                .activeBookings(activeBookings)
                .cancelledAppointments(cancelled)
                .completedAppointments(completed)
                .rescheduledAppointments(rescheduled)
                .appointmentsBySpecialty(specialtyCounts)
                .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get all appointments with optional date range filter")
    public ResponseEntity<List<AppointmentDto>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(
                    appointmentRepository.findBetweenDates(startDate, endDate).stream()
                            .map(appointmentService::mapToDto)
                            .collect(Collectors.toList()));
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered accounts")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "Update appointment status (e.g. mark COMPLETED)")
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointment.setStatus(newStatus);
            Appointment updated = appointmentRepository.save(appointment);
            return ResponseEntity.ok(appointmentService.mapToDto(updated));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }
}
