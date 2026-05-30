package com.jsh.erp.utils;


/**
 * 响应 JSON 工具类
 * 提供接口响应 JSON 的构建工具方法
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ResponseJsonUtil {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    /**
     * 鍝嶅簲杩囨护鍣?     */
    public static final class ResponseFilter extends ExtJsonUtils.ExtFilter implements ValueFilter {
        @Override
        public Object process(Object object, String name, Object value) {
            if (name.equals("createTime") || name.equals("modifyTime")||name.equals("updateTime")) {
                return value;
            } else if (value instanceof Date) {
                return FORMAT.format(value);
            } else {
                return value;
            }
        }
    }

    /**
     * 鎴愬姛鐨刯son涓?     * @param responseCode
     * @return
     */
    public static String backJson(ResponseCode responseCode) {
        if (responseCode != null) {
            return JSON.toJSONString(responseCode, new ResponseFilter(),
                    SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteNonStringKeyAsString);
        }
        return null;
    }

    public static String returnJson(Map<String, Object> map, String message, int code) {
        map.put("message", message);
        return backJson(new ResponseCode(code, map));
    }

    public static String returnStr(Map<String, Object> objectMap, int res) {
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else if(res == -1) {
            return returnJson(objectMap, ErpInfo.TEST_USER.name, ErpInfo.TEST_USER.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }
}
