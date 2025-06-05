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
    @Scheduled(fixedRate = 30 * 1000) // Corre a cada 30 segundos
    @Transactional
    public void maintainReservationAndChargerStatus() {
        logger.info("Running scheduled maintenance at {}", new Date());

        List<Reservation> reservations = reservationRepository.findAll();
        Date now = new Date();

        for (Reservation reservation : reservations) {
            Date start = reservation.getStartTime();
            Long chargingTime = reservation.getChargingTime() != null ? reservation.getChargingTime() : 0L;
            Date end = new Date(start.getTime() + chargingTime * 60000); // chargingTime em minutos

            Charger charger = chargerRepository.findById(reservation.getChargerId()).orElse(null);
            if (charger == null) {
                logger.warn("Charger with ID {} not found for reservation {}", reservation.getChargerId(), reservation.getId());
                continue;
            }

            // Caso: reserva terminou mas não foi marcada como EXPIRADA
            if (end.before(now)
                    && reservation.getStatus() != ReservationStatus.CANCELLED
                    && reservation.getStatus() != ReservationStatus.EXPIRED) {

                logger.info("Expiring reservation ID {} (ended at {})", reservation.getId(), end);
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                charger.setChargerStatus(Charger.Status.AVAILABLE);
                chargerRepository.save(charger);

                logger.info("Charger ID {} set to AVAILABLE", charger.getId());
            }
            // Caso: reserva está a decorrer e ainda marcada como SCHEDULED
            else if (start.before(now) && end.after(now) && reservation.getStatus() == ReservationStatus.SCHEDULED) {

                logger.info("Reservation ID {} is currently active. Setting charger ID {} to OCCUPIED", reservation.getId(), charger.getId());
                charger.setChargerStatus(Charger.Status.OCCUPIED);
                chargerRepository.save(charger);
            }
        }

        logger.info("Reservation maintenance cycle completed.\n");
    }
}
