package pt.ua.tqs.voltconnect.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;
import pt.ua.tqs.voltconnect.dtos.VehicleMapper;
import pt.ua.tqs.voltconnect.models.Brand;
import pt.ua.tqs.voltconnect.models.Vehicle;
import pt.ua.tqs.voltconnect.repositories.BrandRepository;
import pt.ua.tqs.voltconnect.repositories.VehicleRepository;
import pt.ua.tqs.voltconnect.services.BrandService;
import pt.ua.tqs.voltconnect.services.VehicleService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BrandRepository brandRepository;
    private final BrandService brandService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Override
    public void importAllVehicles(boolean force) {
        brandService.importAllBrands(force);

        if (force) {
            vehicleRepository.deleteAll();
        }

        List<Brand> brands = brandRepository.findAll();

        for (Brand brand : brands) {
            String brandFile = brand.getModelsFile()
                    .replace("models/", "")
                    .replace(".json", "");
            importVehiclesFromBrand(brandFile);
        }
    }

    @Override
    public void importVehiclesFromBrand(String brandFile) {
        String url = baseUrl + "/vehicles/" + brandFile.toLowerCase();
        ResponseEntity<VehicleDTO[]> response = restTemplate.getForEntity(url, VehicleDTO[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) return;

        List<VehicleDTO> dtos = Arrays.asList(response.getBody());
        if (dtos.isEmpty()) return;

        VehicleDTO sample = dtos.getFirst();
        Brand brand = brandRepository.findById(sample.getBrandId())
                .orElseGet(() -> {
                    Brand newBrand = Brand.builder()
                            .id(sample.getBrandId())
                            .name(sample.getBrand())
                            .modelsFile("models/" + brandFile.toLowerCase() + ".json")
                            .build();
                    return brandRepository.save(newBrand);
                });

        List<Vehicle> vehicles = dtos.stream().map(dto -> {
            Vehicle existing = vehicleRepository.findById(dto.getId()).orElse(null);

            try {
                Vehicle vehicle = (existing != null) ? existing : new Vehicle();
                vehicle.setId(dto.getId());
                vehicle.setBrand(brand);
                vehicle.setModel(dto.getModel());
                vehicle.setReleaseYear(dto.getReleaseYear());
                vehicle.setVariant(dto.getVariant());
                vehicle.setVehicleType(dto.getVehicleType());
                vehicle.setUsableBatterySize(dto.getUsableBatterySize());
                vehicle.setChargingVoltage(dto.getChargingVoltage());
                vehicle.setAcChargerJson(objectMapper.writeValueAsString(dto.getAcCharger()));
                vehicle.setDcChargerJson(objectMapper.writeValueAsString(dto.getDcCharger()));
                vehicle.setEnergyConsumptionJson(objectMapper.writeValueAsString(dto.getEnergyConsumption()));
                vehicle.setImageUrl(dto.getImageUrl());

                if (dto.getImageUpdatedAt() != null) {
                    vehicle.setImageUpdatedAt(LocalDateTime.parse(dto.getImageUpdatedAt()));
                }

                return vehicle;

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize charger or energy data", e);
            }
        }).toList();

        vehicleRepository.saveAll(vehicles);
    }

    @Override
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleDTO> getVehiclesByBrand(String brandName) {
        return vehicleRepository.findByBrand_NameIgnoreCase(brandName).stream()
                .map(VehicleMapper::toDTO)
                .collect(Collectors.toList());
    }
}