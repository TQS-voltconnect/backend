package pt.ua.tqs.voltconnect.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.dtos.ReviewResponseDTO;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.services.ReviewService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private Review review1;
    private Review review2;
    private ChargingStation chargingStation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        chargingStation = new ChargingStation();
        chargingStation.setId(1L);

        review1 = Review.builder()
                .id(1L)
                .chargingStation(chargingStation)
                .rating(4)
                .comment("Great station!")
                .build();

        review2 = Review.builder()
                .id(2L)
                .chargingStation(chargingStation)
                .rating(5)
                .comment("Excellent service!")
                .build();
    }

    @Test
    void getAllReviews_ShouldReturnAllReviews() {
        // Arrange
        List<Review> reviews = Arrays.asList(review1, review2);
        when(reviewService.getAllReviews()).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseDTO>> response = reviewController.getAllReviews();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        ReviewResponseDTO firstReview = response.getBody().get(0);
        assertEquals(chargingStation.getId(), firstReview.getChargingStationId());
        assertEquals(4, firstReview.getRating());
        assertEquals("Great station!", firstReview.getComment());
    }

    @Test
    void createReview_ShouldCreateNewReview() {
        // Arrange
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(chargingStation.getId());
        requestDTO.setRating(4);
        requestDTO.setComment("Great station!");
        when(reviewService.createReview(any(ReviewRequestDTO.class))).thenReturn(review1);

        // Act
        ResponseEntity<ReviewResponseDTO> response = reviewController.createReview(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        ReviewResponseDTO createdReview = response.getBody();
        assertEquals(chargingStation.getId(), createdReview.getChargingStationId());
        assertEquals(4, createdReview.getRating());
        assertEquals("Great station!", createdReview.getComment());
    }
} 