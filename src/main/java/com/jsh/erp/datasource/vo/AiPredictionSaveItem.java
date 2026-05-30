package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - AiPredictionSaveItem
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public class AiPredictionSaveItem {

    @JsonAlias({"material_id", "mId", "mid"})
    private Long materialId;

    @JsonAlias({"depot_id", "dId", "depotID"})
    private Long depotId;

    @JsonAlias({"forecast_qty", "forecast", "predictQty"})
    private BigDecimal forecastQty;

    @JsonAlias({"suggest_qty", "suggest", "replenishQty"})
    private BigDecimal suggestQty;

    @JsonAlias({"ai_analysis", "analysis", "remark"})
    private String aiAnalysis;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getDepotId() {
        return depotId;
    }

    public void setDepotId(Long depotId) {
        this.depotId = depotId;
    }

    public BigDecimal getForecastQty() {
        return forecastQty;
    }

    public void setForecastQty(BigDecimal forecastQty) {
        this.forecastQty = forecastQty;
    }

    public BigDecimal getSuggestQty() {
        return suggestQty;
    }

    public void setSuggestQty(BigDecimal suggestQty) {
        this.suggestQty = suggestQty;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}

