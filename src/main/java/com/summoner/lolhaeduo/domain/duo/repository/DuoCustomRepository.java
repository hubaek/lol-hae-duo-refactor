package com.summoner.lolhaeduo.domain.duo.repository;

import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DuoCustomRepository {
    Page<Duo> findDuosByCondition(QueueType queueType, Lane lane, String tier, Pageable pageable);
}
