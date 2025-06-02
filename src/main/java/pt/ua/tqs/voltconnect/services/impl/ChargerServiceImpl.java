package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.services.ChargerService;

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
        // Atribui automaticamente o preço e velocidade de carregamento com base no tipo
        if (charger.getChargerType() != null) {
            switch (charger.getChargerType()) {
                case AC1 -> {
                    charger.setPricePerKWh(0.15);
                    charger.setChargingSpeed(3.7);
                }
                case AC2 -> {
                    charger.setPricePerKWh(0.25);
                    charger.setChargingSpeed(22.0);
                }
                case DC -> {
                    charger.setPricePerKWh(0.45);
                    charger.setChargingSpeed(50.0);
                }
            }
        }

        // charger.setChargingStation(chargingStation);

        return chargerRepository.save(charger);
    }

    @Override
    public void deleteCharger(Long id) {
        chargerRepository.deleteById(id);
    }
}
