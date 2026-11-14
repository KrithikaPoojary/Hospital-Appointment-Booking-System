package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for rescheduling an existing appointment to a new slot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {

    @NotNull(message = "New Slot ID is required")
    private Long newSlotId;

    private String reason;
}
