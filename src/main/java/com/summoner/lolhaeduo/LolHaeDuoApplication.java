package com.summoner.lolhaeduo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class LolHaeDuoApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LolHaeDuoApplication.class);
		app.setAdditionalProfiles("redis");
		app.run(args);
	}

}
