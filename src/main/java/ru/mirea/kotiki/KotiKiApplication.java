package ru.mirea.kotiki;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "KOTIKI API",
		version = "1.0",
		description = "API for the social network KOTIKI"))
public class KotiKiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KotiKiApplication.class, args);
	}
}