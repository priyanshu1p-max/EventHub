package com.flowtech.eventhub_spring_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class EventhubSpringBackendApplication {

	public static void main(String[] args) {
		
		ConfigurableApplicationContext applicationContext= SpringApplication.run(EventhubSpringBackendApplication.class, args);
		
		ConfigurableEnvironment environment=applicationContext.getEnvironment();
		System.out.println("event hub is running in: "+environment.getProperty("server.port"));
	}

}
