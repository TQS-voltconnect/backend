package pt.ua.tqs.voltconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.Reservation.ReservationStatus;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.services.impl.ReservationMaintenanceServiceImpl;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationMaintenanceServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ReservationMaintenanceServiceImpl maintenanceService;

    private Reservation reservation;
    private Charger charger;

    @BeforeEach
    void setup() {
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setChargerId(100L);
        // start time = 1 hour ago
        reservation.setStartTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        reservation.setChargingTime(15L); // 15 minutes charging
        reservation.setStatus(ReservationStatus.SCHEDULED);

        charger = new Charger();
        charger.setId(100L);
        charger.setChargerStatus(Charger.Status.OCCUPIED);
    }

    @Test
    void testExpireReservationIfEnded() {
        // Change to CHARGING so that end < now triggers COMPLETED
        reservation.setStatus(ReservationStatus.CHARGING);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(chargerRepository.findById(100L)).thenReturn(Optional.of(charger));

        maintenanceService.maintainReservationAndChargerStatus();

        verify(reservationRepository).save(any(Reservation.class));
        verify(chargerRepository).save(any(Charger.class));
    }

    @Test
    void testSetChargerToOccupiedIfInProgress() {
        // start = 5 minutes ago, chargingTime = 30 => end = 25 minutes from now
        reservation.setStartTime(new Date(System.currentTimeMillis() - 5 * 60 * 1000));
        reservation.setChargingTime(30L);
        reservation.setStatus(ReservationStatus.SCHEDULED);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(chargerRepository.findById(100L)).thenReturn(Optional.of(charger));

        maintenanceService.maintainReservationAndChargerStatus();

        verify(chargerRepository).save(any(Charger.class));
    }


    @Test
    void testReservationAlreadyCancelledShouldSkip() {
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(chargerRepository.findById(100L)).thenReturn(Optional.of(charger));

        maintenanceService.maintainReservationAndChargerStatus();

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testReservationAlreadyExpiredShouldSkip() {
        reservation.setStatus(ReservationStatus.EXPIRED);
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(chargerRepository.findById(100L)).thenReturn(Optional.of(charger));

        maintenanceService.maintainReservationAndChargerStatus();

        verify(reservationRepository, never()).save(any());
    }
}
