package com.timeweaver.common.exception;

import com.timeweaver.common.constant.ApiCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.code = apiCode.getCode();
    }

    public BusinessException(ApiCode apiCode, String message) {
        super(message);
        this.code = apiCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
