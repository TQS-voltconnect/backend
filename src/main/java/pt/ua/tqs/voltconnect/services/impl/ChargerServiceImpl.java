package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.services.ChargerService;
import pt.ua.tqs.voltconnect.models.ChargingStation;

import java.util.List;
import java.util.Optional;

@Service
public class ChargerServiceImpl implements ChargerService {

    @Autowired
    private ChargerRepository chargerRepository;

    @Override
    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    @Override
    public Optional<Charger> getChargerById(Long id) {
        return chargerRepository.findById(id);
    }

    @Override
    public List<Charger> getChargersByStationId(Long stationId) {
        return chargerRepository.findByChargingStationId(stationId);
    }

    @Override
    public Charger saveCharger(Charger charger) {
        if (charger.getPricePerKWh() == null || charger.getPricePerKWh() <= 0) {
            throw new IllegalArgumentException("Price per kWh must be a positive value");
        }

        if (charger.getChargingSpeed() == null || charger.getChargingSpeed() <= 0) {
            throw new IllegalArgumentException("Charging speed must be a positive value");
        }

        return chargerRepository.save(charger);
    }

    @Override
    public void deleteCharger(Long id) {
        Charger charger = chargerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        ChargingStation station = charger.getChargingStation();
        if (station != null) {
            station.removeCharger(charger);
        }

        chargerRepository.delete(charger);
    }

}