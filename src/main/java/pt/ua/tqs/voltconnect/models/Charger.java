package pt.ua.tqs.voltconnect.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chargingTime;

    @Enumerated(EnumType.STRING)
    private Status chargerStatus;

    @Enumerated(EnumType.STRING)
    private Type chargerType;

    private Double pricePerKWh;

    private Double chargingSpeed; // em kW, por exemplo

    public enum Status {
        AVAILABLE, OCCUPIED, OUT_OF_ORDER
    }

    public enum Type {
        AC1, AC2, DC
    }

    @ManyToOne
    @JoinColumn(name = "station_id")
    private ChargingStation chargingStation;

    // Faz com que no JSON só devolva o ID da station
    @JsonGetter("stationId")
    public Long getStationId() {
        return chargingStation != null ? chargingStation.getId() : null;
    }
}
