package com.hospital.appointment.service;

import com.hospital.appointment.dto.SlotDto;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing doctor appointment slots and availability.
 * Enforces start/end time validation and overlap prevention.
 */
@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public SlotDto createSlot(SlotDto request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        boolean overlaps = slotRepository.existsOverlappingSlot(
                doctor.getId(),
                request.getSlotDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (overlaps) {
            throw new BadRequestException("A slot overlapping with this time already exists for this doctor on " + request.getSlotDate());
        }

        Slot slot = Slot.builder()
                .doctor(doctor)
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .available(true)
                .build();

        Slot saved = slotRepository.save(slot);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndSlotDateAndAvailableTrue(doctorId, date).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAllAvailableFutureSlots(Long doctorId) {
        return slotRepository.findAvailableSlots(doctorId, LocalDate.now()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getDoctorSlotsByDate(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", slotId));

        if (!slot.isAvailable()) {
            throw new BadRequestException("Cannot delete a slot that has already been booked");
        }

        slotRepository.delete(slot);
    }

    public SlotDto mapToDto(Slot slot) {
        return SlotDto.builder()
                .id(slot.getId())
                .doctorId(slot.getDoctor().getId())
                .doctorName(slot.getDoctor().getUser().getFullName())
                .specialty(slot.getDoctor().getSpecialty())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .build();
    }
}
