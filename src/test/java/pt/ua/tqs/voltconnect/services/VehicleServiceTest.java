package pt.ua.tqs.voltconnect.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;
import pt.ua.tqs.voltconnect.models.Brand;
import pt.ua.tqs.voltconnect.models.Vehicle;
import pt.ua.tqs.voltconnect.repositories.BrandRepository;
import pt.ua.tqs.voltconnect.repositories.VehicleRepository;
import pt.ua.tqs.voltconnect.services.impl.VehicleServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandService brandService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private ObjectMapper objectMapper;
    private VehicleDTO vehicleDTO;
    private Brand brand;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        vehicleService = new VehicleServiceImpl(vehicleRepository, brandRepository, brandService, restTemplate, objectMapper);

        vehicleDTO = new VehicleDTO();
        vehicleDTO.setId("test-id");
        vehicleDTO.setBrand("Test Brand");
        vehicleDTO.setBrandId("brand-id");
        vehicleDTO.setModel("Test Model");
        vehicleDTO.setReleaseYear(2024);
        vehicleDTO.setUsableBatterySize(50.0);
        vehicleDTO.setVehicleType("BEV");

        brand = new Brand();
        brand.setId("brand-id");
        brand.setName("Test Brand");
        brand.setModelsFile("models/test-brand.json");
    }

    @Test
    void importAllVehicles_ShouldImportVehicles() {
        doNothing().when(brandService).importAllBrands(false);
        when(brandRepository.findAll()).thenReturn(List.of(brand));
        when(restTemplate.getForEntity(anyString(), eq(VehicleDTO[].class)))
                .thenReturn(ResponseEntity.ok(new VehicleDTO[]{vehicleDTO}));
        when(brandRepository.findById(anyString())).thenReturn(Optional.of(brand));

        vehicleService.importAllVehicles(false);

        verify(brandService, times(1)).importAllBrands(false);
        verify(brandRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(VehicleDTO[].class));
    }

    @Test
    void importAllVehicles_WithForce_ShouldImportVehicles() {
        doNothing().when(brandService).importAllBrands(true);
        doNothing().when(vehicleRepository).deleteAll();
        when(brandRepository.findAll()).thenReturn(List.of(brand));
        when(restTemplate.getForEntity(anyString(), eq(VehicleDTO[].class)))
                .thenReturn(ResponseEntity.ok(new VehicleDTO[]{vehicleDTO}));
        when(brandRepository.findById(anyString())).thenReturn(Optional.of(brand));

        vehicleService.importAllVehicles(true);

        verify(brandService, times(1)).importAllBrands(true);
        verify(vehicleRepository, times(1)).deleteAll();
        verify(brandRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(VehicleDTO[].class));
    }

    @Test
    void importVehiclesFromBrand_ShouldImportVehicles() {
        when(restTemplate.getForEntity(anyString(), eq(VehicleDTO[].class)))
                .thenReturn(ResponseEntity.ok(new VehicleDTO[]{vehicleDTO}));
        when(brandRepository.findById(anyString())).thenReturn(Optional.of(brand));

        vehicleService.importVehiclesFromBrand("test-brand");

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(VehicleDTO[].class));
        verify(brandRepository, times(1)).findById(anyString());
        verify(vehicleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getAllVehicles_ShouldReturnListOfVehicles() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("test-id");
        vehicle.setBrand(brand);
        vehicle.setModel("Test Model");

        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));

        List<VehicleDTO> result = vehicleService.getAllVehicles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vehicle.getId(), result.get(0).getId());
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void getVehiclesByBrand_ShouldReturnListOfVehicles() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("test-id");
        vehicle.setBrand(brand);
        vehicle.setModel("Test Model");

        when(vehicleRepository.findByBrand_NameIgnoreCase("Test Brand")).thenReturn(List.of(vehicle));

        List<VehicleDTO> result = vehicleService.getVehiclesByBrand("Test Brand");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vehicle.getId(), result.get(0).getId());
        verify(vehicleRepository, times(1)).findByBrand_NameIgnoreCase("Test Brand");
    }

    @Test
    void getVehicleById_WhenVehicleExists_ShouldReturnVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("test-id");
        vehicle.setBrand(brand);
        vehicle.setModel("Test Model");

        when(vehicleRepository.findById("test-id")).thenReturn(Optional.of(vehicle));

        Optional<VehicleDTO> result = vehicleService.getVehicleById("test-id");

        assertTrue(result.isPresent());
        assertEquals(vehicle.getId(), result.get().getId());
        verify(vehicleRepository, times(1)).findById("test-id");
    }

    @Test
    void getVehicleById_WhenVehicleDoesNotExist_ShouldReturnEmpty() {
        when(vehicleRepository.findById("non-existent")).thenReturn(Optional.empty());

        Optional<VehicleDTO> result = vehicleService.getVehicleById("non-existent");

        assertTrue(result.isEmpty());
        verify(vehicleRepository, times(1)).findById("non-existent");
    }
} 