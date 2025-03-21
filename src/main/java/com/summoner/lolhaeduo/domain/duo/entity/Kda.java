package com.summoner.lolhaeduo.domain.duo.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Kda {

    private double averageKills;
    private double averageAssists;
    private double averageDeaths;

    private Kda(double averageKills, double averageAssists, double averageDeaths) {
        this.averageKills = averageKills;
        this.averageAssists = averageAssists;
        this.averageDeaths = averageDeaths;
    }

    public static Kda of(double averageKills, double averageAssists, double averageDeaths) {
        return new Kda(averageKills, averageAssists, averageDeaths);
    }
}
