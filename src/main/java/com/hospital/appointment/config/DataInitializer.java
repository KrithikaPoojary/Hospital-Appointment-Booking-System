package com.hospital.appointment.config;

import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;
import com.hospital.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Initializes database with default seed accounts and sample doctors
 * on initial launch if the database is empty.
 * Runs only on non-test environments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data setup.");
            return;
        }

        log.info("Empty database detected. Seeding demo accounts and doctors...");

        // 1. Seed Admin
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .email("admin@hospital.com")
                .phone("9998887770")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // 2. Seed Patient
        User patient = User.builder()
                .username("patient")
                .password(passwordEncoder.encode("patient123"))
                .fullName("Jane Doe")
                .email("patient@example.com")
                .phone("9876543210")
                .role(Role.PATIENT)
                .build();
        userRepository.save(patient);

        // 3. Seed Doctors
        seedDoctor("dr.smith", "doctor123", "John Smith", "smith@hospital.com",
                "1112223330", "Cardiology", "MD, FACC", 14,
                "Senior Cardiologist specializing in preventative cardiology and heart failure management.");

        seedDoctor("dr.sarah", "doctor123", "Sarah Johnson", "sarah@hospital.com",
                "2223334440", "Neurology", "MD, PhD (Neurology)", 10,
                "Consultant Neurologist focusing on neurodegenerative diseases, stroke recovery, and epilepsy care.");

        seedDoctor("dr.raj", "doctor123", "Raj Patel", "raj@hospital.com",
                "3334445550", "Pediatrics", "MBBS, MD (Pediatrics)", 16,
                "Experienced Pediatrician dedicated to infant wellness, developmental pediatrics, and adolescent medicine.");

        seedDoctor("dr.emily", "doctor123", "Emily Brown", "emily@hospital.com",
                "4445556660", "Dermatology", "MD (Dermatology)", 8,
                "Dermatology specialist offering medical and aesthetic skincare treatments for complex skin conditions.");

        log.info("Demo accounts and doctor slots successfully seeded.");
    }

    private void seedDoctor(String username, String password, String fullName, String email,
                            String phone, String specialty, String qualification,
                            int experienceYears, String bio) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .role(Role.DOCTOR)
                .build();
        User savedUser = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(savedUser)
                .specialty(specialty)
                .qualification(qualification)
                .experienceYears(experienceYears)
                .bio(bio)
                .build();
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Seed slots for the upcoming 5 days
        LocalDate today = LocalDate.now();
        List<LocalTime[]> slotTimes = List.of(
                new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(9, 30)},
                new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(10, 30)},
                new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(11, 30)},
                new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(14, 30)},
                new LocalTime[]{LocalTime.of(15, 0), LocalTime.of(15, 30)}
        );

        for (int day = 1; day <= 4; day++) {
            LocalDate date = today.plusDays(day);
            for (LocalTime[] timeRange : slotTimes) {
                Slot slot = Slot.builder()
                        .doctor(savedDoctor)
                        .slotDate(date)
                        .startTime(timeRange[0])
                        .endTime(timeRange[1])
                        .available(true)
                        .build();
                slotRepository.save(slot);
            }
        }
    }
}
