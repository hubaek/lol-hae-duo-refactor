package com.summoner.lolhaeduo.domain.account.repository.dataStorage;

import com.summoner.lolhaeduo.domain.account.entity.dataStorage.SoloRankData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoloRankDataRepository extends JpaRepository<SoloRankData, Long> {
}
