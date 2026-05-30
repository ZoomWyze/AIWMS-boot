package com.jsh.erp.service;

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
            analysis = "AI未返回说明，已使用规则兜底建议";
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
            system.put("content", "你是资深库存与供应链规划专家。请严格返回JSON对象，字段仅包含forecastQty,suggestQty,aiAnalysis。不要返回markdown代码块。");
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
                logger.warn("DeepSeek接口异常，status={}, body={}", statusCode, body);
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
            logger.warn("调用DeepSeek失败: {}", e.getMessage());
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
        return "请根据下列库存预警数据预测未来7天销量并给出补货建议。"
                + "商品名称:" + nullToEmpty(item.getMaterialName())
                + ", 条码:" + nullToEmpty(item.getBarCode())
                + ", 仓库:" + nullToEmpty(item.getDepotName())
                + ", 当前库存:" + toNumStr(item.getCurrentNumber())
                + ", 最低安全库存:" + toNumStr(item.getLowSafeStock())
                + ", 最高安全库存:" + toNumStr(item.getHighSafeStock())
                + ", 建议入库量(规则):" + toNumStr(item.getLowCritical())
                + ", 建议出库量(规则):" + toNumStr(item.getHighCritical())
                + ". 返回JSON: {\"forecastQty\":数字,\"suggestQty\":数字,\"aiAnalysis\":\"原因说明\"}。"
                + "forecastQty和suggestQty保留2位小数, 且不能为负数。";
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
        fallback.put("aiAnalysis", "基于安全库存规则兜底计算：结合当前库存与安全库存阈值，给出未来7天销量与建议补货量。") ;
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
