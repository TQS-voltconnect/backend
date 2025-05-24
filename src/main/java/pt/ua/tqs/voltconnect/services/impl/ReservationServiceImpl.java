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
import pt.ua.tqs.voltconnect.models.Vehicle;
import pt.ua.tqs.voltconnect.repositories.ReservationRepository;
import pt.ua.tqs.voltconnect.services.ReservationService;
import pt.ua.tqs.voltconnect.repositories.ChargingStationRepository;
import pt.ua.tqs.voltconnect.repositories.VehicleRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getStartTime() == null || reservation.getStartTime().before(new Date())) {
            throw new IllegalArgumentException("Start time must be a future date");
        }

        ChargingStation station = chargingStationRepository.findById(reservation.getChargingStationId())
            .orElseThrow(() -> new IllegalArgumentException("Charging station not found"));

        boolean chargerBelongsToStation = station.getChargers()
            .stream()
            .anyMatch(charger -> charger.getId().equals(reservation.getChargerId()));

        if (!chargerBelongsToStation) {
            throw new IllegalArgumentException("Charger does not belong to the specified charging station");
        }

        Charger charger = station.getChargers()
            .stream()
            .filter(c -> c.getId().equals(reservation.getChargerId()))
            .findFirst()
            .orElseThrow();

        Vehicle vehicle = vehicleRepository.findById(reservation.getVehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

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

        double chargingTimeMinutes;
        if (charger.getChargerType() == Charger.Type.DC && vehicle.getDcChargerJson() != null) {
            // Usa curva de carga para cálculo detalhado

            // Classe interna para representar pontos da curva
            class CurvePoint {
                public int percentage;
                public double power;
            }

            // Desserializa charging_curve JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dcChargerNode;
            try {
                dcChargerNode = mapper.readTree(vehicle.getDcChargerJson());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error parsing DC charger JSON", e);
            }
            JsonNode curveNode = dcChargerNode.get("charging_curve");
            if (curveNode == null || !curveNode.isArray() || curveNode.size() < 2) {
                throw new IllegalArgumentException("Invalid or missing charging_curve data");
            }

            // Converte array JSON em lista de pontos
            List<CurvePoint> curvePoints = new ArrayList<>();
            for (JsonNode pointNode : curveNode) {
                CurvePoint p = new CurvePoint();
                p.percentage = pointNode.get("percentage").asInt();
                p.power = pointNode.get("power").asDouble();
                curvePoints.add(p);
            }

            // Calcula energia total (kWh) = usableBatterySize
            double totalEnergy = vehicle.getUsableBatterySize();

            // Soma tempo segmentado
            chargingTimeMinutes = 0.0;
            for (int i = 0; i < curvePoints.size() - 1; i++) {
                CurvePoint start = curvePoints.get(i);
                CurvePoint end = curvePoints.get(i + 1);

                double percentageDelta = (end.percentage - start.percentage) / 100.0;
                double energySegment = totalEnergy * percentageDelta;

                double avgPower = (start.power + end.power) / 2.0;

                if (avgPower <= 0) {
                    throw new IllegalArgumentException("Invalid average power in charging curve");
                }

                double segmentTime = (energySegment / avgPower) * 60.0; // minutos
                chargingTimeMinutes += segmentTime;
            }

        } else {
            // Para AC ou se não houver curva, calcula linearmente
            chargingTimeMinutes = (vehicle.getUsableBatterySize() / charger.getChargingSpeed()) * 60.0;
        }

        reservation.setChargingTime((long) Math.ceil(chargingTimeMinutes));

        // Calcula preço
        double price = vehicle.getUsableBatterySize() * charger.getPricePerKWh();
        reservation.setPrice(price);

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
}
