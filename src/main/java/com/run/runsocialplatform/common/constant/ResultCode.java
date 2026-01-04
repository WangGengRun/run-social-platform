package com.run.runsocialplatform.common.constant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    // 认证相关
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    TOKEN_INVALID(40101, "Token无效"),
    TOKEN_EXPIRED(40102, "Token已过期"),

    // 用户相关
    USER_NOT_EXIST(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已被禁用"),
    USERNAME_EXISTS(10003, "用户名已存在"),
    EMAIL_EXISTS(10004, "邮箱已存在"),
    PHONE_EXISTS(10005, "手机号已存在"),
    PASSWORD_ERROR(10006, "密码错误"),

    // 校友认证相关
    ALUMNI_NOT_VERIFIED(11001, "校友身份未认证"),
    ALUMNI_VERIFY_PENDING(11002, "校友身份待审核"),
    ALUMNI_VERIFY_REJECTED(11003, "校友身份审核不通过"),
    STUDENT_ID_EXISTS(11004, "学号已存在"),

    // 参数校验
    PARAM_ERROR(400, "参数错误"),
    PARAM_VALIDATE_FAILED(40001, "参数校验失败"),

    // 系统错误
    SYSTEM_ERROR(500, "系统错误"),
    SERVICE_ERROR(50001, "服务异常"),
    DATABASE_ERROR(50002, "数据库异常"),
    REDIS_ERROR(50003, "Redis异常"),

    // 业务错误
    OPERATION_FAILED(60001, "操作失败"),
    DATA_NOT_EXIST(60002, "数据不存在"),
    DATA_EXISTS(60003, "数据已存在"),
    REQUEST_LIMIT(60004, "请求过于频繁"),

    // 文件相关
    FILE_UPLOAD_FAILED(70001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(70002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(70003, "文件大小超出限制");

    private final Integer code;
    private final String message;
}