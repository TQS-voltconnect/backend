package pt.ua.tqs.voltconnect.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.services.impl.ChargingStationServiceImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChargingStationServiceTest {

    @Mock
    private ChargingStationRepository stationRepository;

    @InjectMocks
    private ChargingStationServiceImpl stationService;

    private ChargingStation station;

    @BeforeEach
    void setUp() {
        station = new ChargingStation();
        station.setId(1L);
    }

    @Test
    void getAllStations_ReturnsList() {
        when(stationRepository.findAll()).thenReturn(List.of(station));
        List<ChargingStation> result = stationService.getAllStations();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findById_ExistingId_ReturnsStation() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        ChargingStation found = stationService.findById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void findById_NotFound_Throws() {
        when(stationRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> stationService.findById(1L));
        assertEquals("Station not found", ex.getMessage());
    }

    @Test
    void getStationsByOperatorId_ReturnsList() {
        when(stationRepository.findByOperatorId(10L)).thenReturn(List.of(station));
        List<ChargingStation> result = stationService.getStationsByOperatorId(10L);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void saveStation_ReturnsSavedStation() {
        when(stationRepository.save(station)).thenReturn(station);
        ChargingStation saved = stationService.saveStation(station);
        assertNotNull(saved);
        assertEquals(station, saved);
    }

    @Test
    void deleteStation_DeletesSuccessfully() {
        Long id = 1L;

        when(stationRepository.existsById(id)).thenReturn(true); // mocka a existência
        doNothing().when(stationRepository).deleteById(id);

        assertDoesNotThrow(() -> stationService.deleteStation(id));

        verify(stationRepository, times(1)).deleteById(id);
    }

}
