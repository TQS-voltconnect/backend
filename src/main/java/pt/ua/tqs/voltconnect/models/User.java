package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.HashMap;

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

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection
    @CollectionTable(name = "user_station_count", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "station_id")
    @Column(name = "reservation_count")
    @Builder.Default
    private Map<Long, Integer> stationReservationsCount = new HashMap<>();

    public enum Role {
        DRIVER, OPERATOR, TECHNICIAN, ADMIN
    }
}