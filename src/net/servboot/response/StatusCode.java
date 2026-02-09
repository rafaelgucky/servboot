package net.servboot.response;

import java.util.Arrays;

public enum StatusCode {
    OK(200),
    BadRequest(400),
    NotFound(404),
    InternalServerError(500);

    private final int code;

    StatusCode(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    public static StatusCode getFromCode(int code){
        return Arrays.stream(StatusCode.values()).filter(x -> x.getCode() == code).findFirst().orElse(null);
    }
}
