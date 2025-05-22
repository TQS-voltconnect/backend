package pt.ua.tqs.voltconnect.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VehicleDTO {

    private String id;

    private String brand;

    @JsonProperty("brand_id")
    private String brandId;

    @JsonProperty("model")
    private String model;

    @JsonProperty("release_year")
    private int releaseYear;

    private String variant;

    @JsonProperty("vehicle_type")
    private String vehicleType;

    @JsonProperty("usable_battery_size")
    private double usableBatterySize;

    @JsonProperty("charging_voltage")
    private String chargingVoltage;

    @JsonProperty("ac_charger")
    private ChargerSpec acCharger;

    @JsonProperty("dc_charger")
    private DcChargerSpec dcCharger;

    @JsonProperty("energy_consumption")
    private EnergyConsumption energyConsumption;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_updated_at")
    private String imageUpdatedAt;
}

// ac charger
@Data
class ChargerSpec {

    @JsonProperty("usable_phases")
    private int usablePhases;

    private List<String> ports;

    @JsonProperty("max_power")
    private double maxPower;
}

// can be used to calculate charging time
@Data
class ChargingCurvePoint {

    private int percentage;

    private double power;
}

// dc charger
@Data
class DcChargerSpec {

    private List<String> ports;

    @JsonProperty("max_power")
    private double maxPower;

    @JsonProperty("is_default_charging_curve")
    private boolean isDefaultChargingCurve;

    @JsonProperty("charging_curve")
    private List<ChargingCurvePoint> chargingCurve;
}

@Data
class EnergyConsumption {

    @JsonProperty("average_consumption")
    private double averageConsumption;

    // possible to calculate the range if not available from the API with average_consumption and usable_battery_size
    private Integer range;
}