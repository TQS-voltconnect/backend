package pt.ua.tqs.voltconnect.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.tqs.voltconnect.controllers.ReservationController;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.services.ReservationService;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ReservationService reservationService;

        private Reservation sampleReservation;

        @BeforeEach
        void setup() {
                sampleReservation = Reservation.builder()
                                .id(1L)
                                .userId(1L)
                                .vehicleId("vehicle-123")
                                .chargingStationId(10L)
                                .chargerId(100L)
                                .startTime(Date.from(Instant.now().plusSeconds(3600)))
                                .build();
        }

        @Test
        void createReservation_Success() throws Exception {
                when(reservationService.createReservation(any(Reservation.class))).thenReturn(sampleReservation);

                mockMvc.perform(post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleReservation)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(sampleReservation.getId()))
                                .andExpect(jsonPath("$.userId").value(sampleReservation.getUserId()))
                                .andExpect(jsonPath("$.vehicleId").value(sampleReservation.getVehicleId()));
        }

        @Test
        void createReservation_InvalidData_BadRequest() throws Exception {
                when(reservationService.createReservation(any(Reservation.class)))
                                .thenThrow(new IllegalArgumentException("Invalid data"));

                Reservation invalidReservation = Reservation.builder()
                                .userId(1L) // Faltando campos obrigatórios, exemplo vehicleId
                                .build();

                mockMvc.perform(post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidReservation)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid data"));
        }

        @Test
        void getAllReservations_ReturnsList() throws Exception {
                when(reservationService.getAllReservations()).thenReturn(List.of(sampleReservation));

                mockMvc.perform(get("/api/reservations"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(sampleReservation.getId()));
        }

        @Test
        void getReservation_ExistingId_Success() throws Exception {
                when(reservationService.getReservationById(sampleReservation.getId()))
                                .thenReturn(Optional.of(sampleReservation));

                mockMvc.perform(get("/api/reservations/" + sampleReservation.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(sampleReservation.getId()));
        }

        @Test
        void getReservation_NonExistingId_NotFound() throws Exception {
                when(reservationService.getReservationById(9999L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/reservations/9999"))
                                .andExpect(status().isNotFound());
        }
}
