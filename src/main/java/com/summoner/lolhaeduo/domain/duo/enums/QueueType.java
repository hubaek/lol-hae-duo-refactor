package com.summoner.lolhaeduo.domain.duo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {
	// QUICK 은 어떤 값으로 넘겨주는 찾아야함
	QUICK("QUICK", 490),
	SOLO("RANKED_SOLO_5x5", 420),
	FLEX("RANKED_FLEX_SR", 440);

	private final String queueType;
	private final Integer queueId;

	public static QueueType fromRiotQueueType(String queueType) {
		for (QueueType type : values()) {
			if (type.queueType.equalsIgnoreCase(queueType)) {
				return type;
			}
		}
			throw new IllegalArgumentException("Unknown QueueType: " + queueType);
		}
	}

