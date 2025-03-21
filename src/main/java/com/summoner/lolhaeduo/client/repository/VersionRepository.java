package com.summoner.lolhaeduo.client.repository;

import com.summoner.lolhaeduo.client.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VersionRepository extends JpaRepository<Version, Long> {

    @Query("SELECT v FROM Version v ORDER BY v.id DESC")
    Version findLatestVersion();
}
