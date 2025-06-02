package pt.ua.tqs.voltconnect.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.tqs.voltconnect.controllers.ChargingStationController;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.services.ChargingStationService;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargingStationController.class)
class ChargingStationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ChargingStationService stationService;

    private ChargingStation sampleStation;

    @BeforeEach
    void setup() {
        sampleStation = ChargingStation.builder()
                .id(1L)
                .city("Porto")
                .location(List.of(41.1579f, -8.6291f))
                .operatorId(100L)
                .chargers(List.of()) 
                .build();
    }

    @Test
    void createSimpleStation_Success() throws Exception {
        when(stationService.saveStation(any(ChargingStation.class))).thenReturn(sampleStation);

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleStation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleStation.getId()))
                .andExpect(jsonPath("$.city").value(sampleStation.getCity()))
                .andExpect(jsonPath("$.location[0]").value(sampleStation.getLocation().get(0)))
                .andExpect(jsonPath("$.location[1]").value(sampleStation.getLocation().get(1)))
                .andExpect(jsonPath("$.operatorId").value(sampleStation.getOperatorId()));
    }

    @Test
    void getAllStations_ReturnsList() throws Exception {
        when(stationService.getAllStations()).thenReturn(List.of(sampleStation));

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(sampleStation.getId()))
                .andExpect(jsonPath("$[0].city").value(sampleStation.getCity()))
                .andExpect(jsonPath("$[0].location[0]").value(sampleStation.getLocation().get(0)))
                .andExpect(jsonPath("$[0].location[1]").value(sampleStation.getLocation().get(1)))
                .andExpect(jsonPath("$[0].operatorId").value(sampleStation.getOperatorId()));
    }

    @Test
    void getStationById_ExistingId_ReturnsStation() throws Exception {
        when(stationService.findById(sampleStation.getId())).thenReturn(sampleStation);

        mockMvc.perform(get("/api/stations/" + sampleStation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleStation.getId()))
                .andExpect(jsonPath("$.city").value(sampleStation.getCity()))
                .andExpect(jsonPath("$.location[0]").value(sampleStation.getLocation().get(0)))
                .andExpect(jsonPath("$.location[1]").value(sampleStation.getLocation().get(1)))
                .andExpect(jsonPath("$.operatorId").value(sampleStation.getOperatorId()));
    }

    @Test
    void getStationById_NotFound_ReturnsNotFound() throws Exception {
        when(stationService.findById(999L)).thenThrow(new RuntimeException("Station not found"));

        mockMvc.perform(get("/api/stations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStation_ExistingId_ReturnsNoContent() throws Exception {

        mockMvc.perform(delete("/api/stations/{id}", sampleStation.getId()))
                .andExpect(status().isNoContent());
    }


    @Test
    void deleteStation_NotFound_ReturnsNotFound() throws Exception {
        Long nonExistingId = 999L;

        doThrow(new RuntimeException("Station not found"))
            .when(stationService).deleteStation(nonExistingId);

        mockMvc.perform(delete("/api/stations/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }


}
