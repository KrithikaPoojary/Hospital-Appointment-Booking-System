package com.hospital.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing doctor details including
 * their associated user profile information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private String qualification;
    private Integer experienceYears;
    private String bio;
}
