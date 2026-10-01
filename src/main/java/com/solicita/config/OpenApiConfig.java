package com.solicita.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Solicita API", version = "1.0.0", description = "API para gerenciamento de solicitações."))
public class OpenApiConfig {

}
