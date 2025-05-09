package pt.ua.tqs.voltconnect.services.impl;
import pt.ua.tqs.voltconnect.services.ChargerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;

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
        return chargerRepository.save(charger);
    }

    @Override
    public void deleteCharger(Long id) {
        chargerRepository.deleteById(id);
    }
}
