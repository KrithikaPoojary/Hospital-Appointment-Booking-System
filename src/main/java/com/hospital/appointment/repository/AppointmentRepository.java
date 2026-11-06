package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access for the appointments table.
 * Supports queries for patients, doctors, and admin reports.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** All appointments for a patient */
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /** All appointments for a doctor */
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    /** Filter by status */
    List<Appointment> findByStatus(AppointmentStatus status);

    /** Appointments for a doctor on a specific date */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.slot.slotDate = :date")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    /** Count appointments by status (for admin dashboard) */
    long countByStatus(AppointmentStatus status);

    /** Upcoming appointments needing email reminders (booked, tomorrow) */
    @Query("SELECT a FROM Appointment a WHERE a.status = 'BOOKED' AND a.slot.slotDate = :tomorrow")
    List<Appointment> findUpcomingForReminder(@Param("tomorrow") LocalDate tomorrow);

    /** Admin report: appointments between two dates */
    @Query("SELECT a FROM Appointment a WHERE a.slot.slotDate BETWEEN :start AND :end ORDER BY a.slot.slotDate")
    List<Appointment> findBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** Check if patient already has a booked appointment in same slot */
    boolean existsByPatientIdAndSlotIdAndStatus(Long patientId, Long slotId, AppointmentStatus status);
}
