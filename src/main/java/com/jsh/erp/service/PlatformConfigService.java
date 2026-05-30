package com.jsh.erp.service;


/**
 * 平台参数配置 Service
 * 提供平台级配置参数的查询和管理业务逻辑
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.PlatformConfig;
import com.jsh.erp.datasource.entities.PlatformConfigExample;
import com.jsh.erp.datasource.mappers.PlatformConfigMapper;
import com.jsh.erp.datasource.mappers.PlatformConfigMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.HttpClient;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.PermissionUtil;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

@Service
public class PlatformConfigService {
    private Logger logger = LoggerFactory.getLogger(PlatformConfigService.class);

    @Resource
    private UserService userService;

    @Resource
    private RedisService redisService;

    @Resource
    private PlatformConfigMapper platformConfigMapper;

    @Resource
    private PlatformConfigMapperEx platformConfigMapperEx;

    public PlatformConfig getPlatformConfig(long id)throws Exception {
        PlatformConfig result=null;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = platformConfigMapper.selectByPrimaryKey(id);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<PlatformConfig> getPlatformConfig()throws Exception {
        PlatformConfigExample example = new PlatformConfigExample();
        example.createCriteria();
        List<PlatformConfig> list=null;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                list = platformConfigMapper.selectByExample(example);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<PlatformConfig> select(String platformKey)throws Exception {
        List<PlatformConfig> list=null;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                PageUtils.startPage();
                list = platformConfigMapperEx.selectByConditionPlatformConfig(platformKey);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertPlatformConfig(JSONObject obj, HttpServletRequest request) throws Exception{
        PlatformConfig platformConfig = JSONObject.parseObject(obj.toJSONString(), PlatformConfig.class);
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = platformConfigMapper.insertSelective(platformConfig);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updatePlatformConfig(JSONObject obj, HttpServletRequest request) throws Exception{
        PlatformConfig platformConfig = JSONObject.parseObject(obj.toJSONString(), PlatformConfig.class);
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = platformConfigMapper.updateByPrimaryKeySelective(platformConfig);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deletePlatformConfig(Long id, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = platformConfigMapper.deleteByPrimaryKey(id);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeletePlatformConfig(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        PlatformConfigExample example = new PlatformConfigExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = platformConfigMapper.deleteByExample(example);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int updatePlatformConfigByKey(String platformKey, String platformValue)throws Exception {
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                PlatformConfig platformConfig = new PlatformConfig();
                platformConfig.setPlatformValue(platformValue);
                PlatformConfigExample example = new PlatformConfigExample();
                example.createCriteria().andPlatformKeyEqualTo(platformKey);
                result = platformConfigMapper.updateByExampleSelective(platformConfig, example);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public PlatformConfig getInfoByKey(String platformKey)throws Exception {
        PlatformConfig platformConfig = new PlatformConfig();
        try{
            if(platformKey.contains("aliOss") || platformKey.contains("weixin")) {
                platformConfig = null;
            } else {
                PlatformConfigExample example = new PlatformConfigExample();
                example.createCriteria().andPlatformKeyEqualTo(platformKey);
                List<PlatformConfig> list=platformConfigMapper.selectByExample(example);
                if(list!=null && list.size()>0){
                    platformConfig = list.get(0);
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return platformConfig;
    }

    /**
     * 鏍规嵁key鏌ヨ骞冲彴淇℃伅-鍐呴儴涓撶敤鏂规硶
     * @param platformKey
     * @return
     * @throws Exception
     */
    public PlatformConfig getPlatformConfigByKey(String platformKey)throws Exception {
        PlatformConfig platformConfig = new PlatformConfig();
        try{
            PlatformConfigExample example = new PlatformConfigExample();
            example.createCriteria().andPlatformKeyEqualTo(platformKey);
            List<PlatformConfig> list=platformConfigMapper.selectByExample(example);
            if(list!=null && list.size()>0){
                platformConfig = list.get(0);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return platformConfig;
    }

    /**
     * 鑾峰彇寰俊token淇℃伅
     * @return
     * @throws Exception
     */
    public String getAccessToken() throws Exception {
        String accessToken = "";
        if(redisService.getCacheObject("weixinToken")==null) {
            //1-鑾峰彇token
            String weixinToken = getPlatformConfigByKey("weixinUrl").getPlatformValue() + BusinessConstants.WEIXIN_TOKEN;
            String weixinAppid = getPlatformConfigByKey("weixinAppid").getPlatformValue();
            String weixinSecret = getPlatformConfigByKey("weixinSecret").getPlatformValue();
            String url = weixinToken + "?grant_type=client_credential&appid=" + weixinAppid + "&secret=" + weixinSecret;
            JSONObject jsonObject = HttpClient.httpGet(url);
            logger.info("鑾峰彇鍒板井淇oken淇℃伅:{}", jsonObject);
            if (jsonObject != null) {
                accessToken = jsonObject.getString("access_token");
                Long expiresIn = jsonObject.getLong("expires_in") - 10;
                if (StringUtil.isNotEmpty(accessToken)) {
                    //瀛榬edis
                    redisService.storageKeyWithTime("weixinToken", accessToken, expiresIn);
                }
            }
        } else {
            accessToken = redisService.getCacheObject("weixinToken");
        }
        return accessToken;
    }

    /**
     * 鍙戦€侀偖浠?璇ユ柟娉曞皢鍦ㄤ竴涓崟鐙殑绾跨▼涓墽琛?
     * @return
     * @throws Exception
     */
    @Async
    public void sendEmail(String emailFrom, String emailAuthCode, String emailSmtpHost, String toEmail, String emailSubject, String emailBody) {
        // 閰嶇疆閭欢鏈嶅姟鍣ㄥ睘鎬?        Properties properties = new Properties();
        properties.put("mail.smtp.host", emailSmtpHost); // 缃戞槗閭SMTP鏈嶅姟鍣?        properties.put("mail.smtp.port", "465"); // SSL绔彛
        properties.put("mail.smtp.auth", "true"); // 闇€瑕佽璇?        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 浣跨敤SSL
        properties.put("mail.smtp.socketFactory.port", "465"); // SSL绔彛
        try {
            // 鍒涘缓浼氳瘽
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailFrom, emailAuthCode);
                }
            });
            // 鍒涘缓閭欢
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(emailSubject);
            message.setText(emailBody);
            // 鍙戦€侀偖浠?            Transport.send(message);
            logger.info("閭欢鍙戦€佹垚鍔燂紒");
        } catch (Exception e) {
            logger.error("閭欢鍙戦€佸け璐? " + e.getMessage());
        }
    }
}
