package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.dtos.VehicleDTO;

import java.util.List;

public interface VehicleService {
    void importAllVehicles(boolean force);
    void importVehiclesFromBrand(String brandFile);
    List<VehicleDTO> getAllVehicles();
    List<VehicleDTO> getVehiclesByBrand(String brandName);
}