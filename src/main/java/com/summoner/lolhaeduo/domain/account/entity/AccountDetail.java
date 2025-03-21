package com.summoner.lolhaeduo.domain.account.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class AccountDetail {

    private String puuid;
    private String encryptedAccountId;
    private String encryptedSummonerId;

    private AccountDetail(String puuid, String encryptedAccountId, String encryptedSummonerId) {
        this.puuid = puuid;
        this.encryptedAccountId = encryptedAccountId;
        this.encryptedSummonerId = encryptedSummonerId;
    }

    public static AccountDetail of(String puuid, String encryptedAccountId, String encryptedSummonerId) {
        return new AccountDetail(puuid, encryptedAccountId, encryptedSummonerId);
    }
}
