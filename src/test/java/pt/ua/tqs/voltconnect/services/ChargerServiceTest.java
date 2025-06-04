package pt.ua.tqs.voltconnect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.services.impl.ChargerServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {

    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ChargerServiceImpl chargerService;

    private Charger charger1;
    private Charger charger2;
    private ChargingStation station;

    @BeforeEach
    void setUp() {
        station = new ChargingStation();
        station.setId(100L);

        charger1 = new Charger();
        charger1.setId(1L);
        charger1.setChargerType(Charger.Type.AC1);
        charger1.setChargingStation(station);

        charger2 = new Charger();
        charger2.setId(2L);
        charger2.setChargerType(Charger.Type.DC);
        charger2.setChargingStation(station);
    }

    @Test
    void getAllChargers_ReturnsList() {
        when(chargerRepository.findAll()).thenReturn(Arrays.asList(charger1, charger2));

        List<Charger> result = chargerService.getAllChargers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(charger1.getId(), result.get(0).getId());
        assertEquals(charger2.getId(), result.get(1).getId());
        verify(chargerRepository, times(1)).findAll();
    }

    @Test
    void getChargerById_ExistingId_ReturnsCharger() {
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger1));

        Optional<Charger> result = chargerService.getChargerById(1L);

        assertTrue(result.isPresent());
        assertEquals(charger1.getId(), result.get().getId());
        verify(chargerRepository, times(1)).findById(1L);
    }

    @Test
    void getChargerById_NonExistingId_ReturnsEmpty() {
        when(chargerRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Charger> result = chargerService.getChargerById(999L);

        assertTrue(result.isEmpty());
        verify(chargerRepository, times(1)).findById(999L);
    }

    @Test
    void getChargersByStationId_ReturnsList() {
        when(chargerRepository.findByChargingStationId(100L)).thenReturn(Arrays.asList(charger1, charger2));

        List<Charger> result = chargerService.getChargersByStationId(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(station, result.get(0).getChargingStation());
        assertEquals(station, result.get(1).getChargingStation());
        verify(chargerRepository, times(1)).findByChargingStationId(100L);
    }

    @Test
    void saveCharger_AC1Type_SetsCorrectPriceAndSpeed() {
        Charger charger = new Charger();
        charger.setChargerType(Charger.Type.AC1);

        when(chargerRepository.save(any(Charger.class))).thenReturn(charger);

        Charger result = chargerService.saveCharger(charger);

        assertEquals(0.15, result.getPricePerKWh());
        assertEquals(3.7, result.getChargingSpeed());
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    void saveCharger_AC2Type_SetsCorrectPriceAndSpeed() {
        Charger charger = new Charger();
        charger.setChargerType(Charger.Type.AC2);

        when(chargerRepository.save(any(Charger.class))).thenReturn(charger);

        Charger result = chargerService.saveCharger(charger);

        assertEquals(0.25, result.getPricePerKWh());
        assertEquals(22.0, result.getChargingSpeed());
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    void saveCharger_DCType_SetsCorrectPriceAndSpeed() {
        Charger charger = new Charger();
        charger.setChargerType(Charger.Type.DC);

        when(chargerRepository.save(any(Charger.class))).thenReturn(charger);

        Charger result = chargerService.saveCharger(charger);

        assertEquals(0.45, result.getPricePerKWh());
        assertEquals(50.0, result.getChargingSpeed());
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    void saveCharger_NullType_DoesNotSetPriceAndSpeed() {
        Charger charger = new Charger();
        charger.setChargerType(null);

        when(chargerRepository.save(any(Charger.class))).thenReturn(charger);

        Charger result = chargerService.saveCharger(charger);

        assertNull(result.getPricePerKWh());
        assertNull(result.getChargingSpeed());
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    void deleteCharger_DeletesSuccessfully() {
        Long id = 1L;
        Charger charger = new Charger();
        charger.setId(id);

        when(chargerRepository.findById(id)).thenReturn(Optional.of(charger));
        doNothing().when(chargerRepository).delete(charger);

        assertDoesNotThrow(() -> chargerService.deleteCharger(id));
        verify(chargerRepository).delete(charger);
    }

    @Test
    void deleteCharger_WithStation_RemovesFromStationAndDeletes() {
        Long chargerId = 1L;

        ChargingStation stationMock = mock(ChargingStation.class);
        Charger chargerMock = mock(Charger.class);

        when(chargerMock.getChargingStation()).thenReturn(stationMock);
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(chargerMock));

        chargerService.deleteCharger(chargerId);

        verify(stationMock, times(1)).removeCharger(chargerMock);
        verify(chargerRepository, times(1)).delete(chargerMock);
    }

    @Test
    void deleteCharger_NoStation_DeletesOnly() {
        Long chargerId = 2L;

        Charger charger = mock(Charger.class);
        when(charger.getChargingStation()).thenReturn(null);
        when(chargerRepository.findById(chargerId)).thenReturn(Optional.of(charger));

        chargerService.deleteCharger(chargerId);

        verify(chargerRepository, times(1)).delete(charger);
    }

}