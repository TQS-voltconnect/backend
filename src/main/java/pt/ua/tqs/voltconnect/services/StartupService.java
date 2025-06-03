package pt.ua.tqs.voltconnect.services;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupService implements ApplicationListener<ApplicationReadyEvent> {

    private final VehicleService vehicleService;

    @Override
    @Async
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        vehicleService.importAllVehicles(false);
    }
}