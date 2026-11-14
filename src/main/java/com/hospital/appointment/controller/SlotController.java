package com.hospital.appointment.controller;

import com.hospital.appointment.dto.MessageResponse;
import com.hospital.appointment.dto.SlotDto;
import com.hospital.appointment.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing and querying doctor availability slots.
 */
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Time slot availability and management endpoints")
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/doctor/{doctorId}/available")
    @Operation(summary = "Get all upcoming available slots for a specific doctor")
    public ResponseEntity<List<SlotDto>> getAvailableSlots(@PathVariable Long doctorId) {
        return ResponseEntity.ok(slotService.getAllAvailableFutureSlots(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/date")
    @Operation(summary = "Get doctor slots for a specific date")
    public ResponseEntity<List<SlotDto>> getDoctorSlotsByDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlotsByDoctorAndDate(doctorId, date));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Create an appointment slot (Doctor or Admin only)")
    public ResponseEntity<SlotDto> createSlot(@Valid @RequestBody SlotDto slotDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createSlot(slotDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Delete an unbooked slot (Doctor or Admin only)")
    public ResponseEntity<MessageResponse> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.ok(new MessageResponse("Slot successfully deleted"));
    }
}
