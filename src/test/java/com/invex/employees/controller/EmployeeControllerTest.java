package com.invex.employees.controller;

import static com.invex.employees.support.EmployeeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.invex.employees.config.RequestLoggingFilter;
import com.invex.employees.config.SecurityConfig;
import com.invex.employees.dto.EmployeeRequest;
import com.invex.employees.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.service.EmployeeService;

/**
 * Web layer tests: routing, HTTP status codes, JSON contract, validation and
 * security.
 * The service is mocked so failures here point straight at the controller or
 * its configuration.
 */
@WebMvcTest(controllers = EmployeeController.class, properties = { "spring.security.user.name=tester",
        "spring.security.user.password=secret" })
@Import({ SecurityConfig.class, RequestLoggingFilter.class })
class EmployeeControllerTest {

    private static final RequestPostProcessor AUTH = httpBasic("tester", "secret");

    private static final String VALID_EMPLOYEE = """
            {"firstName":"Juan","middleName":"Carlos","paternalLastName":"Pérez","maternalLastName":"López",
             "age":30,"sex":"MALE","birthDate":"15-03-1994","position":"Backend Developer"}""";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EmployeeService service;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.with(AUTH).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Nested
    @DisplayName("security")
    class Security {

        @Test
        @DisplayName("rejects requests without credentials with 401 and a JSON body")
        void noCredentials() throws Exception {
            mvc.perform(get("/employees"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().exists("WWW-Authenticate"))
                    .andExpect(jsonPath("$.status", is(401)));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("rejects wrong credentials with 401")
        void wrongCredentials() throws Exception {
            mvc.perform(get("/employees").with(httpBasic("tester", "wrong")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("every response carries a request id, and a safe client id is echoed back")
        void requestId() throws Exception {
            mvc.perform(get("/employees").with(AUTH).header("X-Request-Id", "abc-123"))
                    .andExpect(header().string("X-Request-Id", "abc-123"));
            mvc.perform(get("/employees").with(AUTH).header("X-Request-Id", "bad id\nINJECTED"))
                    .andExpect(header().string("X-Request-Id", matchesPattern("[0-9a-f-]{36}")));
        }
    }

    @Nested
    @DisplayName("GET /employees")
    class FindAll {

        @Test
        @DisplayName("returns 200 with the list and dates in dd-MM-yyyy")
        void ok() throws Exception {
            when(service.findAll()).thenReturn(List.of(juanResponse().build()));

            mvc.perform(get("/employees").with(AUTH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].firstName", is("Juan")))
                    .andExpect(jsonPath("$[0].birthDate", is("15-03-1994")))
                    .andExpect(jsonPath("$[0].createdAt", is("26-11-2026 10:15:30")))
                    .andExpect(jsonPath("$[0].sex", is("MALE")))
                    .andExpect(jsonPath("$[0].active", is(true)));
        }
    }

    @Nested
    @DisplayName("GET /employees/{id}")
    class FindById {

        @Test
        @DisplayName("returns 200 when the employee exists")
        void ok() throws Exception {
            when(service.findById(1L)).thenReturn(juanResponse().build());

            mvc.perform(get("/employees/1").with(AUTH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 404 with a problem body when it does not exist")
        void notFound() throws Exception {
            when(service.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

            mvc.perform(get("/employees/99").with(AUTH))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail", is("Employee with id 99 was not found")))
                    .andExpect(jsonPath("$.instance", is("/employees/99")))
                    .andExpect(jsonPath("$.requestId").exists());
        }

        @Test
        @DisplayName("returns 400 when the id is not a number")
        void invalidId() throws Exception {
            mvc.perform(get("/employees/abc").with(AUTH))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("Parameter 'id' has an invalid value: 'abc'")));
        }
    }

    @Nested
    @DisplayName("GET /employees/search")
    class Search {

        @Test
        @DisplayName("returns 200 with the matches")
        void ok() throws Exception {
            when(service.searchByName("per")).thenReturn(List.of(juanResponse().build()));

            mvc.perform(get("/employees/search").param("name", "per").with(AUTH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].paternalLastName", is("Pérez")));
        }

        @Test
        @DisplayName("returns 400 when name is missing")
        void missing() throws Exception {
            mvc.perform(get("/employees/search").with(AUTH))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("Required parameter 'name' is missing")));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void blank() throws Exception {
            mvc.perform(get("/employees/search").param("name", "  ").with(AUTH))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field", is("name")));
            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /employees")
    class Create {

        @Test
        @DisplayName("accepts an array and returns 201 with the created employees")
        void createsArray() throws Exception {
            when(service.createAll(anyList()))
                    .thenReturn(List.of(juanResponse().build(), juanResponse().id(2L).firstName("Ana").build()));

            mvc.perform(json(post("/employees"), "[" + VALID_EMPLOYEE + "," + VALID_EMPLOYEE + "]"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("accepts a single object too and parses dd-MM-yyyy dates")
        void createsSingleObject() throws Exception {
            when(service.createAll(anyList())).thenReturn(List.of(juanResponse().build()));
            ArgumentCaptor<List<EmployeeRequest>> captor = ArgumentCaptor.forClass(List.class);

            mvc.perform(json(post("/employees"), VALID_EMPLOYEE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(service).createAll(captor.capture());
            assertThat(captor.getValue())
                    .singleElement()
                    .satisfies(request -> assertThat(request.birthDate())
                            .isEqualTo(BIRTH_DATE));
        }

        @Test
        @DisplayName("returns 400 listing every invalid field, with its position in the batch")
        void invalidFields() throws Exception {
            String invalid = """
                    {"firstName":"","paternalLastName":"P3rez","age":15,"birthDate":"15-03-1994","position":"Dev"}""";

            mvc.perform(json(post("/employees"), "[" + VALID_EMPLOYEE + "," + invalid + "]"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("One or more fields are invalid")))
                    .andExpect(
                            jsonPath("$.errors[?(@.field == 'requests[1].age')].message").value("must be at least 18"))
                    .andExpect(jsonPath("$.errors[?(@.field == 'requests[1].sex')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field == 'requests[1].paternalLastName')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field == 'requests[1].firstName')]").exists());
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 for an empty array")
        void emptyArray() throws Exception {
            mvc.perform(json(post("/employees"), "[]"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].message", is("at least one employee is required")));
        }

        @Test
        @DisplayName("returns 400 when the date does not follow dd-MM-yyyy")
        void wrongDateFormat() throws Exception {
            mvc.perform(json(post("/employees"), VALID_EMPLOYEE.replace("15-03-1994", "1994-03-15")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(matchesPattern(".*dd-MM-yyyy.*")));
        }

        @Test
        @DisplayName("returns 400 for an unknown sex value")
        void wrongSex() throws Exception {
            mvc.perform(json(post("/employees"), VALID_EMPLOYEE.replace("MALE", "M")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 for unknown fields instead of silently ignoring them")
        void unknownField() throws Exception {
            mvc.perform(json(post("/employees"), VALID_EMPLOYEE.replace("\"firstName\"", "\"id\":5,\"firstName\"")))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 415 when the body is not JSON")
        void notJson() throws Exception {
            mvc.perform(post("/employees").with(AUTH).contentType(MediaType.TEXT_PLAIN).content("hello"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("PUT /employees/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with the updated employee for a partial body")
        void ok() throws Exception {
            when(service.update(eq(1L), any(EmployeeUpdateRequest.class)))
                    .thenReturn(juanResponse().position("Tech Lead").build());

            mvc.perform(json(put("/employees/1"), "{\"position\":\"Tech Lead\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.position", is("Tech Lead")));
        }

        @Test
        @DisplayName("returns 400 when a sent field is invalid")
        void invalid() throws Exception {
            mvc.perform(json(put("/employees/1"), "{\"age\":150,\"position\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasSize(2)));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 404 when the employee does not exist")
        void notFound() throws Exception {
            when(service.update(eq(99L), any())).thenThrow(new EmployeeNotFoundException(99L));

            mvc.perform(json(put("/employees/99"), "{\"age\":40}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /employees/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 when deleted")
        void ok() throws Exception {
            mvc.perform(delete("/employees/1").with(AUTH))
                    .andExpect(status().isNoContent());
            verify(service).delete(1L);
        }

        @Test
        @DisplayName("returns 404 when the employee does not exist")
        void notFound() throws Exception {
            doThrow(new EmployeeNotFoundException(99L)).when(service).delete(99L);

            mvc.perform(delete("/employees/99").with(AUTH))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 405 for an unsupported method on the collection")
        void methodNotAllowed() throws Exception {
            mvc.perform(delete("/employees").with(AUTH))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
