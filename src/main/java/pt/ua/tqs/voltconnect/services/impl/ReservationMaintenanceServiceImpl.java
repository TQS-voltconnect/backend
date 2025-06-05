package pt.ua.tqs.voltconnect.services.impl;

import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.Reservation.ReservationStatus;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.repositories.ChargerRepository;
import pt.ua.tqs.voltconnect.services.ReservationMaintenanceService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Service
public class ReservationMaintenanceServiceImpl implements ReservationMaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationMaintenanceServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ChargerRepository chargerRepository;

    public ReservationMaintenanceServiceImpl(ReservationRepository reservationRepository,
            ChargerRepository chargerRepository) {
        this.reservationRepository = reservationRepository;
        this.chargerRepository = chargerRepository;
    }

    @Override
    @Scheduled(fixedRate = 30 * 1000)
    @Transactional
    public void maintainReservationAndChargerStatus() {
        logger.info("Running scheduled maintenance at {}", new Date());

        List<Reservation> reservations = reservationRepository.findAll();
        Date now = new Date();

        for (Reservation reservation : reservations) {
            Date start = reservation.getStartTime();
            Long chargingTime = reservation.getChargingTime() != null ? reservation.getChargingTime() : 0L;
            Date end = new Date(start.getTime() + chargingTime * 60000);

            Charger charger = chargerRepository.findById(reservation.getChargerId()).orElse(null);
            if (charger == null) {
                logger.warn("Charger with ID {} not found for reservation {}", reservation.getChargerId(),
                        reservation.getId());
                continue;
            }

            if (reservation.getStatus() == ReservationStatus.CHARGING && end.before(now)) {
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(reservation);

                charger.setChargerStatus(Charger.Status.AVAILABLE);
                chargerRepository.save(charger);

                logger.info("Completed reservation ID {} (ended at {})", reservation.getId(), end);
            } else if (reservation.getStatus() == ReservationStatus.SCHEDULED && start.before(now) && end.after(now)) {
                charger.setChargerStatus(Charger.Status.OCCUPIED);
                reservation.setStatus(ReservationStatus.CHARGING);
                reservationRepository.save(reservation);
                chargerRepository.save(charger);

                logger.info("Reservation ID {} is now CHARGING. Charger ID {} set to OCCUPIED", reservation.getId(),
                        charger.getId());
            } else if (reservation.getStatus() == ReservationStatus.SCHEDULED && end.before(now)) {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                charger.setChargerStatus(Charger.Status.AVAILABLE);
                chargerRepository.save(charger);

                logger.info("Expired reservation ID {} (ended at {})", reservation.getId(), end);
            }
        }

        logger.info("Reservation maintenance cycle completed.\n");
    }
}
