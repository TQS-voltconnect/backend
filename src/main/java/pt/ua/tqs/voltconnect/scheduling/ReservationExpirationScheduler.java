package pt.ua.tqs.voltconnect.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.models.Reservation.ReservationStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ReservationExpirationScheduler {

    private final ReservationRepository reservationRepository;

    public ReservationExpirationScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(fixedRate = 60000) // Executa a cada 1 minuto
    public void checkExpiredReservations() {
        // Busca todas as reservas agendadas
        List<Reservation> scheduledReservations = reservationRepository.findByStatus(ReservationStatus.SCHEDULED);
        
        Instant now = Instant.now();
        
        for (Reservation reservation : scheduledReservations) {
            Instant startTime = reservation.getStartTime().toInstant();
            Instant expirationTime = startTime.plus(15, ChronoUnit.MINUTES);
            
            // Se passou do tempo de expiração (startTime + 15min)
            if (now.isAfter(expirationTime)) {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
            }
        }
    }
} 