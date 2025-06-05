package pt.ua.tqs.voltconnect.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.services.ReservationService;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
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
                                .userId(1L)
                                .build();

                mockMvc.perform(post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidReservation)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("{\"message\":\"Invalid data\"}"));
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

        @Test
        void cancelReservation_Success() throws Exception {
                mockMvc.perform(delete("/api/reservations/1"))
                                .andExpect(status().isOk());

                verify(reservationService).cancelReservation(1L);
        }

        @Test
        void cancelReservation_InvalidId_BadRequest() throws Exception {
                doThrow(new IllegalArgumentException("Invalid ID"))
                                .when(reservationService).cancelReservation(999L);

                mockMvc.perform(delete("/api/reservations/999"))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid ID"));
        }

        @Test
        void startCharging_Success() throws Exception {
                sampleReservation.setStatus(Reservation.ReservationStatus.CHARGING);
                when(reservationService.startCharging(1L)).thenReturn(sampleReservation);

                mockMvc.perform(post("/api/reservations/1/start"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("CHARGING"));
        }

        @Test
        void startCharging_InvalidId_BadRequest() throws Exception {
                doThrow(new IllegalArgumentException("Invalid ID"))
                                .when(reservationService).startCharging(999L);

                mockMvc.perform(post("/api/reservations/999/start"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Invalid ID"));
        }

        @Test
        void stopCharging_Success() throws Exception {
                sampleReservation.setStatus(Reservation.ReservationStatus.COMPLETED);
                when(reservationService.stopCharging(1L)).thenReturn(sampleReservation);

                mockMvc.perform(post("/api/reservations/1/stop"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        void stopCharging_InvalidId_BadRequest() throws Exception {
                doThrow(new IllegalArgumentException("Stop failed"))
                                .when(reservationService).stopCharging(999L);

                mockMvc.perform(post("/api/reservations/999/stop"))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Stop failed"));
        }

        @Test
        void processPayment_Success() throws Exception {
                sampleReservation.setStatus(Reservation.ReservationStatus.PAID);
                when(reservationService.processPayment(eq(1L), eq("card"))).thenReturn(sampleReservation);

                String payload = objectMapper
                                .writeValueAsString(new pt.ua.tqs.voltconnect.models.PaymentRequest("card"));

                mockMvc.perform(post("/api/reservations/1/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("PAID"));
        }

        @Test
        void processPayment_Invalid_BadRequest() throws Exception {
                doThrow(new IllegalArgumentException("Payment failed"))
                                .when(reservationService).processPayment(eq(999L), eq("card"));

                String payload = objectMapper
                                .writeValueAsString(new pt.ua.tqs.voltconnect.models.PaymentRequest("card"));

                mockMvc.perform(post("/api/reservations/999/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Payment failed"));
        }

}
