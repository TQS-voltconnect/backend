package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.Charger;

import java.util.List;
import java.util.Optional;

public interface ChargerService {
    List<Charger> getAllChargers();

    Optional<Charger> getChargerById(Long id);

    List<Charger> getChargersByStationId(Long stationId);

    Charger saveCharger(Charger charger);

    void deleteCharger(Long id);
}
