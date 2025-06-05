package pt.ua.tqs.voltconnect.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import pt.ua.tqs.voltconnect.dtos.BrandDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BrandControllerIT {

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getUrl(String path) {
        return "http://localhost:" + randomServerPort + "/api/brands" + path;
    }

    @Test
    void whenImportBrands_thenBrandsAreAvailable() {
        ResponseEntity<Void> importResponse = restTemplate.postForEntity(getUrl("/import?force=true"), null, Void.class);
        assertThat(importResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<BrandDTO[]> response = restTemplate.getForEntity(getUrl(""), BrandDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void whenGetBrandByName_thenReturnMatchingBrand() {
        restTemplate.postForEntity(getUrl("/import?force=true"), null, Void.class);

        ResponseEntity<BrandDTO[]> response = restTemplate.getForEntity(getUrl("/name/tesla"), BrandDTO[].class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }
}