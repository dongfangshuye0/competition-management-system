package com.tangqilin.competition.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return of(200, "操作成功", data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        return of(500, message, null);
    }

    public static <T> Result<T> failure(Integer code, String message) {
        return of(code, message, null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return failure(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return failure(403, message);
    }

    public static <T> Result<T> of(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
