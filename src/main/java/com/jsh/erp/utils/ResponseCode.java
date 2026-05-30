package com.jsh.erp.utils;


/**
 * 响应码枚举类
 * 定义接口统一响应的状态码枚举（成功、失败、未登录等）
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author jishenghua qq752718920  2018-10-7 15:26:27
 */
public class ResponseCode {

    public final int code;
    public final Object data;

    /**
     *
     * @param code
     * @param data
     */
    @JSONCreator
    public ResponseCode(@JSONField(name = "code") int code, @JSONField(name = "data")Object data) {
        this.code = code;
        this.data = data;
    }
}