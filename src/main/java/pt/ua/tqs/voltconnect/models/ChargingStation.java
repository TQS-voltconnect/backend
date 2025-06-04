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

    private String city;

    private List<Float> location;

    private Long operatorId;

    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Charger> chargers;

    public void addCharger(Charger charger) {
        chargers.add(charger);
        charger.setChargingStation(this);
    }

    public void removeCharger(Charger charger) {
        chargers.remove(charger);
        charger.setChargingStation(null); // quebra a relação
    }

}
