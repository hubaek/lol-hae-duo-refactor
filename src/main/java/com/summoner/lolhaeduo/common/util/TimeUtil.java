package com.summoner.lolhaeduo.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class TimeUtil {
    public long convertToEpochSeconds(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime is null");
        }
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
