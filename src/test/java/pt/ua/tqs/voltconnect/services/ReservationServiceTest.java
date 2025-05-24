package pt.ua.tqs.voltconnect.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import pt.ua.tqs.voltconnect.models.*;
import pt.ua.tqs.voltconnect.repositories.*;
import pt.ua.tqs.voltconnect.services.impl.ReservationServiceImpl;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation;
    private ChargingStation station;
    private Charger chargerAC;
    private Charger chargerDC;
    private Vehicle vehicleWithAC;
    private Vehicle vehicleWithDC;
    private Vehicle vehicleWithNoCharger;

    @BeforeEach
    void setUp() {
        // Reserva padrão com data no futuro
        reservation = Reservation.builder()
                .userId(1L)
                .vehicleId("vehicle-uuid")
                .chargingStationId(1L)
                .chargerId(100L)
                .startTime(new Date(System.currentTimeMillis() + 3600_000))
                .build();

        // Carregador AC
        chargerAC = new Charger();
        chargerAC.setId(100L);
        chargerAC.setChargerType(Charger.Type.AC2);
        chargerAC.setChargingSpeed(22.0);
        chargerAC.setPricePerKWh(0.25);

        // Carregador DC
        chargerDC = new Charger();
        chargerDC.setId(101L);
        chargerDC.setChargerType(Charger.Type.DC);
        chargerDC.setChargingSpeed(50.0);
        chargerDC.setPricePerKWh(0.45);

        // Estação com dois carregadores
        station = new ChargingStation();
        station.setId(1L);
        station.setChargers(List.of(chargerAC, chargerDC));

        // Veículo que aceita AC
        vehicleWithAC = new Vehicle();
        vehicleWithAC.setId("vehicle-ac");
        vehicleWithAC.setUsableBatterySize(42.2);
        vehicleWithAC.setAcChargerJson("{}"); // aceita AC
        vehicleWithAC.setDcChargerJson(null);

        // Veículo que aceita DC com curva
        vehicleWithDC = new Vehicle();
        vehicleWithDC.setId("vehicle-dc");
        vehicleWithDC.setUsableBatterySize(42.2);
        vehicleWithDC.setAcChargerJson(null);
        vehicleWithDC.setDcChargerJson(
                "{\"charging_curve\":[{\"percentage\":0,\"power\":80.75},{\"percentage\":50,\"power\":85},{\"percentage\":80,\"power\":42.5},{\"percentage\":100,\"power\":17}]}");

        // Veículo que não aceita nenhum carregador
        vehicleWithNoCharger = new Vehicle();
        vehicleWithNoCharger.setId("vehicle-none");
        vehicleWithNoCharger.setUsableBatterySize(42.2);
        vehicleWithNoCharger.setAcChargerJson(null);
        vehicleWithNoCharger.setDcChargerJson(null);
    }

    @Test
    void createReservation_ValidACCharger_Success() throws Exception {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation saved = reservationService.createReservation(reservation);

        assertNotNull(saved);
        assertEquals(chargerAC.getId(), saved.getChargerId());
        assertTrue(saved.getChargingTime() > 0);
        assertTrue(saved.getPrice() > 0);
    }

    @Test
    void createReservation_ValidDCChargerWithCurve_Success() throws Exception {
        reservation.setChargerId(chargerDC.getId());
        reservation.setVehicleId(vehicleWithDC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithDC.getId())).thenReturn(Optional.of(vehicleWithDC));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation saved = reservationService.createReservation(reservation);

        assertNotNull(saved);
        assertEquals(chargerDC.getId(), saved.getChargerId());
        assertTrue(saved.getChargingTime() > 0);
        assertTrue(saved.getPrice() > 0);
    }

    @Test
    void createReservation_StartTimeNull_Throws() {
        reservation.setStartTime(null);
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_StartTimeInPast_Throws() {
        reservation.setStartTime(new Date(System.currentTimeMillis() - 1000));
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_StationNotFound_Throws() {
        when(chargingStationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ChargerNotInStation_Throws() {
        station.setChargers(Collections.emptyList());
        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_VehicleNotFound_Throws() {
        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_VehicleNotAcceptChargerType_Throws() {
        reservation.setVehicleId(vehicleWithNoCharger.getId());
        reservation.setChargerId(chargerAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithNoCharger.getId())).thenReturn(Optional.of(vehicleWithNoCharger));

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ConflictWithExistingReservation_Throws() {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));

        // Simula reserva existente que conflita
        Reservation existingReservation = Reservation.builder()
            .startTime(new Date(System.currentTimeMillis() + 3000_000)) // 50 minutos no futuro
            .chargingTime(60L) // 60 minutos
            .chargerId(chargerAC.getId())
            .build();

        when(reservationRepository.findByChargerId(chargerAC.getId())).thenReturn(List.of(existingReservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ChargerIsMarkedOccupied() throws Exception {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));
        when(reservationRepository.findByChargerId(chargerAC.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation saved = reservationService.createReservation(reservation);

        assertNotNull(saved);
        assertEquals(Charger.Status.OCCUPIED, chargerAC.getChargerStatus());
    }

}
