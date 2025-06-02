package pt.ua.tqs.voltconnect.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.tqs.voltconnect.controllers.UserController;
import pt.ua.tqs.voltconnect.models.User;
import pt.ua.tqs.voltconnect.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setup() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john.doe@example.com");
    }

    @Test
    void getAllUsers_ReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(sampleUser.getId()))
                .andExpect(jsonPath("$[0].name").value(sampleUser.getName()))
                .andExpect(jsonPath("$[0].email").value(sampleUser.getEmail()));
    }

    @Test
    void getUserById_ExistingId_ReturnsUser() throws Exception {
        when(userService.getUserById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/" + sampleUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleUser.getId()))
                .andExpect(jsonPath("$.name").value(sampleUser.getName()))
                .andExpect(jsonPath("$.email").value(sampleUser.getEmail()));
    }

    @Test
    void getUserById_NonExistingId_ReturnsNotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.saveUser(any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleUser.getId()))
                .andExpect(jsonPath("$.name").value(sampleUser.getName()))
                .andExpect(jsonPath("$.email").value(sampleUser.getEmail()));
    }

    @Test
    void createUser_InvalidData_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setName("");
        invalidUser.setEmail("");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(sampleUser.getId());

        mockMvc.perform(delete("/api/users/" + sampleUser.getId()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(sampleUser.getId());
    }

    @Test
    void deleteUser_NonExistingId_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("User not found"))
            .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
} 