package com.hospital.appointment.enums;

/**
 * Roles available in the system.
 * PATIENT — can search doctors & book appointments.
 * DOCTOR  — can manage availability slots.
 * ADMIN   — can view reports and manage users.
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN
}

