package pt.ua.tqs.voltconnect.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(
    name = "charging-station-controller",
    description = "Handles operations related to charging station management."
)
@RestController
@RequestMapping("/api/stations")
public class ChargingStationController {

    private final ChargingStationService stationService;

    public ChargingStationController(ChargingStationService stationService) {
        this.stationService = stationService;
    }

    @Operation(summary = "Create a new station", description = "Add a new charging station to the system.")
    @PostMapping
    public ResponseEntity<ChargingStation> createSimpleStation(
            @RequestBody ChargingStation station) {
        ChargingStation savedStation = stationService.saveStation(station);
        return ResponseEntity.ok(savedStation);
    }

    @Operation(summary = "Get all stations", description = "Retrieve a list of all charging stations.")
    @GetMapping
    public List<ChargingStation> getAllStations() {
        return stationService.getAllStations();
    }

    @Operation(summary = "Get station by ID", description = "Retrieve details of a specific charging station by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStationById(@PathVariable Long id) {
        try {
            ChargingStation station = stationService.findById(id);
            return ResponseEntity.ok(station);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete station", description = "Delete a charging station by its ID.")
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
