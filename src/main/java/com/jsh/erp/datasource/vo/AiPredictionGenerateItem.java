package com.jsh.erp.datasource.vo;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public class AiPredictionGenerateItem {

    @JsonAlias({"material_id", "mId", "mid"})
    private Long materialId;

    @JsonAlias({"depot_id", "dId", "depotID"})
    private Long depotId;

    private String barCode;

    private String materialName;

    private String depotName;

    private String unitName;

    private BigDecimal currentNumber;

    private BigDecimal lowSafeStock;

    private BigDecimal highSafeStock;

    private BigDecimal lowCritical;

    private BigDecimal highCritical;

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

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public BigDecimal getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(BigDecimal currentNumber) {
        this.currentNumber = currentNumber;
    }

    public BigDecimal getLowSafeStock() {
        return lowSafeStock;
    }

    public void setLowSafeStock(BigDecimal lowSafeStock) {
        this.lowSafeStock = lowSafeStock;
    }

    public BigDecimal getHighSafeStock() {
        return highSafeStock;
    }

    public void setHighSafeStock(BigDecimal highSafeStock) {
        this.highSafeStock = highSafeStock;
    }

    public BigDecimal getLowCritical() {
        return lowCritical;
    }

    public void setLowCritical(BigDecimal lowCritical) {
        this.lowCritical = lowCritical;
    }

    public BigDecimal getHighCritical() {
        return highCritical;
    }

    public void setHighCritical(BigDecimal highCritical) {
        this.highCritical = highCritical;
    }
}
