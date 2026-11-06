package com.hospital.appointment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A single bookable time slot offered by a doctor.
 * The "available" flag flips to false once a patient books it,
 * preventing double-booking at the database level.
 */
@Entity
@Table(name = "slots",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"doctor_id", "slot_date", "start_time"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** true = bookable, false = already taken */
    @Column(nullable = false)
    private boolean available = true;
}

