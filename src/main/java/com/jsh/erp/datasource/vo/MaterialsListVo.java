package com.jsh.erp.datasource.vo;


/**
 * 视图对象 (VO) - MaterialsListVo
 * 用于特定业务场景的数据传输，封装查询结果或前端请求参数
 *
 * @author jishenghua
 */
public class MaterialsListVo {

    private Long headerId;

    private String materialsList;

    public Long getHeaderId() {
        return headerId;
    }

    public void setHeaderId(Long headerId) {
        this.headerId = headerId;
    }

    public String getMaterialsList() {
        return materialsList;
    }

    public void setMaterialsList(String materialsList) {
        this.materialsList = materialsList;
    }
}
