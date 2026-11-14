package com.hospital.appointment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending transactional email alerts to patients and doctors.
 * Runs asynchronously so email sending never delays API response times.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@hospital.com}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmation(String toEmail, String patientName, String doctorName,
                                        String specialty, String date, String time) {
        String subject = "Appointment Confirmation — Hospital Booking System";
        String body = String.format(
                "Hello %s,\n\n" +
                "Your appointment has been successfully booked!\n\n" +
                "Doctor: Dr. %s (%s)\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "Please arrive 15 minutes prior to your scheduled time.\n\n" +
                "Best regards,\nHospital Management Team",
                patientName, doctorName, specialty, date, time
        );
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendCancellationNotice(String toEmail, String patientName, String doctorName,
                                       String date, String time) {
        String subject = "Appointment Cancelled — Hospital Booking System";
        String body = String.format(
                "Hello %s,\n\n" +
                "Your appointment with Dr. %s on %s at %s has been cancelled.\n\n" +
                "If you need to reschedule, please visit our website.\n\n" +
                "Best regards,\nHospital Management Team",
                patientName, doctorName, date, time
        );
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendRescheduleNotice(String toEmail, String patientName, String doctorName,
                                     String newDate, String newTime) {
        String subject = "Appointment Rescheduled — Hospital Booking System";
        String body = String.format(
                "Hello %s,\n\n" +
                "Your appointment with Dr. %s has been rescheduled.\n\n" +
                "New Date: %s\n" +
                "New Time: %s\n\n" +
                "Best regards,\nHospital Management Team",
                patientName, doctorName, newDate, newTime
        );
        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String patientName, String doctorName,
                                        String specialty, String date, String time) {
        String subject = "Upcoming Appointment Reminder — Tomorrow";
        String body = String.format(
                "Hello %s,\n\n" +
                "This is a friendly reminder of your appointment scheduled for tomorrow:\n\n" +
                "Doctor: Dr. %s (%s)\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "Please notify us in advance if you need to cancel or change your appointment.\n\n" +
                "Best regards,\nHospital Management Team",
                patientName, doctorName, specialty, date, time
        );
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent email notification to: {}", to);
        } catch (Exception e) {
            log.warn("Failed to deliver email to {} (SMTP may not be configured): {}", to, e.getMessage());
        }
    }
}
