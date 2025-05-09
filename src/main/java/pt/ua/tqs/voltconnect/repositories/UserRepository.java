package pt.ua.tqs.voltconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.tqs.voltconnect.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
