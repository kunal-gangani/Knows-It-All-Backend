package com.example.knowitall_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.knowitall_backend.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class KnowitallBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(KnowitallBackendApplication.class, args);
	}
}