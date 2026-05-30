package com.jsh.erp.exception;

/**
 * 业务参数校验异常
 *
 * 当业务方法接收到不合法的参数时抛出此异常，
 * 由 GlobalExceptionHandler 统一捕获并返回前端友好的错误信息。
 *
 * @author jishenghua
 */
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class BusinessParamCheckingException extends Exception {

    private static final long serialVersionUID = 1L;
    private int code;
    private Map<String, Object> data;

    public BusinessParamCheckingException(int code, String reason) {
        super(reason);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("message", reason);
        this.code = code;
        this.data = objectMap;
    }

    public BusinessParamCheckingException(int code, String reason, Throwable throwable) {
        super(reason, throwable);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("message", reason);
        this.code = code;
        this.data = objectMap;
    }
}
