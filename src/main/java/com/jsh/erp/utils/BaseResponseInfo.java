package com.jsh.erp.utils;


/**
 * 基础响应信息类
 * 封装接口统一响应格式，包含状态码（code）和消息（info）
 *
 * @author jishenghua
 */
public class BaseResponseInfo {
	public int code;
	public Object data;
	
	public BaseResponseInfo() {
		code = 400;
		data = null;
	}
}
