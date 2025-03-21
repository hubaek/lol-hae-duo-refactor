package com.summoner.lolhaeduo.domain.account.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.summoner.lolhaeduo.domain.account.entity.QAccount.account;

@RequiredArgsConstructor
public class AccountQuerydslRepositoryImpl implements AccountQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean isMemberAccountLimitExceeded(Long memberId) {
        long count = queryFactory
                .select(account.count())
                .from(account)
                .where(account.memberId.eq(memberId))
                .fetchOne();

        return count >= 3;
    }
}
