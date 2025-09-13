package com.example.bulksmsAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BulksmsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulksmsApiApplication.class, args);
	}

}
