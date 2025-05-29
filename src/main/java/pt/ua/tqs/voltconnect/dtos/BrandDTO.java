package pt.ua.tqs.voltconnect.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BrandDTO {

    private String id;
    private String name;

    @JsonProperty("models_file")
    private String modelsFile;
}