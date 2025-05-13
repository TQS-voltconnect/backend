package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private List<Float> location;

    @Column(name = "operator_id")
    private Long operatorId;

    private Double pricePerKWh;

    private int numberOfChargers;

    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Charger> chargers;
}
