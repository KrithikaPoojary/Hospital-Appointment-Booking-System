package com.hospital.appointment.service;

import com.hospital.appointment.dto.AppointmentDto;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.RescheduleRequest;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.SlotRepository;
import com.hospital.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing appointment lifecycles: booking, cancellation,
 * rescheduling, and query retrieval. Enforces double-booking prevention.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public synchronized AppointmentDto bookAppointment(Long patientUserId, AppointmentRequest request) {
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientUserId));

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", request.getSlotId()));

        if (!slot.isAvailable()) {
            throw new BadRequestException("This slot has already been booked. Please choose another slot.");
        }

        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book an appointment for a past date.");
        }

        boolean alreadyBooked = appointmentRepository.existsByPatientIdAndSlotIdAndStatus(
                patient.getId(), slot.getId(), AppointmentStatus.BOOKED);
        if (alreadyBooked) {
            throw new BadRequestException("You have already booked this slot.");
        }

        // Reserve the slot to prevent race condition double-booking
        slot.setAvailable(false);
        slotRepository.save(slot);

        Doctor doctor = slot.getDoctor();

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .status(AppointmentStatus.BOOKED)
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Send confirmation email
        emailService.sendBookingConfirmation(
                patient.getEmail(),
                patient.getFullName(),
                doctor.getUser().getFullName(),
                doctor.getSpecialty(),
                slot.getSlotDate().toString(),
                slot.getStartTime().toString()
        );

        return mapToDto(saved);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long appointmentId, Long requestedByUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.BOOKED && appointment.getStatus() != AppointmentStatus.RESCHEDULED) {
            throw new BadRequestException("Only active appointments can be cancelled.");
        }

        User user = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestedByUserId));

        boolean isPatient = appointment.getPatient().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isPatient && !isDoctor && !isAdmin) {
            throw new BadRequestException("You do not have permission to cancel this appointment.");
        }

        // Release the slot back for other patients
        Slot slot = appointment.getSlot();
        slot.setAvailable(true);
        slotRepository.save(slot);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);

        emailService.sendCancellationNotice(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getUser().getFullName(),
                slot.getSlotDate().toString(),
                slot.getStartTime().toString()
        );

        return mapToDto(updated);
    }

    @Transactional
    public synchronized AppointmentDto rescheduleAppointment(Long appointmentId, Long patientUserId, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (!appointment.getPatient().getId().equals(patientUserId)) {
            throw new BadRequestException("You can only reschedule your own appointments.");
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED && appointment.getStatus() != AppointmentStatus.RESCHEDULED) {
            throw new BadRequestException("Only active booked appointments can be rescheduled.");
        }

        Slot newSlot = slotRepository.findById(request.getNewSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", request.getNewSlotId()));

        if (!newSlot.isAvailable()) {
            throw new BadRequestException("The selected new slot is not available.");
        }

        if (!newSlot.getDoctor().getId().equals(appointment.getDoctor().getId())) {
            throw new BadRequestException("Rescheduled slot must be with the same doctor.");
        }

        // Release old slot
        Slot oldSlot = appointment.getSlot();
        oldSlot.setAvailable(true);
        slotRepository.save(oldSlot);

        // Reserve new slot
        newSlot.setAvailable(false);
        slotRepository.save(newSlot);

        appointment.setSlot(newSlot);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        if (request.getReason() != null && !request.getReason().isBlank()) {
            appointment.setNotes(request.getReason());
        }

        Appointment updated = appointmentRepository.save(appointment);

        emailService.sendRescheduleNotice(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getUser().getFullName(),
                newSlot.getSlotDate().toString(),
                newSlot.getStartTime().toString()
        );

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AppointmentDto mapToDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .patientEmail(appointment.getPatient().getEmail())
                .patientPhone(appointment.getPatient().getPhone())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .specialty(appointment.getDoctor().getSpecialty())
                .slotId(appointment.getSlot().getId())
                .appointmentDate(appointment.getSlot().getSlotDate())
                .startTime(appointment.getSlot().getStartTime())
                .endTime(appointment.getSlot().getEndTime())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
