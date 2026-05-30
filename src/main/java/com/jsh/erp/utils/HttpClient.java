package com.jsh.erp.utils;


/**
 * HTTP 客户端工具类
 * 封装 HTTP 请求的发送方法（GET/POST），用于调用外部接口
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.http.HttpStatus.SC_OK;

public final class HttpClient {
    private static Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();

    /**
     * 閲囩敤Get鏂瑰紡鍙戦€佽姹傦紝鑾峰彇鍝嶅簲鏁版嵁
     * @param url
     * @return
     */
    public static JSONObject httpGet(String url){
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(REQUEST_CONFIG);
        try {
            CloseableHttpResponse chr = client.execute(httpGet);
            int statusCode = chr.getStatusLine().getStatusCode();
            if (SC_OK != statusCode) {
                throw new RuntimeException(String.format("%s鏌ヨ鍑虹幇寮傚父", url));
            }
            String entity = EntityUtils.toString(chr.getEntity(), StandardCharsets.UTF_8);
            JSONObject object = JSONObject.parseObject(entity);
            return object;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(String.format("%s", url) + "鏌ヨ鍑虹幇寮傚父");
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 閲囩敤Post鏂瑰紡鍙戦€佽姹傦紝鑾峰彇鍝嶅簲鏁版嵁
     *
     * @param url        url鍦板潃
     * @param param  鍙傛暟鍊奸敭鍊煎鐨勫瓧绗︿覆
     * @return
     */
    public static String httpPost(String url, String param) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        try {
            HttpPost post = new HttpPost(url);
            EntityBuilder builder = EntityBuilder.create();
            builder.setContentType(ContentType.APPLICATION_JSON);
            builder.setText(param);
            post.setEntity(builder.build());

            CloseableHttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String data = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            logger.info("鐘舵€?"+statusCode+",鏁版嵁:"+data);
            return data;
        } catch(Exception e){
            throw new RuntimeException(e.getMessage());
        } finally {
            try{
                client.close();
            }catch(Exception ex){ }
        }
    }
}