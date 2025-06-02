package pt.ua.tqs.voltconnect.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.voltconnect.models.User;
import pt.ua.tqs.voltconnect.repositories.UserRepository;
import pt.ua.tqs.voltconnect.services.impl.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setName("User One");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<User> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user1, result.get(0));
        assertEquals(user2, result.get(1));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ExistingId_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Optional<User> result = userService.getUserById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(user1, result.get());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NonExistingId_ReturnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(999L);
        
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getUserByEmail_ExistingEmail_ReturnsUser() {
        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
        Optional<User> result = userService.getUserByEmail("user1@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(user1, result.get());
        verify(userRepository, times(1)).findByEmail("user1@example.com");
    }

    @Test
    void getUserByEmail_NonExistingEmail_ReturnsEmpty() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");
        
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void saveUser_ReturnsSavedUser() {
        when(userRepository.save(user1)).thenReturn(user1);
        User result = userService.saveUser(user1);
        
        assertNotNull(result);
        assertEquals(user1, result);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void deleteUser_DeletesSuccessfully() {
        Long id = 1L;
        doNothing().when(userRepository).deleteById(id);
        
        assertDoesNotThrow(() -> userService.deleteUser(id));
        verify(userRepository, times(1)).deleteById(id);
    }
} 