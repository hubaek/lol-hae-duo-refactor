package com.summoner.lolhaeduo.common.limiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket riotApiBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(20)
                                .refillGreedy(20, Duration.ofSeconds(1)) // 1초에 20개
                                .build()
                )
                .addLimit(
                        Bandwidth.builder()
                                .capacity(100)
                                .refillGreedy(100, Duration.ofMinutes(2)) // 2분에 100개
                                .build()
                )
                .build();
    }
}
