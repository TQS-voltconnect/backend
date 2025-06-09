package pt.ua.tqs.voltconnect.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;
import pt.ua.tqs.voltconnect.services.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(
    name = "brand-controller",
    description = "Handles operations related to EV brands, including listing and importing."
)
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "Get all brands", description = "Retrieve all registered car brands.")
    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @Operation(summary = "Get brand by ID", description = "Retrieve a specific car brand by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable String id) {
        BrandDTO brand = brandService.getBrandById(id);
        return brand != null ? ResponseEntity.ok(brand) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get brands by name", description = "Retrieve car brands matching a specific name.")
    @GetMapping("/name/{name}")
    public ResponseEntity<List<BrandDTO>> getBrandByName(@PathVariable String name) {
        List<BrandDTO> brands = brandService.getBrandByName(name);
        return brands.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(brands);
    }

    @Operation(summary = "Import brands", description = "Upload and register multiple car brands.")
    @PostMapping("/import")
    public ResponseEntity<Void> importBrands(@RequestParam(defaultValue = "false") boolean force) {
        brandService.importAllBrands(force);
        return ResponseEntity.ok().build();
    }
}