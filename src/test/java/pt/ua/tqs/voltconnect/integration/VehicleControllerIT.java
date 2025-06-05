package pt.ua.tqs.voltconnect.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import pt.ua.tqs.voltconnect.dtos.VehicleDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VehicleControllerIT {

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getUrl(String path) {
        return "http://localhost:" + randomServerPort + "/api/vehicles" + path;
    }

    @Test
    void whenImportVehicles_thenVehiclesAreAvailable() {
        ResponseEntity<Void> importResponse = restTemplate.postForEntity(getUrl("/import?force=true"), null, Void.class);
        assertThat(importResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<VehicleDTO[]> response = restTemplate.getForEntity(getUrl(""), VehicleDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void whenGetVehiclesByBrand_thenReturnVehiclesOrNotFound() {
        restTemplate.postForEntity(getUrl("/import?force=true"), null, Void.class);

        // get vehicles by brand
        ResponseEntity<VehicleDTO[]> response = restTemplate.getForEntity(getUrl("/brand/Tesla"), VehicleDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();

        // get vehicles by non existent brand
        ResponseEntity<VehicleDTO[]> responseNotFound = restTemplate.getForEntity(getUrl("/brand/NonExistentBrand"), VehicleDTO[].class);
        assertThat(responseNotFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}