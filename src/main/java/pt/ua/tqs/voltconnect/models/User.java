package pt.ua.tqs.voltconnect.models;

import java.util.HashMap;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @ElementCollection
    @CollectionTable(name = "user_station_count", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "station_id")
    @Column(name = "reservation_count")
    @Builder.Default
    private Map<Long, Integer> stationReservationsCount = new HashMap<>();


    @Enumerated(EnumType.STRING)
    private Role role;


    public enum Role {
        DRIVER, OPERATOR, TECHNICIAN, ADMIN
    }
}