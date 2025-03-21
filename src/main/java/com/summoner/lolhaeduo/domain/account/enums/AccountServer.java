package com.summoner.lolhaeduo.domain.account.enums;

import lombok.Getter;

@Getter
public enum AccountServer {
    BR1(AccountRegion.AMERICAS),
    EUN1(AccountRegion.EUROPE),
    EUW1(AccountRegion.EUROPE),
    JP1(AccountRegion.ASIA),
    KR(AccountRegion.ASIA),
    LA1(AccountRegion.AMERICAS),
    LA2(AccountRegion.AMERICAS),
    ME1(AccountRegion.ASIA),
    NA1(AccountRegion.AMERICAS),
    OC1(AccountRegion.AMERICAS),
    PH2(AccountRegion.ASIA),
    RU(AccountRegion.EUROPE),
    SG2(AccountRegion.ASIA),
    TH2(AccountRegion.ASIA),
    TR1(AccountRegion.EUROPE),
    TW2(AccountRegion.ASIA),
    VN2(AccountRegion.ASIA);

    private final AccountRegion region;

    AccountServer(AccountRegion region) {
        this.region = region;
    }

}
