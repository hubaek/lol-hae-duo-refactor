package com.summoner.lolhaeduo.client.controller;

import com.summoner.lolhaeduo.client.riot.DataDragonScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RiotClientController {
    private final DataDragonScheduler dataDragonScheduler;

    @PostMapping("/ddragon")
    public ResponseEntity<Void> updateDataDragonInfo() {
        dataDragonScheduler.updateDataDragonInfo();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
