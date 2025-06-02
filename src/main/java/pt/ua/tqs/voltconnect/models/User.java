package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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

    public enum Role {
        DRIVER, OPERATOR, TECHNICIAN, ADMIN
    }
}