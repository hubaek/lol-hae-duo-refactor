package com.summoner.lolhaeduo.domain.account.repository.dataStorage;

import com.summoner.lolhaeduo.domain.account.entity.dataStorage.QuickGameData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickGameDataRepository extends JpaRepository<QuickGameData, Long> {
}
