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

import java.util.Date;
import java.util.List;

@Service
public class ReservationMaintenanceServiceImpl implements ReservationMaintenanceService {

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

        List<Reservation> reservations = reservationRepository.findAll();
        Date now = new Date();

        for (Reservation reservation : reservations) {
            Date start = reservation.getStartTime();
            Long chargingTime = reservation.getChargingTime() != null ? reservation.getChargingTime() : 0L;
            Date end = new Date(start.getTime() + chargingTime * 60000); // chargingTime em minutos

            Charger charger = chargerRepository.findById(reservation.getChargerId()).orElse(null);
            if (charger == null)
                continue;

            // Reserva terminada e não CANCELLED/EXPIRED
            if (end.before(now)
                    && reservation.getStatus() != ReservationStatus.CANCELLED
                    && reservation.getStatus() != ReservationStatus.EXPIRED) {

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                charger.setChargerStatus(Charger.Status.AVAILABLE);

                chargerRepository.save(charger);
            }
            // Reserva ativa (a decorrer) e status SCHEDULED
            else if (start.before(now) && end.after(now) && reservation.getStatus() == ReservationStatus.SCHEDULED) {
                charger.setChargerStatus(Charger.Status.OCCUPIED);

                chargerRepository.save(charger);
            }
        }
    }
}
