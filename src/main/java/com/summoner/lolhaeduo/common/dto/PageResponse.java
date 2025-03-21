package com.summoner.lolhaeduo.common.dto;

import java.util.List;

import org.springframework.data.domain.Pageable;

import lombok.Getter;

@Getter
public class PageResponse<T> {
	private List<T> data;
	private int page;
	private int size;
	private int totalPage;

	private PageResponse(List<T> data, int page, int size, int totalPage) {
		this.data = data;
		this.page = page;
		this.size = size;
		this.totalPage = totalPage;
	}

	public static <T> PageResponse<T> of(List<T> data, Pageable pageable, int totalPage) {
		return new PageResponse<T>(data, pageable.getPageNumber() + 1, pageable.getPageSize(), totalPage);
	}
}
