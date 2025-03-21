package com.summoner.lolhaeduo.domain.duo.repository;

import com.summoner.lolhaeduo.domain.duo.entity.Duo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuoRepository extends JpaRepository<Duo, Long> , DuoCustomRepository {
}

