package pt.ua.tqs.voltconnect.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ua.tqs.voltconnect.models.Vehicle;

public class VehicleMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static VehicleDTO toDTO(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand().getName());
        dto.setBrandId(vehicle.getBrand().getId());
        dto.setModel(vehicle.getModel());
        dto.setReleaseYear(vehicle.getReleaseYear());
        dto.setVariant(vehicle.getVariant());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setUsableBatterySize(vehicle.getUsableBatterySize());
        dto.setChargingVoltage(vehicle.getChargingVoltage());
        dto.setImageUrl(vehicle.getImageUrl());
        dto.setImageUpdatedAt(vehicle.getImageUpdatedAt() != null
                ? vehicle.getImageUpdatedAt().toString()
                : null);

        try {
            if (vehicle.getAcChargerJson() != null) {
                dto.setAcCharger(objectMapper.readValue(vehicle.getAcChargerJson(), ChargerSpec.class));
            }
            if (vehicle.getDcChargerJson() != null) {
                dto.setDcCharger(objectMapper.readValue(vehicle.getDcChargerJson(), DcChargerSpec.class));
            }
            if (vehicle.getEnergyConsumptionJson() != null) {
                dto.setEnergyConsumption(objectMapper.readValue(vehicle.getEnergyConsumptionJson(), EnergyConsumption.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting vehicle to DTO", e);
        }

        return dto;
    }
}
