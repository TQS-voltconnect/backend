package pt.ua.tqs.voltconnect.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargingStationService;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*") // Add this
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

    
    // @Autowired
    // private ChargingStationService stationService;

    // // Explicitly define consumes and produces
    // @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // public ResponseEntity<ChargingStation> createStation(@RequestBody ChargingStation station) {
    //     ChargingStation savedStation = stationService.saveStation(station);
    //     return ResponseEntity.ok(savedStation);
    // }
    
    @GetMapping
    public List<ChargingStation> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStationById(@PathVariable Long id) {
        ChargingStation station = stationService.findById(id);
        if (station != null) {
            return ResponseEntity.ok(station);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
