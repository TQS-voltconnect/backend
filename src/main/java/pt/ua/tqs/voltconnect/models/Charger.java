package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @JoinColumn(name = "station_id", nullable = false)
    @JsonBackReference
    private ChargingStation chargingStation;

}
