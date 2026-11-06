package com.hospital.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Hospital Appointment Booking System.
 * @EnableScheduling enables cron-based email reminders.
 */
@SpringBootApplication
@EnableScheduling
public class HospitalAppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalAppointmentApplication.class, args);
    }
}

