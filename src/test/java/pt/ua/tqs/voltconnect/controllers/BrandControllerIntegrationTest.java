package pt.ua.tqs.voltconnect.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;
import pt.ua.tqs.voltconnect.services.BrandService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
class BrandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private RestTemplate restTemplate;

    private BrandDTO brand1;
    private BrandDTO brand2;

    @BeforeEach
    void setup() {
        brand1 = new BrandDTO();
        brand1.setId("brand1");
        brand1.setName("Brand One");
        brand1.setModelsFile("models1.json");

        brand2 = new BrandDTO();
        brand2.setId("brand2");
        brand2.setName("Brand Two");
        brand2.setModelsFile("models2.json");
    }

    @Test
    void getAllBrands_ReturnsList() throws Exception {
        when(brandService.getAllBrands()).thenReturn(Arrays.asList(brand1, brand2));

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(brand1.getId()))
                .andExpect(jsonPath("$[0].name").value(brand1.getName()))
                .andExpect(jsonPath("$[0].models_file").value(brand1.getModelsFile()))
                .andExpect(jsonPath("$[1].id").value(brand2.getId()))
                .andExpect(jsonPath("$[1].name").value(brand2.getName()))
                .andExpect(jsonPath("$[1].models_file").value(brand2.getModelsFile()));
    }

    @Test
    void getBrandById_ExistingId_ReturnsBrand() throws Exception {
        when(brandService.getBrandById("brand1")).thenReturn(brand1);

        mockMvc.perform(get("/api/brands/brand1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brand1.getId()))
                .andExpect(jsonPath("$.name").value(brand1.getName()))
                .andExpect(jsonPath("$.models_file").value(brand1.getModelsFile()));
    }

    @Test
    void getBrandById_NonExistingId_ReturnsNotFound() throws Exception {
        when(brandService.getBrandById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/brands/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBrandByName_ExistingName_ReturnsBrands() throws Exception {
        when(brandService.getBrandByName("Brand")).thenReturn(Arrays.asList(brand1, brand2));

        mockMvc.perform(get("/api/brands/name/Brand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(brand1.getId()))
                .andExpect(jsonPath("$[0].models_file").value(brand1.getModelsFile()))
                .andExpect(jsonPath("$[1].id").value(brand2.getId()))
                .andExpect(jsonPath("$[1].models_file").value(brand2.getModelsFile()));
    }

    @Test
    void getBrandByName_NonExistingName_ReturnsNotFound() throws Exception {
        when(brandService.getBrandByName("nonexistent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/brands/name/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importBrands_DefaultForce_ImportsBrands() throws Exception {
        doNothing().when(brandService).importAllBrands(false);

        mockMvc.perform(post("/api/brands/import"))
                .andExpect(status().isOk());

        verify(brandService, times(1)).importAllBrands(false);
    }

    @Test
    void importBrands_ForceTrue_ImportsBrands() throws Exception {
        doNothing().when(brandService).importAllBrands(true);

        mockMvc.perform(post("/api/brands/import?force=true"))
                .andExpect(status().isOk());

        verify(brandService, times(1)).importAllBrands(true);
    }
} 