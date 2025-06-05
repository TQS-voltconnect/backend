package pt.ua.tqs.voltconnect.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import pt.ua.tqs.voltconnect.models.*;
import pt.ua.tqs.voltconnect.repositories.*;
import pt.ua.tqs.voltconnect.services.impl.ReservationServiceImpl;
import pt.ua.tqs.voltconnect.services.PaymentService;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Mock
    private PaymentService paymentService;

    private Reservation reservation;
    private ChargingStation station;
    private Charger chargerAC;
    private Charger chargerDC;
    private Vehicle vehicleWithAC;
    private Vehicle vehicleWithDC;
    private Vehicle vehicleWithNoCharger;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .userId(1L)
                .vehicleId("vehicle-uuid")
                .chargingStationId(1L)
                .chargerId(100L)
                .startTime(new Date(System.currentTimeMillis() + 3600_000))
                .build();

        chargerAC = new Charger();
        chargerAC.setId(100L);
        chargerAC.setChargerType(Charger.Type.AC2);
        chargerAC.setChargingSpeed(22.0);
        chargerAC.setPricePerKWh(0.25);

        chargerDC = new Charger();
        chargerDC.setId(101L);
        chargerDC.setChargerType(Charger.Type.DC);
        chargerDC.setChargingSpeed(50.0);
        chargerDC.setPricePerKWh(0.45);

        station = new ChargingStation();
        station.setId(1L);
        station.setChargers(List.of(chargerAC, chargerDC));

        vehicleWithAC = new Vehicle();
        vehicleWithAC.setId("vehicle-ac");
        vehicleWithAC.setUsableBatterySize(42.2);
        vehicleWithAC.setAcChargerJson("{}");
        vehicleWithAC.setDcChargerJson(null);

        vehicleWithDC = new Vehicle();
        vehicleWithDC.setId("vehicle-dc");
        vehicleWithDC.setUsableBatterySize(42.2);
        vehicleWithDC.setAcChargerJson(null);
        vehicleWithDC.setDcChargerJson(
                "{\"charging_curve\":[{\"percentage\":0,\"power\":80.75},{\"percentage\":50,\"power\":85},{\"percentage\":80,\"power\":42.5},{\"percentage\":100,\"power\":17}]}");

        vehicleWithNoCharger = new Vehicle();
        vehicleWithNoCharger.setId("vehicle-none");
        vehicleWithNoCharger.setUsableBatterySize(42.2);
        vehicleWithNoCharger.setAcChargerJson(null);
        vehicleWithNoCharger.setDcChargerJson(null);
    }

    @Test
    void createReservation_VehicleIdNull_Throws() {
        reservation.setVehicleId(null);
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ChargingStationIdNull_Throws() {
        reservation.setChargingStationId(null);
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ChargerIdNull_Throws() {
        reservation.setChargerId(null);
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ValidACCharger_Success() {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));
        when(reservationRepository.findByChargerId(chargerAC.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.createReservation(reservation);

    }

    @Test
    void createReservation_ValidDCChargerWithCurve_Success() {
        reservation.setChargerId(chargerDC.getId());
        reservation.setVehicleId(vehicleWithDC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithDC.getId())).thenReturn(Optional.of(vehicleWithDC));
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));
        when(reservationRepository.findByChargerId(chargerDC.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.createReservation(reservation);
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
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ConflictWithExistingReservation_Throws() {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));

        Reservation existingReservation = Reservation.builder()
                .startTime(new Date(System.currentTimeMillis() + 3000_000))
                .chargingTime(60L)
                .chargerId(chargerAC.getId())
                .build();

        when(reservationRepository.findByChargerId(chargerAC.getId())).thenReturn(List.of(existingReservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
    }

    @Test
    void createReservation_ChargerIsMarkedOccupied() {
        reservation.setChargerId(chargerDC.getId());
        reservation.setVehicleId(vehicleWithDC.getId());
        chargerDC.setChargerStatus(Charger.Status.AVAILABLE);

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithDC.getId())).thenReturn(Optional.of(vehicleWithDC));
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));
        when(reservationRepository.findByChargerId(chargerDC.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.createReservation(reservation);

        assertEquals(Charger.Status.AVAILABLE, chargerDC.getChargerStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'vehicle-invalid-json', 'INVALID_JSON', 'Error parsing DC charger JSON'",
            "'vehicle-invalid-curve', '{\"charging_curve\": []}', 'Invalid or missing charging_curve data'",
            "'vehicle-zero-power', '{\"charging_curve\": [{\"percentage\": 0, \"power\": 0}, {\"percentage\": 100, \"power\": 0}]}', 'Invalid average power in charging curve'"
    })
    void createReservation_InvalidDCChargerData_Throws(String vehicleId, String dcChargerJson, String expectedMessage) {
        reservation.setChargerId(chargerDC.getId());

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setUsableBatterySize(42.2);
        vehicle.setDcChargerJson(dcChargerJson);

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(reservation.getUserId())).thenReturn(Optional.of(new User()));

        reservation.setVehicleId(vehicle.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(reservation));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void getReservationById_ExistingId_ReturnsReservation() {
        Reservation mockReservation = Reservation.builder().id(1L).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(mockReservation));

        Optional<Reservation> result = reservationService.getReservationById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getAllReservations_ReturnsAllReservations() {
        List<Reservation> mockList = List.of(
                Reservation.builder().id(1L).build(),
                Reservation.builder().id(2L).build());

        when(reservationRepository.findAll()).thenReturn(mockList);

        Iterable<Reservation> result = reservationService.getAllReservations();
        assertNotNull(result);
        assertEquals(2, ((Collection<?>) result).size());
    }

    @Test
    void createReservation_FourthTimeDiscountApplied() {
        reservation.setChargerId(chargerAC.getId());
        reservation.setVehicleId(vehicleWithAC.getId());
        User user = new User();
        user.setId(1L);
        user.setStationReservationsCount(new HashMap<>());
        user.getStationReservationsCount().put(station.getId(), 3);

        when(chargingStationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(vehicleRepository.findById(vehicleWithAC.getId())).thenReturn(Optional.of(vehicleWithAC));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reservationRepository.findByChargerId(chargerAC.getId())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(reservation);

        assertTrue(result.isDiscount());
        double expectedOriginalPrice = vehicleWithAC.getUsableBatterySize() * chargerAC.getPricePerKWh();
        assertEquals(expectedOriginalPrice, result.getOriginalPrice());
        double expectedDiscounted = Math.round(expectedOriginalPrice * 0.9 * 100.0) / 100.0;
        assertEquals(expectedDiscounted, result.getPrice());
    }

    @Test
    void cancelReservation_Scheduled_Success() {
        reservation.setStatus(Reservation.ReservationStatus.SCHEDULED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.cancelReservation(1L);

        assertEquals(Reservation.ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelReservation_NonScheduled_Throws() {
        reservation.setStatus(Reservation.ReservationStatus.PAID);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancelReservation(1L));
    }

    @Test
    void startCharging_Valid_Success() {
        reservation.setStatus(Reservation.ReservationStatus.SCHEDULED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.startCharging(1L);
        assertEquals(Reservation.ReservationStatus.CHARGING, result.getStatus());
    }

    @Test
    void stopCharging_Valid_Success() {
        reservation.setStatus(Reservation.ReservationStatus.CHARGING);
        reservation.setPrice(10.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.stopCharging(1L);
        assertEquals(Reservation.ReservationStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getChargingEndTime());
    }

    @Test
    void processPayment_AlreadyPaid_Throws() {
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setIsPaid(true);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.processPayment(1L, "card"));
    }

}
