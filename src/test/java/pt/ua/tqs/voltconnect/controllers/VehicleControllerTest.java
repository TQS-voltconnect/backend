package pt.ua.tqs.voltconnect.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;
import pt.ua.tqs.voltconnect.services.VehicleService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleDTO vehicleDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        vehicleDTO = new VehicleDTO();
        vehicleDTO.setId("test-id");
        vehicleDTO.setBrand("Test Brand");
        vehicleDTO.setModel("Test Model");
    }

    @Test
    void importAll_ShouldReturnOk() {
        doNothing().when(vehicleService).importAllVehicles(false);

        ResponseEntity<Void> response = vehicleController.importAll(false);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(vehicleService, times(1)).importAllVehicles(false);
    }

    @Test
    void importAll_WithForce_ShouldReturnOk() {
        doNothing().when(vehicleService).importAllVehicles(true);

        ResponseEntity<Void> response = vehicleController.importAll(true);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(vehicleService, times(1)).importAllVehicles(true);
    }

    @Test
    void getAll_ShouldReturnListOfVehicles() {
        List<VehicleDTO> vehicles = Arrays.asList(vehicleDTO);
        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        ResponseEntity<List<VehicleDTO>> response = vehicleController.getAll();
        List<VehicleDTO> responseBody = response.getBody();

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(vehicleDTO.getId(), responseBody.get(0).getId());
        verify(vehicleService, times(1)).getAllVehicles();
    }

    @Test
    void getByBrand_ShouldReturnListOfVehicles() {
        List<VehicleDTO> vehicles = Arrays.asList(vehicleDTO);
        when(vehicleService.getVehiclesByBrand("Test Brand")).thenReturn(vehicles);

        ResponseEntity<List<VehicleDTO>> response = vehicleController.getByBrand("Test Brand");
        List<VehicleDTO> responseBody = response.getBody();

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(vehicleDTO.getId(), responseBody.get(0).getId());
        verify(vehicleService, times(1)).getVehiclesByBrand("Test Brand");
    }

    @Test
    void getByBrand_WhenNoVehiclesFound_ShouldReturnNotFound() {
        when(vehicleService.getVehiclesByBrand("Test Brand")).thenReturn(Collections.emptyList());
        ResponseEntity<List<VehicleDTO>> response = vehicleController.getByBrand("Test Brand");
        assertTrue(response.getStatusCode().is4xxClientError());
        assertNull(response.getBody());
    }

    @Test
    void getById_WhenVehicleExists_ShouldReturnVehicle() {
        when(vehicleService.getVehicleById("test-id")).thenReturn(Optional.of(vehicleDTO));

        ResponseEntity<VehicleDTO> response = vehicleController.getById("test-id");
        VehicleDTO responseBody = response.getBody();

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(responseBody);
        assertEquals(vehicleDTO.getId(), responseBody.getId());
        verify(vehicleService, times(1)).getVehicleById("test-id");
    }

    @Test
    void getById_WhenVehicleDoesNotExist_ShouldReturnNotFound() {
        when(vehicleService.getVehicleById("non-existent")).thenReturn(Optional.empty());

        ResponseEntity<VehicleDTO> response = vehicleController.getById("non-existent");

        assertTrue(response.getStatusCode().is4xxClientError());
        assertNull(response.getBody());
        verify(vehicleService, times(1)).getVehicleById("non-existent");
    }
} 