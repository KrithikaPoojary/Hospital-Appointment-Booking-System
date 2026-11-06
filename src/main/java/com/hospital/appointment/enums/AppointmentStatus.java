package com.hospital.appointment.enums;

/**
 * Lifecycle states of an appointment.
 * BOOKED      — slot reserved by a patient.
 * CANCELLED   — patient or doctor cancelled.
 * COMPLETED   — appointment has taken place.
 * RESCHEDULED — moved to a different slot.
 */
public enum AppointmentStatus {
    BOOKED,
    CANCELLED,
    COMPLETED,
    RESCHEDULED
}

