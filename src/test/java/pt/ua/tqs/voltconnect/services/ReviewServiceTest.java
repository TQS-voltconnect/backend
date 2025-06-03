package pt.ua.tqs.voltconnect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.repositories.ReviewRepository;
import pt.ua.tqs.voltconnect.services.impl.ReviewServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewRequestDTO validRequest;
    private ChargingStation station;

    @BeforeEach
    void setUp() {
        validRequest = new ReviewRequestDTO();
        validRequest.setChargingStationId(1L);
        validRequest.setRating(4);
        validRequest.setComment("Very good station.");

        station = ChargingStation.builder()
                .id(1L)
                .city("City")
                .location(List.of(40.0f, -8.0f))
                .operatorId(1L)
                .chargers(List.of())
                .build();
    }

    @Test
    void createReview_WithValidData_ShouldReturnSavedReview() {
        Review expectedReview = Review.builder()
                .chargingStation(station)
                .rating(4)
                .comment("Very good station.")
                .build();

        when(chargingStationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(reviewRepository.save(any())).thenReturn(expectedReview);

        Review result = reviewService.createReview(validRequest);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Very good station.", result.getComment());
        assertEquals(1L, result.getChargingStation().getId());
    }

    @Test
    void createReview_WithRatingTooLow_ShouldThrowException() {
        validRequest.setRating(0); // Invalid
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(validRequest));
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void createReview_WithRatingTooHigh_ShouldThrowException() {
        validRequest.setRating(6); // Invalid
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(validRequest));
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void createReview_WithNonexistentStation_ShouldThrowException() {
        when(chargingStationRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(validRequest));
        assertEquals("Charging station not found", exception.getMessage());
    }

    @Test
    void getAllReviews_ShouldReturnListOfReviews() {
        List<Review> mockReviews = List.of(
                Review.builder().rating(5).comment("Excellent!").build(),
                Review.builder().rating(3).comment("Okay.").build());

        when(reviewRepository.findAll()).thenReturn(mockReviews);

        List<Review> result = reviewService.getAllReviews();

        assertEquals(2, result.size());
        assertEquals("Excellent!", result.get(0).getComment());
        assertEquals(3, result.get(1).getRating());
    }
}
