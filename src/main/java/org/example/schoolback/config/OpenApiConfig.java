package org.example.schoolback.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolBackOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("School Back API")
                .description("API documentation for School Back")
                .version("v1"));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserOnStartup() {
        String swaggerUrl = "http://localhost:8080/swagger-ui/index.html";
        String os = System.getProperty("os.name").toLowerCase();
        String[] command;
        if (os.contains("win")) {
            command = new String[]{"cmd", "/c", "start", swaggerUrl};
        } else if (os.contains("mac")) {
            command = new String[]{"open", swaggerUrl};
        } else {
            command = new String[]{"xdg-open", swaggerUrl};
        }
        try {
            Runtime.getRuntime().exec(command);
            System.out.println("Swagger UI opened: " + swaggerUrl);
        } catch (IOException e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }
}


