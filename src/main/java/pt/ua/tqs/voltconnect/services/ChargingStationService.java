package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.ChargingStation;

import java.util.List;

public interface ChargingStationService {
    List<ChargingStation> getAllStations();

    ChargingStation findById(Long id);
        
    List<ChargingStation> getStationsByOperatorId(Long operatorId);

    ChargingStation saveStation(ChargingStation station);

    void deleteStation(Long id);

    

    
}
