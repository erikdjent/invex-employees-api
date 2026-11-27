package com.invex.employees;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Boots the whole application on H2 and walks through the real lifecycle of an
 * employee,
 * plus the public endpoints (health check and OpenAPI contract).
 */
@SpringBootTest(properties = { "spring.security.user.name=tester", "spring.security.user.password=secret" })
@AutoConfigureMockMvc
class EmployeeApiIntegrationTest {

        private static final RequestPostProcessor AUTH = httpBasic("tester", "secret");

        @Autowired
        private MockMvc mvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("create two, read, update partially, search, delete, and confirm it is gone")
        void fullLifecycle() throws Exception {
                String batch = """
                                [
                                  {"firstName":"Laura","paternalLastName":"Méndez","maternalLastName":"Ortiz","age":35,
                                   "sex":"FEMALE","birthDate":"10-01-1990","position":"Product Owner"},
                                  {"firstName":"Pedro","paternalLastName":"Núñez","age":45,"sex":"MALE",
                                   "birthDate":"05-05-1980","position":"Architect","active":false}
                                ]""";

                String created = mvc.perform(post("/employees").with(AUTH)
                                .contentType(MediaType.APPLICATION_JSON).content(batch))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                                .andExpect(jsonPath("$[0].active", is(true)))
                                .andExpect(jsonPath("$[1].active", is(false)))
                                .andReturn().getResponse().getContentAsString();
                JsonNode laura = objectMapper.readTree(created).get(0);
                long id = laura.get("id").asLong();

                mvc.perform(get("/employees/{id}", id).with(AUTH))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.birthDate", is("10-01-1990")));

                mvc.perform(put("/employees/{id}", id).with(AUTH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"position\":\"Head of Product\",\"age\":36}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.position", is("Head of Product")))
                                .andExpect(jsonPath("$.age", is(36)))
                                .andExpect(jsonPath("$.firstName", is("Laura")))
                                .andExpect(jsonPath("$.createdAt", is(laura.get("createdAt").asText())));

                mvc.perform(get("/employees/search").param("name", "méndez").with(AUTH))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].id", is((int) id)));

                mvc.perform(delete("/employees/{id}", id).with(AUTH))
                                .andExpect(status().isNoContent());

                mvc.perform(get("/employees/{id}", id).with(AUTH))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("health check and API docs are public")
        void publicEndpoints() throws Exception {
                mvc.perform(get("/actuator/health"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("UP")));
                mvc.perform(get("/v3/api-docs"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paths['/employees/search']").exists());
        }

        @Test
        @DisplayName("other actuator endpoints stay protected")
        void sensitiveActuatorIsNotExposed() throws Exception {
                mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        }
}
