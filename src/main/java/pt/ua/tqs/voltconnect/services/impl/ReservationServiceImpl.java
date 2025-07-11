// ReservationServiceImpl.java
package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import pt.ua.tqs.voltconnect.models.Reservation;
import pt.ua.tqs.voltconnect.models.User;
import pt.ua.tqs.voltconnect.models.Vehicle;
import pt.ua.tqs.voltconnect.models.PaymentResult;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.services.ReservationService;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.repositories.VehicleRepository;
import pt.ua.tqs.voltconnect.services.PaymentService;
import pt.ua.tqs.voltconnect.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Service
public class ReservationServiceImpl implements ReservationService {

    private ReservationRepository reservationRepository;
    private ChargingStationRepository chargingStationRepository;
    private VehicleRepository vehicleRepository;
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    private static final double DISCOUNT_RATE = 0.9;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
            ChargingStationRepository chargingStationRepository, VehicleRepository vehicleRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    private static class CurvePoint {
        private int percentage;
        private double power;

        public int getPercentage() {
            return percentage;
        }

        public void setPercentage(int percentage) {
            this.percentage = percentage;
        }

        public double getPower() {
            return power;
        }

        public void setPower(double power) {
            this.power = power;
        }
    }

    private void validateReservationInput(Reservation reservation) {

        if (reservation.getVehicleId() == null) {
            throw new IllegalArgumentException("vehicleId cannot be null");
        }
        if (reservation.getChargingStationId() == null) {
            throw new IllegalArgumentException("chargingStationId cannot be null");
        }
        if (reservation.getChargerId() == null) {
            throw new IllegalArgumentException("chargerId cannot be null");
        }
        if (reservation.getStartTime() == null || reservation.getStartTime().before(new Date())) {
            throw new IllegalArgumentException("Start time must be a future date");
        }
    }

    private ChargingStation validateAndGetChargingStation(Long stationId, Long chargerId) {
        ChargingStation station = chargingStationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Charging station not found"));

        boolean chargerBelongsToStation = station.getChargers()
                .stream()
                .anyMatch(charger -> charger.getId().equals(chargerId));

        if (!chargerBelongsToStation) {
            throw new IllegalArgumentException("Charger does not belong to the specified charging station");
        }
        return station;
    }

    private Charger getChargerFromStation(ChargingStation station, Long chargerId) {
        return station.getChargers()
                .stream()
                .filter(c -> c.getId().equals(chargerId))
                .findFirst()
                .orElseThrow();
    }

    private void validateChargerCompatibility(Charger charger, Vehicle vehicle) {
        boolean acceptsChargerType;
        if (charger.getChargerType().name().startsWith("AC")) {
            acceptsChargerType = vehicle.getAcChargerJson() != null && !vehicle.getAcChargerJson().isEmpty();
        } else if (charger.getChargerType() == Charger.Type.DC) {
            acceptsChargerType = vehicle.getDcChargerJson() != null && !vehicle.getDcChargerJson().isEmpty();
        } else {
            acceptsChargerType = false;
        }

        if (!acceptsChargerType) {
            throw new IllegalArgumentException("Vehicle does not support this charger type");
        }
    }

    private double calculateChargingTime(Charger charger, Vehicle vehicle) {
        return (vehicle.getUsableBatterySize() / charger.getChargingSpeed()) * 60.0;
    }



    private void validateNoOverlappingReservations(Reservation reservation, long chargingTimeMinutes) {
        Date start = reservation.getStartTime();
        Date end = new Date(start.getTime() + chargingTimeMinutes * 60_000);
        List<Reservation> existingReservations = reservationRepository.findByChargerId(reservation.getChargerId());

        for (Reservation r : existingReservations) {
            Date rStart = r.getStartTime();
            Date rEnd = new Date(rStart.getTime() + r.getChargingTime() * 60_000);
            boolean overlap = start.before(rEnd) && end.after(rStart);
            if (overlap) {
                throw new IllegalArgumentException("Reservation conflicts with an existing reservation");
            }
        }
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        validateReservationInput(reservation);

        ChargingStation station = validateAndGetChargingStation(reservation.getChargingStationId(),
                reservation.getChargerId());
        Charger charger = getChargerFromStation(station, reservation.getChargerId());
        Vehicle vehicle = vehicleRepository.findById(reservation.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        validateChargerCompatibility(charger, vehicle);

        double chargingTimeMinutes = calculateChargingTime(charger, vehicle);
        long chargingTimeLong = (long) Math.ceil(chargingTimeMinutes);
        reservation.setChargingTime(chargingTimeLong);

        validateNoOverlappingReservations(reservation, chargingTimeLong);

        double price = vehicle.getUsableBatterySize() * charger.getPricePerKWh();
        Map<Long, Integer> counts = user.getStationReservationsCount();
        int previousCount = counts.getOrDefault(reservation.getChargingStationId(), 0);
        boolean applyDiscount = previousCount >= 3;

        reservation.setOriginalPrice(price);

        if (applyDiscount) {
            reservation.setDiscount(true);
            price = price * DISCOUNT_RATE; // apply discount
        } else {
            reservation.setDiscount(false);
        }

        price = Math.round(price * 100.0) / 100.0;
        reservation.setPrice(price);

        counts.put(reservation.getChargingStationId(), previousCount + 1);
        user.setStationReservationsCount(counts);
        userRepository.save(user);

        return reservationRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Iterable<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public void cancelReservation(Long id) throws IllegalArgumentException {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != Reservation.ReservationStatus.SCHEDULED) {
            throw new IllegalArgumentException("Can only cancel scheduled reservations");
        }

        ChargingStation station = chargingStationRepository.findById(reservation.getChargingStationId())
                .orElseThrow(() -> new IllegalArgumentException("Charging station not found"));

        Charger charger = station.getChargers()
                .stream()
                .filter(c -> c.getId().equals(reservation.getChargerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Charger not found"));

        charger.setChargerStatus(Charger.Status.AVAILABLE);
        chargingStationRepository.save(station);

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public Reservation processPayment(Long id, String paymentMethod) throws IllegalArgumentException {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
            throw new IllegalArgumentException("Can only pay for completed charging sessions");
        }

        if (reservation.getIsPaid()) {
            throw new IllegalArgumentException("Reservation is already paid");
        }

        PaymentResult result = paymentService.processPayment(
                paymentMethod,
                reservation.getPrice(),
                "EUR");

        if (!result.isSuccess()) {
            throw new IllegalArgumentException("Payment failed: " + result.getErrorMessage());
        }

        reservation.setPaymentMethod(paymentMethod);
        reservation.setIsPaid(true);
        reservation.setStatus(Reservation.ReservationStatus.PAID);

        return reservationRepository.save(reservation);
    }
}
