package com.jsh.erp.service;


/**
 * AI 预测 Service
 * 提供基于 AI 的库存预测功能，分析历史数据生成采购建议
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.vo.AiPredictionGenerateItem;
import com.jsh.erp.datasource.vo.AiPredictionSaveItem;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AiPredictionService {
    private static final Logger logger = LoggerFactory.getLogger(AiPredictionService.class);

    @Value("${ai.deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String deepseekApiUrl;

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${ai.deepseek.timeout-millis:30000}")
    private Integer timeoutMillis;

    @Resource
    private DepotItemService depotItemService;

    public List<AiPredictionSaveItem> generateAndSave(List<AiPredictionGenerateItem> payloadList) {
        if (payloadList == null || payloadList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiPredictionSaveItem> resultList = new ArrayList<>();
        for (AiPredictionGenerateItem item : payloadList) {
            if (item == null || item.getMaterialId() == null || item.getDepotId() == null) {
                continue;
            }
            resultList.add(buildPredictionItem(item));
        }
        if (!resultList.isEmpty()) {
            depotItemService.saveAiPredictionBatch(resultList);
        }
        return resultList;
    }

    private AiPredictionSaveItem buildPredictionItem(AiPredictionGenerateItem item) {
        AiPredictionSaveItem saveItem = new AiPredictionSaveItem();
        saveItem.setMaterialId(item.getMaterialId());
        saveItem.setDepotId(item.getDepotId());

        JSONObject aiResult = callDeepSeek(item);
        if (aiResult == null) {
            aiResult = buildFallbackResult(item);
        }

        saveItem.setForecastQty(normalizeDecimal(aiResult.getBigDecimal("forecastQty")));
        saveItem.setSuggestQty(normalizeDecimal(aiResult.getBigDecimal("suggestQty")));
        String analysis = aiResult.getString("aiAnalysis");
        if (analysis == null || analysis.trim().isEmpty()) {
            analysis = "AI鏈繑鍥炶鏄庯紝宸蹭娇鐢ㄨ鍒欏厹搴曞缓璁?;
        }
        if (analysis.length() > 500) {
            analysis = analysis.substring(0, 500);
        }
        saveItem.setAiAnalysis(analysis);
        return saveItem;
    }

    private JSONObject callDeepSeek(AiPredictionGenerateItem item) {
        if (deepseekApiKey == null || deepseekApiKey.trim().isEmpty()) {
            return null;
        }
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeoutMillis)
                    .setConnectTimeout(timeoutMillis)
                    .build();
            client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpPost post = new HttpPost(deepseekApiUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + deepseekApiKey.trim());

            JSONObject payload = new JSONObject();
            payload.put("model", deepseekModel);
            payload.put("temperature", 0.2);

            JSONArray messages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "浣犳槸璧勬繁搴撳瓨涓庝緵搴旈摼瑙勫垝涓撳銆傝涓ユ牸杩斿洖JSON瀵硅薄锛屽瓧娈典粎鍖呭惈forecastQty,suggestQty,aiAnalysis銆備笉瑕佽繑鍥瀖arkdown浠ｇ爜鍧椼€?);
            messages.add(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", buildUserPrompt(item));
            messages.add(user);
            payload.put("messages", messages);

            post.setEntity(new StringEntity(payload.toJSONString(), ContentType.APPLICATION_JSON));
            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String body = entity == null ? null : EntityUtils.toString(entity, StandardCharsets.UTF_8);
            if (statusCode < 200 || statusCode >= 300 || body == null || body.trim().isEmpty()) {
                logger.warn("DeepSeek鎺ュ彛寮傚父锛宻tatus={}, body={}", statusCode, body);
                return null;
            }
            JSONObject responseJson = JSONObject.parseObject(body);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null) {
                return null;
            }
            String content = message.getString("content");
            return parseAiContent(content);
        } catch (Exception e) {
            logger.warn("璋冪敤DeepSeek澶辫触: {}", e.getMessage());
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (client != null) {
                    client.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String buildUserPrompt(AiPredictionGenerateItem item) {
        return "璇锋牴鎹笅鍒楀簱瀛橀璀︽暟鎹娴嬫湭鏉?澶╅攢閲忓苟缁欏嚭琛ヨ揣寤鸿銆?
                + "鍟嗗搧鍚嶇О:" + nullToEmpty(item.getMaterialName())
                + ", 鏉＄爜:" + nullToEmpty(item.getBarCode())
                + ", 浠撳簱:" + nullToEmpty(item.getDepotName())
                + ", 褰撳墠搴撳瓨:" + toNumStr(item.getCurrentNumber())
                + ", 鏈€浣庡畨鍏ㄥ簱瀛?" + toNumStr(item.getLowSafeStock())
                + ", 鏈€楂樺畨鍏ㄥ簱瀛?" + toNumStr(item.getHighSafeStock())
                + ", 寤鸿鍏ュ簱閲?瑙勫垯):" + toNumStr(item.getLowCritical())
                + ", 寤鸿鍑哄簱閲?瑙勫垯):" + toNumStr(item.getHighCritical())
                + ". 杩斿洖JSON: {\"forecastQty\":鏁板瓧,\"suggestQty\":鏁板瓧,\"aiAnalysis\":\"鍘熷洜璇存槑\"}銆?
                + "forecastQty鍜宻uggestQty淇濈暀2浣嶅皬鏁? 涓斾笉鑳戒负璐熸暟銆?;
    }

    private JSONObject parseAiContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String raw = content.trim();
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        String jsonText = raw.substring(start, end + 1);
        JSONObject result = JSONObject.parseObject(jsonText);
        if (result == null) {
            return null;
        }
        if (!result.containsKey("forecastQty") && result.containsKey("forecast_qty")) {
            result.put("forecastQty", result.getBigDecimal("forecast_qty"));
        }
        if (!result.containsKey("suggestQty") && result.containsKey("suggest_qty")) {
            result.put("suggestQty", result.getBigDecimal("suggest_qty"));
        }
        if (!result.containsKey("aiAnalysis") && result.containsKey("analysis")) {
            result.put("aiAnalysis", result.getString("analysis"));
        }
        return result;
    }

    private JSONObject buildFallbackResult(AiPredictionGenerateItem item) {
        JSONObject fallback = new JSONObject();
        BigDecimal current = safe(item.getCurrentNumber());
        BigDecimal low = safe(item.getLowSafeStock());
        BigDecimal high = safe(item.getHighSafeStock());

        BigDecimal forecastQty = safe(item.getLowCritical());
        if (forecastQty.compareTo(BigDecimal.ZERO) <= 0 && low.compareTo(BigDecimal.ZERO) > 0 && current.compareTo(low) < 0) {
            forecastQty = low.subtract(current);
        }
        if (forecastQty.compareTo(BigDecimal.ZERO) < 0) {
            forecastQty = BigDecimal.ZERO;
        }

        BigDecimal suggestQty = forecastQty;
        if (low.compareTo(BigDecimal.ZERO) > 0 && current.compareTo(low) < 0) {
            suggestQty = low.subtract(current).add(forecastQty.multiply(new BigDecimal("0.30")));
        }
        if (high.compareTo(BigDecimal.ZERO) > 0 && current.compareTo(high) > 0) {
            suggestQty = BigDecimal.ZERO;
        }
        if (suggestQty.compareTo(BigDecimal.ZERO) < 0) {
            suggestQty = BigDecimal.ZERO;
        }

        fallback.put("forecastQty", normalizeDecimal(forecastQty));
        fallback.put("suggestQty", normalizeDecimal(suggestQty));
        fallback.put("aiAnalysis", "鍩轰簬瀹夊叏搴撳瓨瑙勫垯鍏滃簳璁＄畻锛氱粨鍚堝綋鍓嶅簱瀛樹笌瀹夊叏搴撳瓨闃堝€硷紝缁欏嚭鏈潵7澶╅攢閲忎笌寤鸿琛ヨ揣閲忋€?) ;
        return fallback;
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String toNumStr(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
