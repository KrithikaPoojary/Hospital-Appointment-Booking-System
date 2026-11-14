package com.hospital.appointment.service;

import com.hospital.appointment.dto.DoctorDto;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing doctor profiles, specialty filters, and searches.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return mapToDto(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile for user", "userId", userId));
        return mapToDto(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> searchDoctors(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorRepository.searchByNameOrSpecialty(keyword.trim()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return doctorRepository.findAllSpecialties();
    }

    @Transactional
    public DoctorDto createOrUpdateProfile(Long userId, DoctorDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User role must be DOCTOR to create doctor profile");
        }

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElse(Doctor.builder().user(user).build());

        doctor.setSpecialty(dto.getSpecialty());
        doctor.setQualification(dto.getQualification());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setBio(dto.getBio());

        Doctor saved = doctorRepository.save(doctor);
        return mapToDto(saved);
    }

    public DoctorDto mapToDto(Doctor doctor) {
        return DoctorDto.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .fullName(doctor.getUser().getFullName())
                .email(doctor.getUser().getEmail())
                .phone(doctor.getUser().getPhone())
                .specialty(doctor.getSpecialty())
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .bio(doctor.getBio())
                .build();
    }
}
