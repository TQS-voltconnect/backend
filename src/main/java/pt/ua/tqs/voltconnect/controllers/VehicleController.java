package pt.ua.tqs.voltconnect.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;
import pt.ua.tqs.voltconnect.services.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping("/import")
    public ResponseEntity<Void> importAll(@RequestParam(defaultValue = "false") boolean force) {
        vehicleService.importAllVehicles(force);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/brand/{name}")
    public ResponseEntity<List<VehicleDTO>> getByBrand(@PathVariable String name) {
        return ResponseEntity.ok(vehicleService.getVehiclesByBrand(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getById(@PathVariable String id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
