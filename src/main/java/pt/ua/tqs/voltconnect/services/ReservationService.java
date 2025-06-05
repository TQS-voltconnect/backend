// ReservationService.java
package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.Reservation;

import java.util.Optional;

public interface ReservationService {
    Reservation createReservation(Reservation reservation);

    Optional<Reservation> getReservationById(Long id);
    Iterable<Reservation> getAllReservations();
    void cancelReservation(Long id) throws IllegalArgumentException;
    
    Reservation startCharging(Long id) throws IllegalArgumentException;
    Reservation stopCharging(Long id) throws IllegalArgumentException;
    Reservation processPayment(Long id, String paymentMethod) throws IllegalArgumentException;
}
