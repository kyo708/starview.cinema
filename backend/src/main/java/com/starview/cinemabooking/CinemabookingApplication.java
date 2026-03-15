package com.starview.cinemabooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemabookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemabookingApplication.class, args);
	}

}
