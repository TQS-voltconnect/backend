package pt.ua.tqs.voltconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ua.tqs.voltconnect.models.ChargingStation;

import java.util.List;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    List<ChargingStation> findByOperatorId(Long operatorId);
}
