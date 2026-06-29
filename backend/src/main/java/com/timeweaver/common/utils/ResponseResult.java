package com.timeweaver.common.utils;

import com.timeweaver.common.constant.ApiCode;
import lombok.Data;

@Data
public class ResponseResult<T> {

    private int code;
    private String message;
    private T data;

    private ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage(), null);
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(ApiCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResponseResult<T> created(T data) {
        return new ResponseResult<>(ApiCode.CREATED.getCode(), ApiCode.CREATED.getMessage(), data);
    }

    public static <T> ResponseResult<T> failed(ApiCode apiCode) {
        return new ResponseResult<>(apiCode.getCode(), apiCode.getMessage(), null);
    }

    public static <T> ResponseResult<T> failed(ApiCode apiCode, String message) {
        return new ResponseResult<>(apiCode.getCode(), message, null);
    }

    public static <T> ResponseResult<T> failed(int code, String message) {
        return new ResponseResult<>(code, message, null);
    }
}
