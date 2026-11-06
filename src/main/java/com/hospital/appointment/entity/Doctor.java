package com.hospital.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Doctor profile linked 1-to-1 with a User (role=DOCTOR).
 * Stores medical specialty, qualifications, and experience.
 */
@Entity
@Table(name = "doctors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning user account */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String specialty;

    private String qualification;

    private Integer experienceYears;

    /** Short bio shown on the public search page */
    @Column(length = 1000)
    private String bio;
}

