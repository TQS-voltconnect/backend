package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.List;
import java.util.Optional;

@Service
public class ChargingStationServiceImpl implements ChargingStationService {

    @Autowired
    private ChargingStationRepository stationRepository;

    @Override
    public List<ChargingStation> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public Optional<ChargingStation> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    @Override
    public List<ChargingStation> getStationsByOperatorId(Long operatorId) {
        return stationRepository.findByOperatorId(operatorId);
    }
    
    @Override
    public ChargingStation saveStation(ChargingStation station) {
        return stationRepository.save(station);
    }

    // @Override
    // public ChargingStation saveStation(ChargingStation station) {
    //     for (var charger : station.getChargers()) {
    //             charger.setChargingStation(station);
    //     }
    //     }
    //     return stationRepository.save(station);
    // }    

    @Override
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }
}
