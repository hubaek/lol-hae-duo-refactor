package com.summoner.lolhaeduo.domain.account.repository;

public interface AccountQuerydslRepository {
    boolean isMemberAccountLimitExceeded(Long memberId);
}
