package pt.ua.tqs.voltconnect.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargerService;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChargerControllerTest {

    @Mock
    private ChargerService chargerService;

    @Mock
    private ChargingStationService stationService;

    @InjectMocks
    private ChargerController chargerController;

    private Charger charger;
    private ChargingStation station;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        station = new ChargingStation();
        station.setId(1L);
        station.setCity("Test City");
        station.setLocation(Arrays.asList(41.1579f, -8.6291f));
        station.setOperatorId(1L);

        charger = new Charger();
        charger.setId(1L);
        charger.setChargingStation(station);
    }

    @Test
    void getAllChargers_ShouldReturnListOfChargers() {
        List<Charger> chargers = Arrays.asList(charger);
        when(chargerService.getAllChargers()).thenReturn(chargers);

        List<Charger> result = chargerController.getAllChargers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chargerService, times(1)).getAllChargers();
    }

    @Test
    void getChargerById_WhenChargerExists_ShouldReturnCharger() {
        when(chargerService.getChargerById(1L)).thenReturn(Optional.of(charger));

        ResponseEntity<Charger> response = chargerController.getChargerById(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(charger.getId(), response.getBody().getId());
        verify(chargerService, times(1)).getChargerById(1L);
    }

    @Test
    void getChargerById_WhenChargerDoesNotExist_ShouldReturnNotFound() {
        when(chargerService.getChargerById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Charger> response = chargerController.getChargerById(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertNull(response.getBody());
        verify(chargerService, times(1)).getChargerById(1L);
    }

    @Test
    void getChargersByStation_ShouldReturnListOfChargers() {
        List<Charger> chargers = Arrays.asList(charger);
        when(chargerService.getChargersByStationId(1L)).thenReturn(chargers);

        List<Charger> result = chargerController.getChargersByStation(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chargerService, times(1)).getChargersByStationId(1L);
    }

    @Test
    void createCharger_ShouldReturnCreatedCharger() {
        when(stationService.findById(1L)).thenReturn(station);
        when(chargerService.saveCharger(any(Charger.class))).thenReturn(charger);

        ResponseEntity<Charger> response = chargerController.createCharger(charger);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(charger.getId(), response.getBody().getId());
        verify(stationService, times(1)).findById(1L);
        verify(chargerService, times(1)).saveCharger(any(Charger.class));
    }

    @Test
    void deleteCharger_ShouldReturnNoContent() {
        doNothing().when(chargerService).deleteCharger(1L);

        ResponseEntity<Void> response = chargerController.deleteCharger(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(chargerService, times(1)).deleteCharger(1L);
    }
} 