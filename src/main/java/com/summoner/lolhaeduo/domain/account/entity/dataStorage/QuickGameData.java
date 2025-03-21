package com.summoner.lolhaeduo.domain.account.entity.dataStorage;

import com.summoner.lolhaeduo.domain.duo.entity.Kda;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class QuickGameData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int wins;

    private int totalGames;

    private Kda kda;

    private QuickGameData(int wins, int totalGames, Kda kda) {
        this.wins = wins;
        this.totalGames = totalGames;
        this.kda = kda;
    }

    public static QuickGameData of(int wins, int totalGames, Kda kda) {
        return new QuickGameData(wins, totalGames, kda);
    }

    public void update(int wins, int totalGames, Kda kda) {
        this.wins = wins;
        this.totalGames = totalGames;
        this.kda = kda;
    }
}