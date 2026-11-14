package com.hospital.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data transfer object encapsulating key hospital metrics for the admin dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long activeBookings;
    private long cancelledAppointments;
    private long completedAppointments;
    private long rescheduledAppointments;
    private Map<String, Long> appointmentsBySpecialty;
}
