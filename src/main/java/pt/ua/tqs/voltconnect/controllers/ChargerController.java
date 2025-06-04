package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargerService;
import pt.ua.tqs.voltconnect.services.ChargingStationService;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;
    private final ChargingStationService stationService;
    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository chargingStationRepository;

    @Autowired
    public ChargerController(ChargerService chargerService, ChargingStationService stationService,
            ChargerRepository chargerRepository, ChargingStationRepository chargingStationRepository) {
        this.chargerService = chargerService;
        this.stationService = stationService;
        this.chargerRepository = chargerRepository;
        this.chargingStationRepository = chargingStationRepository;
    }

    @GetMapping
    public List<Charger> getAllChargers() {
        return chargerService.getAllChargers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charger> getChargerById(@PathVariable Long id) {
        return chargerService.getChargerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/station/{stationId}")
    public List<Charger> getChargersByStation(@PathVariable Long stationId) {
        return chargerService.getChargersByStationId(stationId);
    }

    @PostMapping
    public ResponseEntity<Charger> createCharger(@RequestBody Charger charger) {
        Long stationId = charger.getChargingStation().getId();
        ChargingStation station = stationService.findById(stationId);
        charger.setChargingStation(station);
        Charger saved = chargerService.saveCharger(charger);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable Long id) {
        Optional<Charger> optionalCharger = chargerRepository.findById(id);
        if (optionalCharger.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Charger charger = optionalCharger.get();
        ChargingStation station = charger.getChargingStation();

        if (station != null) {
            station.removeCharger(charger); // remove da lista e quebra ligação
            chargingStationRepository.save(station);
        }

        chargerRepository.delete(charger); // agora sim, pode apagar

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charger> updateCharger(@PathVariable Long id, @RequestBody Charger updatedCharger) {
        return chargerService.getChargerById(id).map(existing -> {
            existing.setChargerType(updatedCharger.getChargerType());
            existing.setChargerStatus(updatedCharger.getChargerStatus());
            existing.setChargingSpeed(updatedCharger.getChargingSpeed());
            existing.setPricePerKWh(updatedCharger.getPricePerKWh());
            Charger saved = chargerService.saveCharger(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

}