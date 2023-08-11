package com.example.parkingmanager.dto;

import com.example.parkingmanager.constants.Constants;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Response<T> {
    private T body;
    private int httpCode;
    private String message;

    public Response(int httpCode, T body) {
        this.httpCode = httpCode;
        this.body = body;
    }

    public Response(int httpCode, String message) {
        this.httpCode = httpCode;
        this.message = message;
    }

    public Response(int httpCode, String message, T body) {
        this.httpCode = httpCode;
        this.message = message;
        this.body = body;
    }

    public Response(T body) {
        this.body = body;
        this.httpCode = HttpStatus.OK.value();
        this.message = Constants.REQUEST_SUCCESS_MESSAGE;
    }
    public Response(int httpCode) {
        this.httpCode = httpCode;
    }

    public boolean isOk() {
        return this.getHttpCode() == HttpStatus.OK.value();
    }
}
