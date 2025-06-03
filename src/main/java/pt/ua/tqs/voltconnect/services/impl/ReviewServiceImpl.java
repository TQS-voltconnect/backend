package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.repositories.ReviewRepository;
import pt.ua.tqs.voltconnect.services.ReviewService;
import java.util.List;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ChargingStationRepository chargingStationRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, ChargingStationRepository chargingStationRepository) {
        this.reviewRepository = reviewRepository;
        this.chargingStationRepository = chargingStationRepository;
    }

    @Override
    public Review createReview(ReviewRequestDTO reviewRequestDTO) {
        if (reviewRequestDTO.getRating() < 1 || reviewRequestDTO.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ChargingStation station = chargingStationRepository.findById(reviewRequestDTO.getChargingStationId())
                .orElseThrow(() -> new IllegalArgumentException("Charging station not found"));

        Review review = Review.builder()
                .chargingStation(station)
                .rating(reviewRequestDTO.getRating())
                .comment(reviewRequestDTO.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}