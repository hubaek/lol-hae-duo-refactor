package com.summoner.lolhaeduo.client.repository;

import com.summoner.lolhaeduo.client.entity.Champion;
import com.summoner.lolhaeduo.client.entity.ProfileIcon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileIconRepository extends JpaRepository<ProfileIcon, Long> {
}
