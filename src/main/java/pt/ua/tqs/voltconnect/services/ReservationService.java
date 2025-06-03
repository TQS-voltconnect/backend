// ReservationService.java
package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.Reservation;

import java.util.Optional;

public interface ReservationService {
    Reservation createReservation(Reservation reservation);

    Optional<Reservation> getReservationById(Long id);
    Iterable<Reservation> getAllReservations();
}
