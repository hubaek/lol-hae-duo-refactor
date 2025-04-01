package com.summoner.lolhaeduo.domain.account.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class RiotAccountInfo {

    private String puuid;
    private String encryptedAccountId;
    private String encryptedSummonerId;

    private RiotAccountInfo(String puuid, String encryptedAccountId, String encryptedSummonerId) {
        this.puuid = puuid;
        this.encryptedAccountId = encryptedAccountId;
        this.encryptedSummonerId = encryptedSummonerId;
    }

    public static RiotAccountInfo fromRiotApi(String puuid, String encryptedAccountId, String encryptedSummonerId) {
        return new RiotAccountInfo(puuid, encryptedAccountId, encryptedSummonerId);
    }
}
