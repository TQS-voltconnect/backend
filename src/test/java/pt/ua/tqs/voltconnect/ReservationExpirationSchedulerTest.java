package pt.ua.tqs.voltconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.Reservation.ReservationStatus;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.scheduling.ReservationExpirationScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservationExpirationSchedulerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckExpiredReservations_ExpiredReservationIsUpdated() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.SCHEDULED)
                .startTime(java.util.Date.from(Instant.now().minus(20, ChronoUnit.MINUTES)))
                .build();

        when(reservationRepository.findByStatus(ReservationStatus.SCHEDULED))
                .thenReturn(List.of(reservation));

        scheduler.checkExpiredReservations();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void testCheckExpiredReservations_NotExpiredReservationIsNotUpdated() {
        Reservation reservation = Reservation.builder()
                .id(2L)
                .status(ReservationStatus.SCHEDULED)
                .startTime(java.util.Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                .build();

        when(reservationRepository.findByStatus(ReservationStatus.SCHEDULED))
                .thenReturn(List.of(reservation));

        scheduler.checkExpiredReservations();

        assertEquals(ReservationStatus.SCHEDULED, reservation.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCheckExpiredReservations_EmptyList() {
        when(reservationRepository.findByStatus(ReservationStatus.SCHEDULED))
                .thenReturn(Collections.emptyList());

        scheduler.checkExpiredReservations();

        verify(reservationRepository, never()).save(any());
    }
}
