package com.rebay.rebay_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RebayBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebayBackendApplication.class, args);
	}

}
