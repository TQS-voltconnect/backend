package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.services.ChargingStationService;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.Charger;

import java.util.Date;
import java.util.List;

@Service
public class ChargingStationServiceImpl implements ChargingStationService {

    @Autowired
    private ChargingStationRepository stationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public List<ChargingStation> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public ChargingStation findById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
    }

    @Override
    public List<ChargingStation> getStationsByOperatorId(Long operatorId) {
        return stationRepository.findByOperatorId(operatorId);
    }

    @Override
    public ChargingStation saveStation(ChargingStation station) {
        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ChargingStationNotFoundException(id);
        }
        stationRepository.deleteById(id);
    }

    @Override
    public void checkAndUpdateChargerStatuses() {
        Date now = new Date();
        List<ChargingStation> stations = stationRepository.findAll();

        for (ChargingStation station : stations) {
            for (Charger charger : station.getChargers()) {
                List<Reservation> reservations = reservationRepository.findByChargerId(charger.getId());
                
                boolean hasActiveReservation = false;
                boolean hasExpiredReservation = false;

                for (Reservation reservation : reservations) {
                    // Check for current reservations
                    if (reservation.getStatus() == Reservation.ReservationStatus.SCHEDULED && 
                        reservation.getStartTime().before(now) && 
                        reservation.getChargingTime() != null) {
                        
                        Date endTime = new Date(reservation.getStartTime().getTime() + 
                                             (reservation.getChargingTime() * 60 * 1000));
                        
                        if (endTime.after(now)) {
                            hasActiveReservation = true;
                            if (charger.getChargerStatus() != Charger.Status.OCCUPIED) {
                                charger.setChargerStatus(Charger.Status.OCCUPIED);
                            }
                        } else {
                            hasExpiredReservation = true;
                            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
                            reservationRepository.save(reservation);
                        }
                    }
                }

                // If no active reservation and charger is occupied, set it to available
                if (!hasActiveReservation && charger.getChargerStatus() == Charger.Status.OCCUPIED) {
                    charger.setChargerStatus(Charger.Status.AVAILABLE);
                }
            }
            stationRepository.save(station);
        }
    }

    private static class ChargingStationNotFoundException extends RuntimeException {
        public ChargingStationNotFoundException(Long id) {
            super("Charging Station with ID " + id + " not found.");
        }
    }

}
