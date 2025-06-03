package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charging_station_id")
    private ChargingStation chargingStation;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String comment;
}