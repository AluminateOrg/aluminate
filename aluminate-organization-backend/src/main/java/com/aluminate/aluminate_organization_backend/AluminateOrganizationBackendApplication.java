package com.aluminate.aluminate_organization_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class AluminateOrganizationBackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AluminateOrganizationBackendApplication.class);
		Environment env = app.run(args).getEnvironment();
		String port = env.getProperty("server.port", "8098");
		System.out.println("✅ Aluminate Organization Backend is running on port: " + port);
	}
}
