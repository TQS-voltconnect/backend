package pt.ua.tqs.voltconnect.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.repositories.ReviewRepository;

import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    private ChargingStation chargingStation;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        chargingStationRepository.deleteAll();

        chargingStation = ChargingStation.builder()
                .city("Test City")
                .location(Arrays.asList(40.0f, -8.0f))
                .operatorId(1L)
                .chargers(new ArrayList<>())
                .build();
        chargingStation = chargingStationRepository.save(chargingStation);
    }

    @Test
    void createAndGetReview_ShouldWork() throws Exception {
        // Create review
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(chargingStation.getId());
        requestDTO.setRating(4);
        requestDTO.setComment("Great station!");

        MvcResult result = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargingStationId").value(chargingStation.getId()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Great station!"))
                .andReturn();

        // Get all reviews
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargingStationId").value(chargingStation.getId()))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[0].comment").value("Great station!"));

        // Verify in database
        Review savedReview = reviewRepository.findAll().get(0);
        assertEquals(chargingStation.getId(), savedReview.getChargingStation().getId());
        assertEquals(4, savedReview.getRating());
        assertEquals("Great station!", savedReview.getComment());
    }

    @Test
    void createReview_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create review with invalid rating
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(chargingStation.getId());
        requestDTO.setRating(6); // Invalid rating (should be 1-5)
        requestDTO.setComment("Great station!");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
} 