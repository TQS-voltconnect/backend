package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonBackReference
    private Brand brand;

    private String model;
    private int releaseYear;
    private String variant;
    private String vehicleType;
    private double usableBatterySize;
    private String chargingVoltage;

    @Column(columnDefinition = "TEXT")
    private String acChargerJson;

    @Column(columnDefinition = "TEXT")
    private String dcChargerJson;

    @Column(columnDefinition = "TEXT")
    private String energyConsumptionJson;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private LocalDateTime imageUpdatedAt;
}