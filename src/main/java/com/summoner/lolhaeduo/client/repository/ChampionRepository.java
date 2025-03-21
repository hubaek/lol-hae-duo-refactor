package com.summoner.lolhaeduo.client.repository;

import com.summoner.lolhaeduo.client.entity.Champion;
import com.summoner.lolhaeduo.client.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface ChampionRepository extends JpaRepository<Champion, Long> {
    @Query("SELECT c.id FROM Champion c")
    Set<String> findAllIds();
}
