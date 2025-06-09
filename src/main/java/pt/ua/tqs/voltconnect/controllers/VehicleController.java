package pt.ua.tqs.voltconnect.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;
import pt.ua.tqs.voltconnect.services.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(
    name = "vehicle-controller",
    description = "Exposes endpoints for retrieving and importing electric vehicles."
)
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Import vehicles", description = "Imports a list of electric vehicles from a data source (e.g., file or external API).")
    @PostMapping("/import")
    public ResponseEntity<Void> importAll(@RequestParam(defaultValue = "false") boolean force) {
        vehicleService.importAllVehicles(force);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all vehicles", description = "Retrieves a complete list of available electric vehicles.")
    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @Operation(summary = "Get vehicles by brand", description = "Returns all vehicles associated with the specified brand name.")
    @GetMapping("/brand/{name}")
    public ResponseEntity<List<VehicleDTO>> getByBrand(@PathVariable String name) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByBrand(name);
        if (vehicles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Get vehicle by ID", description = "Fetches a specific vehicle by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getById(@PathVariable String id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
