package com.summoner.lolhaeduo.common.dto;

import lombok.Getter;

@Getter
public class PageDataResponse<T> {
	private T data;

	private PageDataResponse(T data) {
		this.data = data;
	}

	public static <T> PageDataResponse<T> of(T data) {
		return new PageDataResponse<T>(data);
	}
}
