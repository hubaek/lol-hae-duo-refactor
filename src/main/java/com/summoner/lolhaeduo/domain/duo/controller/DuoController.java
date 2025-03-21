package com.summoner.lolhaeduo.domain.duo.controller;

import com.summoner.lolhaeduo.common.annotation.Auth;
import com.summoner.lolhaeduo.common.dto.AuthMember;
import com.summoner.lolhaeduo.common.dto.PageResponse;
import com.summoner.lolhaeduo.domain.duo.dto.*;
import com.summoner.lolhaeduo.domain.duo.enums.Lane;
import com.summoner.lolhaeduo.domain.duo.enums.QueueType;
import com.summoner.lolhaeduo.domain.duo.service.DuoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/duo")
@RequiredArgsConstructor
public class DuoController {

    private final DuoService duoService;

    @GetMapping()
    public ResponseEntity<PageResponse<DuoListResponse>> getDuoList(
        @RequestParam(required = false) QueueType queueType,
        @RequestParam(required = false) Lane lane,
        @RequestParam(required = false) String tier,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ){
        PageResponse<DuoListResponse> duoList = duoService.getPagedDuoList(queueType, lane, tier,page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(duoList);
    }

    @PostMapping("")
    public ResponseEntity<DuoCreateResponse> createDuo(@Valid @RequestBody DuoCreateRequest createRequest, @Auth AuthMember authMember) {
        if (!createRequest.isQuickQueueTypeValid()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        DuoCreateResponse duoCreateResponse = duoService.createDuo(createRequest, authMember.getMemberId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(duoCreateResponse);
    }

    @PutMapping("/{duoId}")
    public ResponseEntity<DuoUpdateResponse> updateDuo(
            @Auth AuthMember authMember,
            @PathVariable Long duoId,
            @Valid @RequestBody DuoUpdateRequest duoUpdateRequest) {
        if (!duoUpdateRequest.isQuickQueueTypeValid()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        DuoUpdateResponse response = duoService.updateDuo(authMember.getMemberId(), duoId, duoUpdateRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{duoId}")
    public ResponseEntity<Void> deleteDuo(@PathVariable Long duoId, @Auth AuthMember authMember) {
        duoService.deleteDuoById(duoId, authMember);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
