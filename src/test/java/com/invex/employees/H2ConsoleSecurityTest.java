package com.invex.employees;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The H2 console is a separate servlet (not Spring MVC), so it needs a real server to be tested.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.security.user.name=tester", "spring.security.user.password=secret"})
class H2ConsoleSecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("the H2 console asks for the API credentials")
    void requiresCredentials() {
        ResponseEntity<String> response = restTemplate.getForEntity("/h2-console/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("with the API credentials the H2 console login page is served and can be framed")
    void opensWithCredentials() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("tester", "secret")
                .getForEntity("/h2-console/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("SAMEORIGIN");
        assertThat(response.getBody()).containsIgnoringCase("H2 Console");
    }
}
