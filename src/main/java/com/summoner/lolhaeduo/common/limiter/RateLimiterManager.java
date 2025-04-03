package com.summoner.lolhaeduo.common.limiter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimiterManager {

    private final Bucket riotApiBucket;

    public boolean tryConsume() {
        return riotApiBucket.tryConsume(1);
    }

    public ConsumptionProbe probe() {
        return riotApiBucket.tryConsumeAndReturnRemaining(1);
    }
}
