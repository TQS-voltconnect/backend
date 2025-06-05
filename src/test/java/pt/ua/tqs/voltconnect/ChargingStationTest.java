package pt.ua.tqs.voltconnect;

import org.junit.jupiter.api.Test;
import pt.ua.tqs.voltconnect.models.Charger;
import pt.ua.tqs.voltconnect.models.ChargingStation;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ChargingStationTest {

    @Test
    void testAddCharger() {
        ChargingStation station = ChargingStation.builder()
                .chargers(new ArrayList<>())
                .build();

        Charger charger = new Charger();

        station.addCharger(charger);

        assertEquals(1, station.getChargers().size());
        assertEquals(station, charger.getChargingStation());
    }

    @Test
    void testRemoveCharger() {
        ChargingStation station = ChargingStation.builder()
                .chargers(new ArrayList<>())
                .build();

        Charger charger = new Charger();
        station.addCharger(charger);

        station.removeCharger(charger);

        assertEquals(0, station.getChargers().size());
        assertNull(charger.getChargingStation());
    }
}
