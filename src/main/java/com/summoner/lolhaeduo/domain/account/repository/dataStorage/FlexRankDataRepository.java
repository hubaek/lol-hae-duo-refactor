package com.summoner.lolhaeduo.domain.account.repository.dataStorage;

import com.summoner.lolhaeduo.domain.account.entity.dataStorage.FlexRankData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlexRankDataRepository extends JpaRepository<FlexRankData, Long> {
}
