package pt.ua.tqs.voltconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.tqs.voltconnect.models.Charger;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByChargingStationId(Long stationId);
}
