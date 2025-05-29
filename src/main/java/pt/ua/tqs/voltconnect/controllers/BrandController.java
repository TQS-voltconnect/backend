package pt.ua.tqs.voltconnect.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;
import pt.ua.tqs.voltconnect.services.BrandService;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable String id) {
        BrandDTO brand = brandService.getBrandById(id);
        return brand != null ? ResponseEntity.ok(brand) : ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<BrandDTO>> getBrandByName(@PathVariable String name) {
        List<BrandDTO> brands = brandService.getBrandByName(name);
        return brands.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(brands);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importBrands(@RequestParam(defaultValue = "false") boolean force) {
        brandService.importAllBrands(force);
        return ResponseEntity.ok().build();
    }
}