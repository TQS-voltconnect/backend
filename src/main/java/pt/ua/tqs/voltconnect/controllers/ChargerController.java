package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.services.ChargerService;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
@CrossOrigin(origins = "*")
public class ChargerController {

    @Autowired
    private ChargerService chargerService;

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
    public Charger createCharger(@RequestBody Charger charger) {
        return chargerService.saveCharger(charger);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable Long id) {
        chargerService.deleteCharger(id);
        return ResponseEntity.noContent().build();
    }
}
