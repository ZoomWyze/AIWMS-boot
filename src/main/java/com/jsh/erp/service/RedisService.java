package com.jsh.erp.service;


/**
 * Redis 缓存 Service
 * 提供 Redis 缓存的操作封装：设置/获取/删除/过期管理
 *
 * @author jishenghua
 */
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author jisheng hua
 * @Date: 2021/1/28 18:10
 */
@Component
public class RedisService {

    @Resource
    public RedisTemplate redisTemplate;

    public static final String ACCESS_TOKEN = "X-Access-Token";

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        this.redisTemplate = redisTemplate;
    }

    /**
     * @author jisheng hua
     * description:
     *  浠巗ession涓幏鍙栦俊鎭?     *@date: 2021/1/28 18:10
     * @Param: request
     * @Param: key
     * @return Object
     */
    public Object getObjectFromSessionByKey(HttpServletRequest request, String key){
        Object obj=null;
        if(request==null){
            return null;
        }
        String token = request.getHeader(ACCESS_TOKEN);
        if(token!=null) {
            //寮€鍚痳edis锛岀敤鎴锋暟鎹斁鍦╮edis涓紝浠巖edis涓幏鍙?            if(redisTemplate.opsForHash().hasKey(token,key)){
                //redis涓瓨鍦紝鎷垮嚭鏉ヤ娇鐢?                obj=redisTemplate.opsForHash().get(token,key);
                redisTemplate.expire(token, BusinessConstants.MAX_SESSION_IN_SECONDS, TimeUnit.SECONDS);
            }
        }
        return obj;
    }

    /**
     * 鑾峰緱缂撳瓨鐨勫熀鏈璞°€?     *
     * @param key 缂撳瓨閿€?     * @return 缂撳瓨閿€煎搴旂殑鏁版嵁
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * @author jisheng hua
     * description:
     *  灏嗕俊鎭斁鍏ession鎴栬€卹edis涓?     *@date: 2021/1/28 18:10
     * @Param: request
     * @Param: key
     * @Param: obj
     * @return
     */
    public void storageObjectBySession(String token, String key, Object obj) {
        //寮€鍚痳edis锛岀敤鎴锋暟鎹斁鍒皉edis涓?        redisTemplate.opsForHash().put(token, key, obj.toString());
        redisTemplate.expire(token, BusinessConstants.MAX_SESSION_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * @author jisheng hua
     *  description:
     *  灏嗕俊鎭斁鍏ession鎴栬€卹edis涓?     * @date: 2024/5/28 20:10
     * @return
     */
    public void storageCaptchaObject(String verifyKey, String codeNum) {
        //鎶婇獙璇佺爜鏀惧埌redis涓?        redisTemplate.opsForValue().set(verifyKey, codeNum, BusinessConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
    }

    /**
     * 甯︽湁鏁堟椂闂寸紦瀛樻暟鎹?     * @param key
     * @param value
     * @param time 鍗曚綅绉?     */
    public void storageKeyWithTime(String key, String value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 鍒犻櫎鍗曚釜瀵硅薄
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * @author jisheng hua
     * description:
     *  灏嗕俊鎭粠session鎴栬€卹edis涓Щ闄?     *@date: 2021/1/28 18:10
     * @Param: request
     * @Param: key
     * @Param: obj
     * @return
     */
    public void deleteObjectBySession(HttpServletRequest request, String key){
        if(request!=null){
            String token = request.getHeader(ACCESS_TOKEN);
            if(StringUtil.isNotEmpty(token)){
                //寮€鍚痳edis锛岀敤鎴锋暟鎹斁鍦╮edis涓紝浠巖edis涓垹闄?                redisTemplate.opsForHash().delete(token, key);
            }
        }
    }

    /**
     * @author jisheng hua
     * 灏嗕俊鎭粠redis涓Щ闄わ紝姣斿user鍜宨p
     * @param userId
     * @param clientIp
     */
    public void deleteObjectByUserAndIp(Long userId, String clientIp){
        Set<String> tokens = redisTemplate.keys("*");
        for(String token : tokens) {
            // 妫€鏌ラ敭鏄惁瀛樺湪涓斾负鍝堝笇绫诲瀷
            if (redisTemplate.hasKey(token) && redisTemplate.type(token) == DataType.HASH) {
                Object userIdValue = redisTemplate.opsForHash().get(token, "userId");
                Object clientIpValue = redisTemplate.opsForHash().get(token, "clientIp");
                if(userIdValue!=null && clientIpValue!=null && userIdValue.equals(userId.toString()) && clientIpValue.equals(clientIp)) {
                    redisTemplate.opsForHash().delete(token, "userId");
                }
            }
        }
    }

    /**
     * @author jisheng hua
     * 灏嗕俊鎭粠redis涓Щ闄わ紝姣斿user
     * @param userId
     */
    public void deleteObjectByUser(Long userId){
        Set<String> tokens = redisTemplate.keys("*");
        for(String token : tokens) {
            // 妫€鏌ラ敭鏄惁瀛樺湪涓斾负鍝堝笇绫诲瀷
            if (redisTemplate.hasKey(token) && redisTemplate.type(token) == DataType.HASH) {
                Object userIdValue = redisTemplate.opsForHash().get(token, "userId");
                if(userIdValue!=null && userIdValue.equals(userId.toString())) {
                    redisTemplate.opsForHash().delete(token, "userId");
                }
            }
        }
    }
}
