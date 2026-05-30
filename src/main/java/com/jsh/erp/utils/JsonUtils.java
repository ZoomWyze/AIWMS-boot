package com.jsh.erp.utils;


/**
 * JSON 工具类
 * 提供 JSON 序列化/反序列化的工具方法，封装 Jackson 操作
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by jishenghua 2018-5-11 09:48:08
 *
 * @author jishenghua
 */
public class JsonUtils {

    public static JSONObject ok(){
        JSONObject obj = new JSONObject();
        JSONObject tmp = new JSONObject();
        tmp.put("message", "鎴愬姛");
        obj.put("code", 200);
        obj.put("data", tmp);
        return obj;
    }

}
