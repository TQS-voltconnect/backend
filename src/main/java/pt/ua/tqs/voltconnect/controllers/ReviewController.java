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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "review-controller",
    description = "Allows users to post and manage reviews for charging stations."
)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get all reviews", description = "Retrieves a list of all reviews submitted by users.")
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        List<ReviewResponseDTO> dtos = reviews.stream()
                .map(r -> new ReviewResponseDTO(
                        r.getId(),
                        r.getChargingStation().getId(),
                        r.getRating(),
                        r.getComment()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Post a new review", description = "Allows a user to submit a new review for a charging station.")
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewService.createReview(reviewRequestDTO);
        ReviewResponseDTO dto = new ReviewResponseDTO(
                review.getId(),
                review.getChargingStation().getId(),
                review.getRating(),
                review.getComment()
        );
        return ResponseEntity.ok(dto);
    }
    
    @Operation(summary = "Delete a review", description = "Deletes a review by its ID.")    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReviewById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get reviews by station", description = "Fetches all reviews associated with a specific charging station ID.")
    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByStationId(@PathVariable Long stationId) {
        List<Review> reviews = reviewService.getReviewsByStationId(stationId);
        List<ReviewResponseDTO> dtos = reviews.stream()
                .map(r -> new ReviewResponseDTO(r.getId(), r.getChargingStation().getId(), r.getRating(), r.getComment()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }


}