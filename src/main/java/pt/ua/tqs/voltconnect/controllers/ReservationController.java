package pt.ua.tqs.voltconnect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.PaymentRequest;
import pt.ua.tqs.voltconnect.services.ReservationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Optional;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "reservation-controller", description = "Handles all reservation operations.")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String message;
    }

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Create a reservation", description = "Book a charging session with specific charger and vehicle.")
    @PostMapping
    public ResponseEntity<Object> createReservation(@RequestBody Reservation reservation) {
        try {
            Reservation created = reservationService.createReservation(reservation);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Get all reservations", description = "Retrieve all reservations in the system.")
    @GetMapping
    public ResponseEntity<Iterable<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @Operation(summary = "Get reservation by ID", description = "Retrieve a reservation with full details.")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long id) {
        Optional<Reservation> reservationOpt = reservationService.getReservationById(id);
        if (reservationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservationOpt.get());
    }

    @Operation(summary = "Delete reservation", description = "Cancel a reservation by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Pay for a reservation", description = "Process the payment for a completed reservation.")
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> processPayment(@PathVariable Long id, @RequestBody PaymentRequest paymentRequest) {
        try {
            Reservation updated = reservationService.processPayment(id, paymentRequest.getPaymentMethod());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
