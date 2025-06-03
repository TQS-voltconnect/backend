package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String vehicleId;

    private Long chargingStationId;

    private Long chargerId;

    private Date startTime;
    
    private Long chargingTime; 

    private Double price;
}
