package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    private Long operatorId;

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    // @Column(name = "latitude")
    // private Float latitude;

    // @Column(name = "longitude")
    // private Float longitude;

    // @Column(name = "operator_id")
    // private Long operatorId;

    // private Double pricePerKWh;

    // @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonManagedReference
    // private List<Charger> chargers;

}
