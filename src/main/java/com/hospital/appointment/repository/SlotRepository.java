package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data access for the slots table.
 * Queries support filtering by doctor, date, and availability.
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    /** All slots for a doctor on a given date */
    List<Slot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate date);

    /** Only available (bookable) slots for a doctor on a date */
    List<Slot> findByDoctorIdAndSlotDateAndAvailableTrue(Long doctorId, LocalDate date);

    /** All future available slots for a doctor */
    @Query("SELECT s FROM Slot s WHERE s.doctor.id = :doctorId AND s.slotDate >= :today AND s.available = true ORDER BY s.slotDate, s.startTime")
    List<Slot> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);

    /** Check for overlapping slots (used to prevent duplicate slot creation) */
    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.doctor.id = :doctorId AND s.slotDate = :date " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingSlot(@Param("doctorId") Long doctorId,
                                   @Param("date") LocalDate date,
                                   @Param("startTime") java.time.LocalTime startTime,
                                   @Param("endTime") java.time.LocalTime endTime);
}
