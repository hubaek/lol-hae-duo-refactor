package com.summoner.lolhaeduo.domain.account.dto;

import lombok.Getter;

@Getter
public class LinkAccountResponse {
    private Long accountId;

    private LinkAccountResponse(Long accountId) {
        this.accountId = accountId;
    }

    public static LinkAccountResponse of(Long accountId) {
        return new LinkAccountResponse(accountId);
    }
}
