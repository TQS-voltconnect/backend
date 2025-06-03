package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.Review;
import java.util.List;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;

public interface ReviewService {
    Review createReview(ReviewRequestDTO reviewRequestDTO);

    List<Review> getAllReviews();

    
}