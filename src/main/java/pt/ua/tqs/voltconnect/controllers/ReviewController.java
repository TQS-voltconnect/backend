package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ua.tqs.voltconnect.models.Review;
import pt.ua.tqs.voltconnect.services.ReviewService;
import pt.ua.tqs.voltconnect.dtos.ReviewRequestDTO;
import pt.ua.tqs.voltconnect.dtos.ReviewResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        List<ReviewResponseDTO> dtos = reviews.stream()
                .map(r -> new ReviewResponseDTO(
                        r.getChargingStation().getId(),
                        r.getRating(),
                        r.getComment()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewService.createReview(reviewRequestDTO);
        ReviewResponseDTO dto = new ReviewResponseDTO(
                review.getChargingStation().getId(),
                review.getRating(),
                review.getComment()
        );
        return ResponseEntity.ok(dto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}