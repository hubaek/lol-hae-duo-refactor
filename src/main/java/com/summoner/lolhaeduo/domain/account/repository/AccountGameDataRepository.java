package com.summoner.lolhaeduo.domain.account.repository;

import com.summoner.lolhaeduo.domain.account.entity.AccountGameData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountGameDataRepository extends JpaRepository<AccountGameData, Long> {
}
