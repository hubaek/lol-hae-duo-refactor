package com.summoner.lolhaeduo.client.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Champion {

    @Id
    private String id;

    private String name;

    private Champion(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Champion of(String id, String name) {
        return new Champion(id, name);
    }
}
