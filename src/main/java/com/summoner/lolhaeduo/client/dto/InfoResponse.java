package com.summoner.lolhaeduo.client.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class InfoResponse {
    private final List<ParticipantsResponse> participants;
}
