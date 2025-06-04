package pt.ua.tqs.voltconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long chargingStationId;
    private int rating;
    private String comment;
}