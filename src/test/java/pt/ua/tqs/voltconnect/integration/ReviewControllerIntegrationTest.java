package pt.ua.tqs.voltconnect.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.tqs.voltconnect.controllers.ReviewController;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.services.ReviewService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(ReviewController.class)
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private ReviewRequestDTO requestDTO;
    private Review review;

    @BeforeEach
    void setUp() {
        requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(1L);
        requestDTO.setRating(5);
        requestDTO.setComment("Awesome station!");

        ChargingStation station = ChargingStation.builder()
                .id(1L)
                .city("City")
                .location(List.of(40.0f, -8.0f))
                .operatorId(1L)
                .chargers(List.of())
                .build();

        review = Review.builder()
                .id(10L)
                .chargingStation(station)
                .rating(5)
                .comment("Awesome station!")
                .build();
    }

    @Test
    void getAllReviews_ShouldReturnList() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(review));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargingStationId").value(1L))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Awesome station!"));
    }

    @Test
    void createReview_ValidRequest_ShouldReturnCreatedReview() throws Exception {
        when(reviewService.createReview(any(ReviewRequestDTO.class))).thenReturn(review);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargingStationId").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Awesome station!"));
    }

    @Test
    void createReview_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        when(reviewService.createReview(any(ReviewRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid rating"));

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid rating"));
    }

    @Test
    void deleteReview_ExistingId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/reviews/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_NonExistingId_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Review not found")).when(reviewService).deleteReviewById(999L);

        mockMvc.perform(delete("/api/reviews/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Review not found"));
    }
}
