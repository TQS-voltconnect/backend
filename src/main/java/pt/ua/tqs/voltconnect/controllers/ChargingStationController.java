package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class ChargingStationController {

    @Autowired
    private ChargingStationService stationService;

    @GetMapping
    public List<ChargingStation> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/operator/{operatorId}")
    public List<ChargingStation> getStationsByOperator(@PathVariable Long operatorId) {
        return stationService.getStationsByOperatorId(operatorId);
    }

    @PostMapping
    public ChargingStation createStation(@RequestBody ChargingStation station) {
        return stationService.saveStation(station);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
