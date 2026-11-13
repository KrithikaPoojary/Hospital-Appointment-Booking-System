package com.hospital.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API message response — used for success/error messages
 * like "Registration successful" or "Appointment cancelled".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;
}

