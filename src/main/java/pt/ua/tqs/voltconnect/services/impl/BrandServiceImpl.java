package pt.ua.tqs.voltconnect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;
import pt.ua.tqs.voltconnect.models.Brand;
import pt.ua.tqs.voltconnect.repositories.BrandRepository;
import pt.ua.tqs.voltconnect.services.BrandService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.brands-url}")
    private String brandsUrl;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandById(String id) {
        return brandRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public List<BrandDTO> getBrandByName(String name) {
        return brandRepository.findByNameIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BrandDTO toDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setModelsFile(brand.getModelsFile());
        return dto;
    }

    @Override
    public void importAllBrands(boolean force) {
        if (!force && brandRepository.count() > 0) {
            return;
        }

        if (force) {
            brandRepository.deleteAll();
        }

        ResponseEntity<List<BrandDTO>> response = restTemplate.exchange(
                brandsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return;
        }

        List<BrandDTO> brands = response.getBody();
        for (BrandDTO brandDTO : brands) {
            if (brandRepository.findById(brandDTO.getId()).isEmpty()) {
                Brand brand = Brand.builder()
                        .id(brandDTO.getId())
                        .name(brandDTO.getName())
                        .modelsFile(brandDTO.getModelsFile())
                        .build();
                brandRepository.save(brand);
            }
        }
    }
}
