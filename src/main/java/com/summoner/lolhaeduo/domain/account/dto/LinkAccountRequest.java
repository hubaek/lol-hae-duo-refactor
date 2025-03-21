package com.summoner.lolhaeduo.domain.account.dto;

import com.summoner.lolhaeduo.domain.account.enums.AccountServer;
import com.summoner.lolhaeduo.domain.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LinkAccountRequest {

    @NotNull
    private AccountType accountType;

    @NotNull
    private String accountUsername;

    @NotNull
    private String accountPassword;

    @NotNull
    private String summonerName;

    @NotNull
    private String tagLine;

    @NotNull
    private AccountServer server;
}
