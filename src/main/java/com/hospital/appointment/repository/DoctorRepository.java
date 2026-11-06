package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for the doctors table.
 * Supports specialty search and lookup by user account.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /** Find all doctors whose specialty matches (case-insensitive) */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

    /** Partial match on specialty (for search bar) */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialty) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> searchBySpecialty(@Param("keyword") String keyword);

    /** Search by doctor name or specialty */
    @Query("SELECT d FROM Doctor d WHERE " +
           "LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.specialty) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> searchByNameOrSpecialty(@Param("keyword") String keyword);

    Optional<Doctor> findByUserId(Long userId);

    /** Get all distinct specialties for the filter dropdown */
    @Query("SELECT DISTINCT d.specialty FROM Doctor d ORDER BY d.specialty")
    List<String> findAllSpecialties();
}
