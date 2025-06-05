package pt.ua.tqs.voltconnect.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    private String id;

    private String name;

    private String modelsFile;

    /**
     * Bidirectional relationship with Vehicle.
     * This circular dependency is intentional and required for JPA entity mapping.
     * Serialization issues are handled with @JsonManagedReference/@JsonBackReference.
     * See SonarQube issue: this is a justified exception for ORM navigation.
     */
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Vehicle> vehicles;
}