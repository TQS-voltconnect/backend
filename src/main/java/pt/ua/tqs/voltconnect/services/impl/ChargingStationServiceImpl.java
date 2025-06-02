package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.List;

@Service
public class ChargingStationServiceImpl implements ChargingStationService {

    @Autowired
    private ChargingStationRepository stationRepository;

    @Override
    public List<ChargingStation> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public ChargingStation findById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
    }

    @Override
    public List<ChargingStation> getStationsByOperatorId(Long operatorId) {
        return stationRepository.findByOperatorId(operatorId);
    }

    @Override
    public ChargingStation saveStation(ChargingStation station) {
        return stationRepository.save(station);
    }


    @Override
    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ChargingStationNotFoundException(id);
        }
        stationRepository.deleteById(id);
    }

    private static class ChargingStationNotFoundException extends RuntimeException {
        public ChargingStationNotFoundException(Long id) {
            super("Charging Station with ID " + id + " not found.");
        }
    }

}
