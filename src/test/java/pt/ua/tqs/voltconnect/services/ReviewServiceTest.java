package pt.ua.tqs.voltconnect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.repositories.ReviewRepository;
import pt.ua.tqs.voltconnect.services.impl.ReviewServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

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
        when(reviewRepository.findAll()).thenReturn(reviews);

        // Act
        List<Review> result = reviewService.getAllReviews();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(review1, result.get(0));
        assertEquals(review2, result.get(1));
    }

    @Test
    void createReview_ShouldCreateNewReview() {
        // Arrange
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(chargingStation.getId());
        requestDTO.setRating(4);
        requestDTO.setComment("Great station!");

        when(chargingStationRepository.findById(chargingStation.getId())).thenReturn(Optional.of(chargingStation));
        when(reviewRepository.save(any(Review.class))).thenReturn(review1);

        // Act
        Review result = reviewService.createReview(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(review1.getId(), result.getId());
        assertEquals(review1.getRating(), result.getRating());
        assertEquals(review1.getComment(), result.getComment());
        assertEquals(review1.getChargingStation(), result.getChargingStation());
    }

    @Test
    void createReview_WithInvalidRating_ShouldThrowException() {
        // Arrange
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(chargingStation.getId());
        requestDTO.setRating(6); // Invalid rating
        requestDTO.setComment("Great station!");

        when(chargingStationRepository.findById(chargingStation.getId())).thenReturn(Optional.of(chargingStation));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(requestDTO));
    }

    @Test
    void createReview_WithNonExistentStation_ShouldThrowException() {
        // Arrange
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setChargingStationId(999L); // Non-existent station
        requestDTO.setRating(4);
        requestDTO.setComment("Great station!");

        when(chargingStationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(requestDTO));
    }
} 