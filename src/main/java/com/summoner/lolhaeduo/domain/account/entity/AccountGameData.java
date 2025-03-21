package com.summoner.lolhaeduo.domain.account.entity;

import com.summoner.lolhaeduo.common.entity.Timestamped;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.FlexRankData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.QuickGameData;
import com.summoner.lolhaeduo.domain.account.entity.dataStorage.SoloRankData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor
public class AccountGameData extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profileIconIdUrl;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "quick_game_data_id")
    private QuickGameData quickGameData;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "solo_rank_data_id")
    private SoloRankData soloRankData;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "flex_rank_data_id")
    private FlexRankData flexRankData;

    private AccountGameData(String profileIconIdUrl, QuickGameData quickGameData, SoloRankData soloRankData, FlexRankData flexRankData) {
        this.profileIconIdUrl = profileIconIdUrl;
        this.quickGameData = quickGameData;
        this.soloRankData = soloRankData;
        this.flexRankData = flexRankData;
    }

    public static AccountGameData of(String profileIconIdUrl, QuickGameData quickGameData, SoloRankData soloRankData, FlexRankData flexRankData) {
        return new AccountGameData(profileIconIdUrl, quickGameData, soloRankData, flexRankData);
    }

    public void update(String profileIconIdUrl, QuickGameData quickGameData, SoloRankData soloRankData, FlexRankData flexRankData) {
        this.profileIconIdUrl = profileIconIdUrl;
        this.quickGameData = quickGameData;
        this.soloRankData = soloRankData;
        this.flexRankData = flexRankData;
    }
}
