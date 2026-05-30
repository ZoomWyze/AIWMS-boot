package com.jsh.erp.exception;

/**
 * 业务运行时异常
 *
 * 当业务逻辑执行过程中出现不可恢复的错误时抛出此异常，
 * 继承自 RuntimeException（非受检异常），无需在方法签名中声明。
 * 由 GlobalExceptionHandler 统一捕获并返回错误响应。
 *
 * @author jishenghua
 */
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class BusinessRunTimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private int code;
    private Map<String, Object> data;

    public BusinessRunTimeException(int code, String reason) {
        super(reason);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("message", reason);
        this.code = code;
        this.data = objectMap;
    }

    public BusinessRunTimeException(int code, String reason, Throwable throwable) {
        super(reason, throwable);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("message", reason);
        this.code = code;
        this.data = objectMap;
    }
}
