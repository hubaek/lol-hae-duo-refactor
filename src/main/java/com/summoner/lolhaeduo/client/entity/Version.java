package com.summoner.lolhaeduo.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String versionNumber;

    private Version(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public static Version of(String versionNumber) {
        return new Version(versionNumber);
    }
}
