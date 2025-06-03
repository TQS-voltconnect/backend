package pt.ua.tqs.voltconnect.dtos;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private Long chargingStationId;
    private int rating;
    private String comment;
}