package pt.ua.tqs.voltconnect.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ua.tqs.voltconnect.services.VehicleService;

@Component
@RequiredArgsConstructor
public class VehicleSyncScheduler {

    private final VehicleService vehicleService;

    @Scheduled(cron = "0 0 * * * *")
    public void updateVehicleData() {
        vehicleService.importAllVehicles(false);
    }
}