package com.hs.fastService.enums;

import com.hs.fastService.ErrorMsg;

/**
 * 错误码枚举
 */
public enum BaseErrorMsg implements ErrorMsg {

    // =======  通用错误码 -1 -  -1000 ========
    CHECK_FAILURE(-1, "信息校验失败"),

    NOT_REQUIRED_PARAMETER(-2, "缺少必须参数"),

    // 数据已存在，（唯一约束冲突）
    DATA_ALREADY_EXISTED(-3, "数据已存在"),

    DATA_NOT_EXISTED(-4, "数据不存在"),

    SERVICE_NOT_EXISITED(-5, "服务不存在"),

    SERVER_ERROR(-500, "服务器错误");


    public int code;
    public String message;

    BaseErrorMsg(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
