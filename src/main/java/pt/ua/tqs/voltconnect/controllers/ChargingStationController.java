package pt.ua.tqs.voltconnect.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class ChargingStationController {

    private final ChargingStationService stationService;

    public ChargingStationController(ChargingStationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public ResponseEntity<ChargingStation> createSimpleStation(
            @RequestBody ChargingStation station) {
        ChargingStation savedStation = stationService.saveStation(station);
        return ResponseEntity.ok(savedStation);
    }

    @GetMapping
    public List<ChargingStation> getAllStations() {
        return stationService.getAllStations();
    }

    @PostMapping("/check-charger-statuses")
    public ResponseEntity<Void> checkChargerStatuses() {
        stationService.checkAndUpdateChargerStatuses();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStationById(@PathVariable Long id) {
        try {
            ChargingStation station = stationService.findById(id);
            return ResponseEntity.ok(station);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.noContent().build(); 
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); 
        }
    }

}
