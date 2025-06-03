package pt.ua.tqs.voltconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.tqs.voltconnect.models.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}