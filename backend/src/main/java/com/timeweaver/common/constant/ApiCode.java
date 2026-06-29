package com.timeweaver.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被注册"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    INVALID_TOKEN(1005, "Token 无效"),
    TOKEN_EXPIRED(1006, "Token 已过期"),
    CATEGORY_NOT_FOUND(2001, "分类不存在"),
    RECORD_NOT_FOUND(3001, "记录不存在"),
    RECORD_ONGOING_EXISTS(3002, "已有进行中的记录"),
    GOAL_NOT_FOUND(4001, "目标不存在"),
    REPORT_NOT_FOUND(5001, "周报不存在"),
    REPORT_GENERATING(5002, "周报正在生成中");

    private final int code;
    private final String message;
}
