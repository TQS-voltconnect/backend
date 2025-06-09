package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargerService;
import pt.ua.tqs.voltconnect.services.ChargingStationService;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "charger-controller",
    description = "Handles charger entities, including creation, update, and assignment to stations."
)
@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;
    private final ChargingStationService stationService;

    @Autowired
    public ChargerController(ChargerService chargerService, ChargingStationService stationService) {
        this.chargerService = chargerService;
        this.stationService = stationService;
    }
    
    @Operation(summary = "Get all chargers", description = "Retrieve a list of all chargers in the system.")
    @GetMapping
    public List<Charger> getAllChargers() {
        return chargerService.getAllChargers();
    }

    @Operation(summary = "Get charger by ID", description = "Retrieve details of a specific charger by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Charger> getChargerById(@PathVariable Long id) {
        return chargerService.getChargerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get chargers by station ID", description = "Retrieve all chargers associated with a specific charging station.")
    @GetMapping("/station/{stationId}")
    public List<Charger> getChargersByStation(@PathVariable Long stationId) {
        return chargerService.getChargersByStationId(stationId);
    }

    @Operation(summary = "Create a new charger", description = "Add a new charger to the system.")
    @PostMapping
    public ResponseEntity<Charger> createCharger(@RequestBody Charger charger) {
        Long stationId = charger.getChargingStation().getId();
        ChargingStation station = stationService.findById(stationId);
        charger.setChargingStation(station);
        Charger saved = chargerService.saveCharger(charger);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete a charger", description = "Remove a charger from the system by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable Long id) {
        try {
            chargerService.deleteCharger(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a charger", description = "Update the information of a specific charger by its ID.")
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