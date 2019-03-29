package com.hs.fastService.exceptions;


import com.hs.fastService.ErrorMsg;

public class ResponseException extends RuntimeException {

    private int code;
    private String message;

    public ResponseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ResponseException(ErrorMsg errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
