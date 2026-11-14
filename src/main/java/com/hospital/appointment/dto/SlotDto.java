package com.hospital.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data transfer object representing a bookable doctor time slot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotDto {

    private Long id;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private String doctorName;
    private String specialty;

    @NotNull(message = "Slot date is required")
    @FutureOrPresent(message = "Slot date must be today or in the future")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private boolean available;
}
