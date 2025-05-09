package pt.ua.tqs.voltconnect.models;

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

    public enum Status {
        AVAILABLE, OCCUPIED, OUT_OF_ORDER
    }

    @ManyToOne
    @JoinColumn(name = "station_id")
    private ChargingStation chargingStation;
}
