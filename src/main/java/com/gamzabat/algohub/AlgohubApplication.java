package com.gamzabat.algohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AlgohubApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgohubApplication.class, args);
	}

}
