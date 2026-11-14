package com.hospital.appointment.service;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Background scheduler that queries appointments scheduled for tomorrow
 * and automatically triggers email reminders to patients every morning.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    /**
     * Runs every day at 08:00 AM server time.
     * Cron: second, minute, hour, day-of-month, month, day-of-week
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Running daily appointment reminder job for date: {}", tomorrow);

        List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingForReminder(tomorrow);
        log.info("Found {} upcoming appointments to notify.", upcomingAppointments.size());

        for (Appointment appointment : upcomingAppointments) {
            try {
                emailService.sendAppointmentReminder(
                        appointment.getPatient().getEmail(),
                        appointment.getPatient().getFullName(),
                        appointment.getDoctor().getUser().getFullName(),
                        appointment.getDoctor().getSpecialty(),
                        appointment.getSlot().getSlotDate().toString(),
                        appointment.getSlot().getStartTime().toString()
                );
            } catch (Exception e) {
                log.error("Failed to process reminder for appointment ID {}: {}",
                        appointment.getId(), e.getMessage());
            }
        }
    }
}
