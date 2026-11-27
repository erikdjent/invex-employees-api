package com.invex.employees.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Employees API", version = "1.0.0", description = "Microservice to manage employees: CRUD operations, batch creation and search by name. "
                + "Dates use the format dd-MM-yyyy. All endpoints require HTTP Basic authentication.", contact = @Contact(name = "Erik")))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
