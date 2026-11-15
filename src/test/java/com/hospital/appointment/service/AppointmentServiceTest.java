package com.hospital.appointment.service;

import com.hospital.appointment.dto.AppointmentDto;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.RescheduleRequest;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.Role;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.SlotRepository;
import com.hospital.appointment.repository.UserRepository;
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
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patient;
    private User doctorUser;
    private Doctor doctor;
    private Slot slot;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(2L)
                .username("patient1")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .role(Role.PATIENT)
                .build();

        doctorUser = User.builder()
                .id(3L)
                .username("drsmith")
                .fullName("Dr. Smith")
                .email("smith@example.com")
                .role(Role.DOCTOR)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialty("Cardiology")
                .build();

        slot = Slot.builder()
                .id(100L)
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .available(true)
                .build();

        appointment = Appointment.builder()
                .id(50L)
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .status(AppointmentStatus.BOOKED)
                .notes("Checkup")
                .build();
    }

    @Test
    @DisplayName("Should book slot successfully and flip slot availability to false (Double-booking prevention)")
    void testBookAppointmentSuccess() {
        AppointmentRequest req = new AppointmentRequest(100L, "Routine heart checkup");

        when(userRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByPatientIdAndSlotIdAndStatus(2L, 100L, AppointmentStatus.BOOKED)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.bookAppointment(2L, req);

        assertNotNull(result);
        assertEquals(AppointmentStatus.BOOKED, result.getStatus());
        assertFalse(slot.isAvailable(), "Slot must be marked unavailable to prevent double booking");
        verify(slotRepository, times(1)).save(slot);
        verify(emailService, times(1)).sendBookingConfirmation(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw BadRequestException if slot is already booked (Double-booking guard)")
    void testBookAppointmentSlotUnavailable() {
        slot.setAvailable(false);
        AppointmentRequest req = new AppointmentRequest(100L, "Routine checkup");

        when(userRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> appointmentService.bookAppointment(2L, req));
        assertTrue(ex.getMessage().contains("already been booked"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should cancel appointment and restore slot availability to true")
    void testCancelAppointmentSuccess() {
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto cancelled = appointmentService.cancelAppointment(50L, 2L);

        assertTrue(slot.isAvailable(), "Slot must be released back to available upon cancellation");
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(slotRepository, times(1)).save(slot);
        verify(emailService, times(1)).sendCancellationNotice(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reschedule appointment by releasing old slot and taking new slot")
    void testRescheduleAppointmentSuccess() {
        Slot newSlot = Slot.builder()
                .id(101L)
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .available(true)
                .build();

        RescheduleRequest req = new RescheduleRequest(101L, "Time conflict");

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));
        when(slotRepository.findById(101L)).thenReturn(Optional.of(newSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentDto result = appointmentService.rescheduleAppointment(50L, 2L, req);

        assertTrue(slot.isAvailable(), "Old slot must be freed");
        assertFalse(newSlot.isAvailable(), "New slot must be reserved");
        assertEquals(AppointmentStatus.RESCHEDULED, appointment.getStatus());
        verify(emailService, times(1)).sendRescheduleNotice(any(), any(), any(), any(), any());
    }
}
