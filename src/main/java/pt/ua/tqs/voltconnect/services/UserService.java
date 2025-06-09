package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    User saveUser(User user);

    void deleteUser(Long id);
}
