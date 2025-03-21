package com.summoner.lolhaeduo.client.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ProfileIcon {

    @Id
    private int id;

    private ProfileIcon(int id) {
        this.id = id;
    }

    public static ProfileIcon of(int id) {
        return new ProfileIcon(id);
    }
}
