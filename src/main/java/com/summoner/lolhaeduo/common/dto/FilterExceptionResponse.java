package com.summoner.lolhaeduo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class FilterExceptionResponse {
    private final HttpStatus httpStatus;
    private final String message;

    public static ResponseEntity<FilterExceptionResponse> exceptionResponseFrom(HttpStatus httpStatus, String message) {
        FilterExceptionResponse exceptionResponse = new FilterExceptionResponse(httpStatus, message);
        return new ResponseEntity<FilterExceptionResponse>(exceptionResponse, httpStatus);
    }
}
