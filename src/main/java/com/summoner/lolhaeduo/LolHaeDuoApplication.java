package com.summoner.lolhaeduo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LolHaeDuoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LolHaeDuoApplication.class, args);
	}

}
