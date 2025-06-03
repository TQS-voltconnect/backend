package pt.ua.tqs.voltconnect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;
import pt.ua.tqs.voltconnect.models.Brand;
import pt.ua.tqs.voltconnect.repositories.BrandRepository;
import pt.ua.tqs.voltconnect.services.impl.BrandServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand brand1;
    private Brand brand2;
    private BrandDTO brandDTO1;
    private BrandDTO brandDTO2;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(brandService, "brandsUrl", "http://test.api/brands");

        brand1 = Brand.builder()
                .id("brand1")
                .name("Brand One")
                .modelsFile("models1.json")
                .build();

        brand2 = Brand.builder()
                .id("brand2")
                .name("Brand Two")
                .modelsFile("models2.json")
                .build();

        brandDTO1 = new BrandDTO();
        brandDTO1.setId("brand1");
        brandDTO1.setName("Brand One");
        brandDTO1.setModelsFile("models1.json");

        brandDTO2 = new BrandDTO();
        brandDTO2.setId("brand2");
        brandDTO2.setName("Brand Two");
        brandDTO2.setModelsFile("models2.json");
    }

    @Test
    void getAllBrands_ReturnsList() {
        when(brandRepository.findAll()).thenReturn(Arrays.asList(brand1, brand2));
        
        List<BrandDTO> result = brandService.getAllBrands();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(brandDTO1.getId(), result.get(0).getId());
        assertEquals(brandDTO2.getId(), result.get(1).getId());
        verify(brandRepository, times(1)).findAll();
    }

    @Test
    void getBrandById_ExistingId_ReturnsBrand() {
        when(brandRepository.findById("brand1")).thenReturn(Optional.of(brand1));
        
        BrandDTO result = brandService.getBrandById("brand1");
        
        assertNotNull(result);
        assertEquals(brandDTO1.getId(), result.getId());
        assertEquals(brandDTO1.getName(), result.getName());
        verify(brandRepository, times(1)).findById("brand1");
    }

    @Test
    void getBrandById_NonExistingId_ReturnsNull() {
        when(brandRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        BrandDTO result = brandService.getBrandById("nonexistent");
        
        assertNull(result);
        verify(brandRepository, times(1)).findById("nonexistent");
    }

    @Test
    void getBrandByName_ReturnsMatchingBrands() {
        when(brandRepository.findByNameIgnoreCase("Brand")).thenReturn(Arrays.asList(brand1, brand2));
        
        List<BrandDTO> result = brandService.getBrandByName("Brand");
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(brandDTO1.getName(), result.get(0).getName());
        assertEquals(brandDTO2.getName(), result.get(1).getName());
        verify(brandRepository, times(1)).findByNameIgnoreCase("Brand");
    }

    @Test
    void importAllBrands_ForceTrue_ImportsAllBrands() {
        List<BrandDTO> apiResponse = Arrays.asList(brandDTO1, brandDTO2);
        ResponseEntity<List<BrandDTO>> responseEntity = ResponseEntity.ok(apiResponse);
        ParameterizedTypeReference<List<BrandDTO>> typeRef = new ParameterizedTypeReference<>() {};
        
        when(restTemplate.exchange(
                eq("http://test.api/brands"),
                eq(HttpMethod.GET),
                isNull(),
                eq(typeRef)
        )).thenReturn(responseEntity);
        
        when(brandRepository.findById("brand1")).thenReturn(Optional.empty());
        when(brandRepository.findById("brand2")).thenReturn(Optional.empty());
        
        brandService.importAllBrands(true);
        
        verify(brandRepository, times(1)).deleteAll();
        verify(brandRepository, times(2)).save(any(Brand.class));
    }

    @Test
    void importAllBrands_ForceFalse_ExistingData_DoesNothing() {
        when(brandRepository.count()).thenReturn(1L);
        ParameterizedTypeReference<List<BrandDTO>> typeRef = new ParameterizedTypeReference<>() {};
        
        brandService.importAllBrands(false);
        
        verify(brandRepository, never()).deleteAll();
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                isNull(),
                eq(typeRef)
        );
    }

    @Test
    void importAllBrands_ApiError_DoesNothing() {
        ResponseEntity<List<BrandDTO>> errorResponse = ResponseEntity.badRequest().build();
        ParameterizedTypeReference<List<BrandDTO>> typeRef = new ParameterizedTypeReference<>() {};
        
        when(restTemplate.exchange(
                eq("http://test.api/brands"),
                eq(HttpMethod.GET),
                isNull(),
                eq(typeRef)
        )).thenReturn(errorResponse);
        
        brandService.importAllBrands(true);
        
        verify(brandRepository, times(1)).deleteAll();
        verify(brandRepository, never()).save(any(Brand.class));
    }
} 