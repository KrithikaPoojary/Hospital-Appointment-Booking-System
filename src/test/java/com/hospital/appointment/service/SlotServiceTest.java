package com.hospital.appointment.service;

import com.hospital.appointment.dto.SlotDto;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private SlotService slotService;

    private Doctor doctor;
    private Slot slot;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .fullName("Dr. Alice")
                .role(Role.DOCTOR)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .user(user)
                .specialty("Neurology")
                .build();

        slot = Slot.builder()
                .id(10L)
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Should create slot successfully when no overlap exists")
    void testCreateSlotSuccess() {
        SlotDto request = SlotDto.builder()
                .doctorId(1L)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(slotRepository.existsOverlappingSlot(eq(1L), any(), any(), any())).thenReturn(false);
        when(slotRepository.save(any(Slot.class))).thenReturn(slot);

        SlotDto result = slotService.createSlot(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertTrue(result.isAvailable());
        verify(slotRepository, times(1)).save(any(Slot.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when start time is after end time")
    void testCreateSlotInvalidTime() {
        SlotDto request = SlotDto.builder()
                .doctorId(1L)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> slotService.createSlot(request));
        assertTrue(ex.getMessage().contains("Start time must be before end time"));
    }

    @Test
    @DisplayName("Should throw BadRequestException when overlapping slot exists")
    void testCreateSlotOverlapping() {
        SlotDto request = SlotDto.builder()
                .doctorId(1L)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(slotRepository.existsOverlappingSlot(eq(1L), any(), any(), any())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> slotService.createSlot(request));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when deleting a slot that is already booked")
    void testDeleteBookedSlotThrows() {
        slot.setAvailable(false);
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(BadRequestException.class, () -> slotService.deleteSlot(10L));
        verify(slotRepository, never()).delete(any(Slot.class));
    }
}
