package com.jsh.erp.controller;


/**
 * 单据明细 Controller
 * 提供单据明细行的操作接口，包括：查询商品库存、按仓库和条码查询库存、批号列表查询
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.vo.DepotItemStockWarningCount;
import com.jsh.erp.datasource.vo.DepotItemVoBatchNumberList;
import com.jsh.erp.datasource.vo.InOutPriceVo;
import com.jsh.erp.datasource.vo.MaterialDepotStock;
import com.jsh.erp.datasource.vo.AiPredictionSaveItem;
import com.jsh.erp.datasource.vo.AiPredictionGenerateItem;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.service.DepotService;
import com.jsh.erp.service.DepotHeadService;
import com.jsh.erp.service.DepotItemService;
import com.jsh.erp.service.AiPredictionService;
import com.jsh.erp.service.MaterialService;
import com.jsh.erp.service.RoleService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UnitService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jxl.Sheet;
import jxl.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;

/**
 * @author ji-sheng-hua 绠′紛浣砮rp
 */
@RestController
@RequestMapping(value = "/depotItem")
@Api(tags = {"鍗曟嵁鏄庣粏"})
public class DepotItemController {
    private Logger logger = LoggerFactory.getLogger(DepotItemController.class);

    @Resource
    private DepotHeadService depotHeadService;

    @Resource
    private DepotItemService depotItemService;

    @Resource
    private AiPredictionService aiPredictionService;

    @Resource
    private MaterialService materialService;

    @Resource
    private UnitService unitService;

    @Resource
    private DepotService depotService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @Resource
    private SystemConfigService systemConfigService;

    @Value(value="${file.uploadType}")
    private Long fileUploadType;

    /**
     * 鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠崟鎹垪琛?     * @param mId
     * @param request
     * @return
     */
    @GetMapping(value = "/findDetailByDepotIdsAndMaterialId")
    @ApiOperation(value = "鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠崟鎹垪琛?)
    public String findDetailByDepotIdsAndMaterialId(
            @RequestParam(value = Constants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = Constants.CURRENT_PAGE, required = false) Integer currentPage,
            @RequestParam(value = "depotIds",required = false) String depotIds,
            @RequestParam(value = "sku",required = false) String sku,
            @RequestParam(value = "batchNumber",required = false) String batchNumber,
            @RequestParam(value = "number",required = false) String number,
            @RequestParam(value = "beginTime",required = false) String beginTime,
            @RequestParam(value = "endTime",required = false) String endTime,
            @RequestParam("materialId") Long mId,
            HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        if(StringUtil.isNotEmpty(beginTime)) {
            beginTime = beginTime + BusinessConstants.DAY_FIRST_TIME;
        }
        if(StringUtil.isNotEmpty(endTime)) {
            endTime = endTime + BusinessConstants.DAY_LAST_TIME;
        }
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
        List<DepotItemVo4DetailByTypeAndMId> list = depotItemService.findDetailByDepotIdsAndMaterialIdList(depotIds, forceFlag, inOutManageFlag, sku,
                batchNumber, StringUtil.toNull(number), beginTime, endTime, mId, (currentPage-1)*pageSize, pageSize);
        JSONArray dataArray = new JSONArray();
        if (list != null) {
            for (DepotItemVo4DetailByTypeAndMId d: list) {
                JSONObject item = new JSONObject();
                item.put("number", d.getNumber()); //缂栧彿
                item.put("barCode", d.getBarCode()); //鏉＄爜
                item.put("materialName", d.getMaterialName()); //鍚嶇О
                String type = d.getType();
                String subType = d.getSubType();
                if(("鍏跺畠").equals(type)) {
                    item.put("type", subType); //杩涘嚭绫诲瀷
                } else {
                    item.put("type", subType + type); //杩涘嚭绫诲瀷
                }
                item.put("depotName", d.getDepotName()); //浠撳簱鍚嶇О
                item.put("basicNumber", d.getBnum()); //鏁伴噺
                item.put("unitPrice", d.getUnitPrice()); //鍗曚环
                item.put("allPrice", d.getAllPrice()); //閲戦
                item.put("operTime", Tools.getCenternTime(d.getOtime())); //鏃堕棿
                dataArray.add(item);
            }
        }
        if (list == null) {
            objectMap.put("rows", new ArrayList<Object>());
            objectMap.put("total", BusinessConstants.DEFAULT_LIST_NULL_NUMBER);
            return returnJson(objectMap, "鏌ユ壘涓嶅埌鏁版嵁", ErpInfo.OK.code);
        }
        objectMap.put("rows", dataArray);
        objectMap.put("total", depotItemService.findDetailByDepotIdsAndMaterialIdCount(depotIds, forceFlag, inOutManageFlag, sku,
                batchNumber, StringUtil.toNull(number), beginTime, endTime, mId));
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏍规嵁鍟嗗搧鏉＄爜鍜屼粨搴搃d鏌ヨ搴撳瓨鏁伴噺
     * @param depotId
     * @param barCode
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/findStockByDepotAndBarCode")
    @ApiOperation(value = "鏍规嵁鍟嗗搧鏉＄爜鍜屼粨搴搃d鏌ヨ搴撳瓨鏁伴噺")
    public BaseResponseInfo findStockByDepotAndBarCode(
            @RequestParam(value = "depotId",required = false) Long depotId,
            @RequestParam("barCode") String barCode,
            HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BigDecimal stock = BigDecimal.ZERO;
            List<MaterialVo4Unit> list = materialService.getMaterialByBarCode(barCode);
            if(list!=null && list.size()>0) {
                MaterialVo4Unit materialVo4Unit = list.get(0);
                if(StringUtil.isNotEmpty(materialVo4Unit.getSku())){
                    stock = depotItemService.getSkuStockByParam(depotId,materialVo4Unit.getMeId(),null,null);
                } else {
                    stock = depotItemService.getCurrentStockByParam(depotId, materialVo4Unit.getId());
                    if(materialVo4Unit.getUnitId()!=null) {
                        Unit unit = unitService.getUnit(materialVo4Unit.getUnitId());
                        String commodityUnit = materialVo4Unit.getCommodityUnit();
                        stock = unitService.parseStockByUnit(stock, unit, commodityUnit);
                    }
                }
            }
            map.put("stock", stock);
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鍗曟嵁鏄庣粏鍒楄〃
     * @param headerId
     * @param mpList
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getDetailList")
    @ApiOperation(value = "鍗曟嵁鏄庣粏鍒楄〃")
    public BaseResponseInfo getDetailList(@RequestParam("headerId") Long headerId,
                              @RequestParam(value = "mpList", required = false) String mpList,
                              @RequestParam(value = "linkType", required = false) String linkType,
                              @RequestParam(value = "isReadOnly", required = false) String isReadOnly,
                              HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Long userId = userService.getUserId(request);
            String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            List<DepotItemVo4WithInfoEx> dataList = new ArrayList<>();
            String billCategory = depotHeadService.getBillCategory(depotHeadService.getDepotHead(headerId).getSubType());
            if(headerId != 0) {
                dataList = depotItemService.getDetailList(headerId);
            }
            JSONObject outer = new JSONObject();
            outer.put("total", dataList.size());
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                BigDecimal totalOperNumber = BigDecimal.ZERO;
                BigDecimal totalAllPrice = BigDecimal.ZERO;
                BigDecimal totalTaxMoney = BigDecimal.ZERO;
                BigDecimal totalTaxLastMoney = BigDecimal.ZERO;
                BigDecimal totalWeight = BigDecimal.ZERO;
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", diEx.getId());
                    item.put("materialExtendId", diEx.getMaterialExtendId() == null ? "" : diEx.getMaterialExtendId());
                    item.put("barCode", diEx.getBarCode());
                    item.put("name", diEx.getMName());
                    item.put("standard", diEx.getMStandard());
                    item.put("model", diEx.getMModel());
                    item.put("color", diEx.getMColor());
                    item.put("brand", diEx.getBrand());
                    item.put("mfrs", diEx.getMMfrs());
                    item.put("otherField1", diEx.getMOtherField1());
                    item.put("otherField2", diEx.getMOtherField2());
                    item.put("otherField3", diEx.getMOtherField3());
                    BigDecimal stock;
                    Unit unitInfo = materialService.findUnit(diEx.getMaterialId()); //鏌ヨ澶氬崟浣嶄俊鎭?                    String materialUnit = diEx.getMaterialUnit();
                    if(StringUtil.isNotEmpty(diEx.getSku())){
                        stock = depotItemService.getSkuStockByParam(diEx.getDepotId(),diEx.getMaterialExtendId(),null,null);
                    } else {
                        stock = depotItemService.getCurrentStockByParam(diEx.getDepotId(),diEx.getMaterialId());
                        if (StringUtil.isNotEmpty(unitInfo.getName())) {
                            stock = unitService.parseStockByUnit(stock, unitInfo, materialUnit);
                        }
                    }
                    item.put("stock", stock);
                    item.put("unit", diEx.getMaterialUnit());
                    item.put("snList", diEx.getSnList());
                    item.put("batchNumber", diEx.getBatchNumber());
                    item.put("expirationDate", Tools.parseDateToStr(diEx.getExpirationDate()));
                    item.put("sku", diEx.getSku());
                    item.put("enableSerialNumber", diEx.getEnableSerialNumber());
                    item.put("enableBatchNumber", diEx.getEnableBatchNumber());
                    item.put("operNumber", diEx.getOperNumber());
                    item.put("basicNumber", diEx.getBasicNumber());
                    item.put("preNumber", diEx.getOperNumber()); //鍘熸暟閲?                    item.put("finishNumber", depotItemService.getFinishNumber(diEx.getMaterialExtendId(), diEx.getId(), diEx.getHeaderId(), unitInfo, materialUnit, linkType)); //宸插叆搴搢宸插嚭搴?                    item.put("purchaseDecimal", roleService.parseBillPriceByLimit(diEx.getPurchaseDecimal(), billCategory, priceLimit, request));  //閲囪喘浠?                    if("basic".equals(linkType) || "1".equals(isReadOnly)) {
                        //姝ｅ父鎯呭喌鏄剧ず閲戦锛岃€屼互閿€瀹氳喘鐨勬儏鍐典笉鑳芥樉绀洪噾棰?                        item.put("unitPrice", roleService.parseBillPriceByLimit(diEx.getUnitPrice(), billCategory, priceLimit, request));
                        item.put("taxUnitPrice", roleService.parseBillPriceByLimit(diEx.getTaxUnitPrice(), billCategory, priceLimit, request));
                        item.put("allPrice", roleService.parseBillPriceByLimit(diEx.getAllPrice(), billCategory, priceLimit, request));
                        item.put("taxRate", roleService.parseBillPriceByLimit(diEx.getTaxRate(), billCategory, priceLimit, request));
                        item.put("taxMoney", roleService.parseBillPriceByLimit(diEx.getTaxMoney(), billCategory, priceLimit, request));
                        item.put("taxLastMoney", roleService.parseBillPriceByLimit(diEx.getTaxLastMoney(), billCategory, priceLimit, request));
                    }
                    BigDecimal allWeight = diEx.getBasicNumber()==null||diEx.getWeight()==null?BigDecimal.ZERO:diEx.getBasicNumber().multiply(diEx.getWeight());
                    item.put("weight", allWeight);
                    item.put("position", diEx.getPosition());
                    item.put("remark", diEx.getRemark());
                    item.put("imgName", diEx.getImgName());
                    if(fileUploadType == 2) {
                        item.put("imgSmall", "small");
                        item.put("imgLarge", "large");
                    } else {
                        item.put("imgSmall", "");
                        item.put("imgLarge", "");
                    }
                    item.put("linkId", diEx.getLinkId());
                    item.put("depotId", diEx.getDepotId() == null ? "" : diEx.getDepotId());
                    item.put("depotName", diEx.getDepotId() == null ? "" : diEx.getDepotName());
                    item.put("anotherDepotId", diEx.getAnotherDepotId() == null ? "" : diEx.getAnotherDepotId());
                    item.put("anotherDepotName", diEx.getAnotherDepotId() == null ? "" : diEx.getAnotherDepotName());
                    item.put("mType", diEx.getMaterialType());
                    item.put("op", 1);
                    dataArray.add(item);
                    //鍚堣鏁版嵁姹囨€?                    totalOperNumber = totalOperNumber.add(diEx.getOperNumber()==null?BigDecimal.ZERO:diEx.getOperNumber());
                    totalAllPrice = totalAllPrice.add(diEx.getAllPrice()==null?BigDecimal.ZERO:diEx.getAllPrice());
                    totalTaxMoney = totalTaxMoney.add(diEx.getTaxMoney()==null?BigDecimal.ZERO:diEx.getTaxMoney());
                    totalTaxLastMoney = totalTaxLastMoney.add(diEx.getTaxLastMoney()==null?BigDecimal.ZERO:diEx.getTaxLastMoney());
                    totalWeight = totalWeight.add(allWeight);
                }
                if(StringUtil.isNotEmpty(isReadOnly) && "1".equals(isReadOnly)) {
                    JSONObject footItem = new JSONObject();
                    footItem.put("operNumber", totalOperNumber);
                    footItem.put("allPrice", roleService.parseBillPriceByLimit(totalAllPrice, billCategory, priceLimit, request));
                    footItem.put("taxMoney", roleService.parseBillPriceByLimit(totalTaxMoney, billCategory, priceLimit, request));
                    footItem.put("taxLastMoney", roleService.parseBillPriceByLimit(totalTaxLastMoney, billCategory, priceLimit, request));
                    footItem.put("weight", totalWeight);
                    dataArray.add(footItem);
                }
            }
            outer.put("rows", dataArray);
            res.code = 200;
            res.data = outer;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 杩涢攢瀛樼粺璁℃煡璇?     * @param currentPage
     * @param pageSize
     * @param depotIds
     * @param beginTime
     * @param endTime
     * @param materialParam
     * @param mpList
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getInOutStock")
    @ApiOperation(value = "杩涢攢瀛樼粺璁℃煡璇?)
    public BaseResponseInfo getInOutStock(@RequestParam("currentPage") Integer currentPage,
                                      @RequestParam("pageSize") Integer pageSize,
                                      @RequestParam(value = "depotIds",required = false) String depotIds,
                                      @RequestParam(value = "categoryId", required = false) Long categoryId,
                                      @RequestParam("beginTime") String beginTime,
                                      @RequestParam("endTime") String endTime,
                                      @RequestParam("materialParam") String materialParam,
                                      @RequestParam(value = "mpList", required = false) String mpList,
                                      HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<>();
        try {
            Boolean moveAvgPriceFlag = systemConfigService.getMoveAvgPriceFlag();
            List<Long> categoryIdList = new ArrayList<>();
            if(categoryId != null){
                categoryIdList = materialService.getListByParentId(categoryId);
            }
            beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            List<Long> depotList = parseListByDepotIds(depotIds);
            List<DepotItemVo4WithInfoEx> dataList = depotItemService.getInOutStock(StringUtil.toNull(materialParam),
                    categoryIdList, endTime,(currentPage-1)*pageSize, pageSize);
            int total = depotItemService.getInOutStockCount(StringUtil.toNull(materialParam), categoryIdList, endTime);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    JSONObject item = new JSONObject();
                    Long mId = diEx.getMId();
                    item.put("id", mId);
                    item.put("barCode", diEx.getBarCode());
                    item.put("materialName", diEx.getMName());
                    item.put("materialModel", diEx.getMModel());
                    item.put("materialStandard", diEx.getMStandard());
                    item.put("materialColor", diEx.getMColor());
                    item.put("materialMfrs", diEx.getMMfrs());
                    item.put("materialBrand", diEx.getBrand());
                    //鎵╁睍淇℃伅
                    item.put("otherField1", diEx.getMOtherField1());
                    item.put("otherField2", diEx.getMOtherField2());
                    item.put("otherField3", diEx.getMOtherField3());
                    item.put("unitId", diEx.getUnitId());
                    item.put("unitName", null!=diEx.getUnitId() ? diEx.getMaterialUnit()+"[澶氬崟浣峕" : diEx.getMaterialUnit());
                    BigDecimal prevSum = depotItemService.getStockByParamWithDepotList(depotList,mId,null,beginTime);
                    Map<String,BigDecimal> intervalMap = depotItemService.getIntervalMapByParamWithDepotList(depotList,mId,beginTime,endTime);
                    BigDecimal inSum = intervalMap.get("inSum");
                    BigDecimal outSum = intervalMap.get("outSum");
                    BigDecimal thisSum = prevSum.add(inSum).subtract(outSum);
                    item.put("prevSum", prevSum);
                    item.put("inSum", inSum);
                    item.put("outSum", outSum);
                    item.put("thisSum", thisSum);
                    //灏嗗皬鍗曚綅鐨勫簱瀛樻崲绠椾负澶у崟浣嶇殑搴撳瓨
                    item.put("bigUnitStock", materialService.getBigUnitStock(thisSum, diEx.getUnitId()));
                    if(moveAvgPriceFlag) {
                        item.put("unitPrice", diEx.getCurrentUnitPrice());
                    } else {
                        item.put("unitPrice", diEx.getPurchaseDecimal());
                    }
                    if(moveAvgPriceFlag) {
                        item.put("thisAllPrice", thisSum.multiply(diEx.getCurrentUnitPrice()));
                    } else {
                        item.put("thisAllPrice", thisSum.multiply(diEx.getPurchaseDecimal()));
                    }
                    item.put("imgName", diEx.getImgName());
                    if(fileUploadType == 2) {
                        item.put("imgSmall", "small");
                        item.put("imgLarge", "large");
                    }
                    dataArray.add(item);
                }
            }
            map.put("rows", dataArray);
            res.code = 200;
            res.data = map;
        } catch (BusinessRunTimeException e) {
            res.code = e.getCode();
            res.data = e.getData().get("message");
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 杩涢攢瀛樼粺璁℃€昏閲戦
     * @param depotIds
     * @param endTime
     * @param materialParam
     * @param request
     * @return
     */
    @GetMapping(value = "/getInOutStockCountMoney")
    @ApiOperation(value = "杩涢攢瀛樼粺璁℃€昏閲戦")
    public BaseResponseInfo getInOutStockCountMoney(@RequestParam(value = "depotIds",required = false) String depotIds,
                                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                                            @RequestParam("endTime") String endTime,
                                            @RequestParam("materialParam") String materialParam,
                                            HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<>();
        try {
            Boolean moveAvgPriceFlag = systemConfigService.getMoveAvgPriceFlag();
            List<Long> categoryIdList = new ArrayList<>();
            if(categoryId != null){
                categoryIdList = materialService.getListByParentId(categoryId);
            }
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            List<Long> depotList = parseListByDepotIds(depotIds);
            List<DepotItemVo4WithInfoEx> dataList = depotItemService.getInOutStock(StringUtil.toNull(materialParam),
                    categoryIdList, endTime, null, null);
            BigDecimal thisAllStock = BigDecimal.ZERO;
            BigDecimal thisAllPrice = BigDecimal.ZERO;
            if (null != dataList) {
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    Long mId = diEx.getMId();
                    BigDecimal thisSum = depotItemService.getStockByParamWithDepotList(depotList,mId,null,endTime);
                    thisAllStock = thisAllStock.add(thisSum);
                    BigDecimal unitPrice = null;
                    if(moveAvgPriceFlag) {
                        unitPrice = diEx.getCurrentUnitPrice();
                    } else {
                        unitPrice = diEx.getPurchaseDecimal();
                    }
                    if(unitPrice == null) {
                        unitPrice = BigDecimal.ZERO;
                    }
                    thisAllPrice = thisAllPrice.add(thisSum.multiply(unitPrice));
                }
            }
            map.put("totalStock", thisAllStock);
            map.put("totalCount", thisAllPrice);
            res.code = 200;
            res.data = map;
        } catch (BusinessRunTimeException e) {
            res.code = e.getCode();
            res.data = e.getData().get("message");
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    @PostMapping(value = "/saveAiPrediction")
    @ApiOperation(value = "淇濆瓨AI棰勬祴缁撴灉")
    public BaseResponseInfo saveAiPrediction(@RequestBody List<AiPredictionSaveItem> payloadList) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            int affectedRows = depotItemService.saveAiPredictionBatch(payloadList);
            res.code = 200;
            Map<String, Object> data = new HashMap<>();
            data.put("message", "鎿嶄綔鎴愬姛");
            data.put("affectedRows", affectedRows);
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "淇濆瓨澶辫触";
        }
        return res;
    }

    @PostMapping(value = "/generateAiPrediction")
    @ApiOperation(value = "璋冪敤DeepSeek鐢熸垚骞朵繚瀛楢I棰勬祴缁撴灉")
    public BaseResponseInfo generateAiPrediction(@RequestBody List<AiPredictionGenerateItem> payloadList) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<AiPredictionSaveItem> resultList = aiPredictionService.generateAndSave(payloadList);
            Map<String, Object> data = new HashMap<>();
            data.put("message", "鎿嶄綔鎴愬姛");
            data.put("rows", resultList);
            data.put("affectedRows", resultList.size());
            res.code = 200;
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "AI鍐崇瓥鐢熸垚澶辫触";
        }
        return res;
    }


    /**
     * 鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠簱瀛樺垎甯冩儏鍐?甯︽椂闂存鍙傛暟
     * @param mId
     * @param request
     * @return
     */
    @GetMapping(value = "/getMaterialDepotStockByParam")
    @ApiOperation(value = "鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠簱瀛樺垎甯冩儏鍐?甯︽椂闂存鍙傛暟")
    public String getMaterialDepotStockByParam(
            @RequestParam(value = "depotIds",required = false) String depotIds,
            @RequestParam("materialId") Long mId,
            @RequestParam(value = "unitPrice", required = false) BigDecimal unitPrice,
            @RequestParam("beginTime") String beginTime,
            @RequestParam("endTime") String endTime,
            HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
        endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
        Map<Long, String> depotMap = new HashMap<>();
        JSONArray depotArr = depotService.findDepotByCurrentUser();
        for (Object depotObj: depotArr) {
            if(depotObj!=null) {
                JSONObject depotObject = JSONObject.parseObject(depotObj.toString());
                depotMap.put(depotObject.getLong("id"), depotObject.getString("depotName"));
            }
        }
        String[] depotIdArr = null;
        if(StringUtil.isNotEmpty(depotIds)) {
            depotIdArr = depotIds.split(",");
        }
        List<Long> depotList = depotService.parseDepotListByArr(depotIdArr);
        Long[] depotIdArray = StringUtil.listToLongArray(depotList);
        List<MaterialDepotStock> list = new ArrayList<>();
        for (int i = 0; i < depotIdArray.length; i++) {
            Long depotId = depotIdArray[i];
            List<Long> currentDepotIdList = new ArrayList<>();
            currentDepotIdList.add(depotId);
            String depotName = depotMap.get(depotId);
            MaterialDepotStock materialDepotStock = new MaterialDepotStock();
            materialDepotStock.setDepotId(depotId);
            materialDepotStock.setDepotName(depotName);
            BigDecimal prevSum = depotItemService.getStockByParamWithDepotList(currentDepotIdList,mId,null,beginTime);
            Map<String,BigDecimal> intervalMap = depotItemService.getIntervalMapByParamWithDepotList(currentDepotIdList,mId,beginTime,endTime);
            BigDecimal inSum = intervalMap.get("inSum");
            BigDecimal outSum = intervalMap.get("outSum");
            BigDecimal thisSum = prevSum.add(inSum).subtract(outSum);
            materialDepotStock.setCurrentNumber(thisSum);
            materialDepotStock.setUnitPrice(unitPrice);
            if(materialDepotStock.getCurrentNumber()!=null && materialDepotStock.getUnitPrice()!=null ) {
                materialDepotStock.setAllPrice(materialDepotStock.getCurrentNumber().multiply(materialDepotStock.getUnitPrice()));
            }
            if(thisSum.compareTo(BigDecimal.ZERO)!=0) {
                list.add(materialDepotStock);
            }
        }
        objectMap.put("rows", list);
        objectMap.put("total", list.size());
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    private List<Long> parseListByDepotIds(@RequestParam("depotIds") String depotIds) throws Exception {
        List<Long> depotList = new ArrayList<>();
        if(StringUtil.isNotEmpty(depotIds)) {
            depotList = StringUtil.strToLongList(depotIds);
        } else {
            //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
            JSONArray depotArr = depotService.findDepotByCurrentUser();
            for(Object obj: depotArr) {
                JSONObject object = JSONObject.parseObject(obj.toString());
                depotList.add(object.getLong("id"));
            }
            //濡傛灉鏈夋潈闄愮殑浠撳簱鏁伴噺澶鍒欐彁绀鸿閫夋嫨浠撳簱
            if(depotList.size()>20) {
                throw new BusinessRunTimeException(ExceptionConstants.REPORT_TWO_MANY_DEPOT_FAILED_CODE,
                        ExceptionConstants.REPORT_TWO_MANY_DEPOT_FAILED_MSG);
            }
        }
        return depotList;
    }

    /**
     * 閲囪喘缁熻
     * @param currentPage
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param materialParam
     * @param mpList
     * @param request
     * @return
     */
    @GetMapping(value = "/buyIn")
    @ApiOperation(value = "閲囪喘缁熻")
    public BaseResponseInfo buyIn(@RequestParam("currentPage") Integer currentPage,
                                  @RequestParam("pageSize") Integer pageSize,
                                  @RequestParam("beginTime") String beginTime,
                                  @RequestParam("endTime") String endTime,
                                  @RequestParam(value = "organId", required = false) Long organId,
                                  @RequestParam(value = "depotId", required = false) Long depotId,
                                  @RequestParam(value = "categoryId", required = false) Long categoryId,
                                  @RequestParam(value = "organizationId", required = false) Long organizationId,
                                  @RequestParam("materialParam") String materialParam,
                                  @RequestParam(value = "mpList",required = false) String mpList,
                                  HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
        endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
        try {
            String [] creatorArray = depotHeadService.getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = depotHeadService.getCreatorArrayByOrg(organizationId);
            }
            String [] organArray = null;
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            List<Long> depotList = depotService.parseDepotList(depotId);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            List<DepotItemVo4WithInfoEx> dataList = depotItemService.getListWithBuyOrSale(StringUtil.toNull(materialParam),
                    "buy", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag, (currentPage-1)*pageSize, pageSize);
            int total = depotItemService.getListWithBuyOrSaleCount(StringUtil.toNull(materialParam),
                    "buy", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    JSONObject item = new JSONObject();
                    BigDecimal InSum = depotItemService.buyOrSale("鍏ュ簱", "閲囪喘", diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal OutSum = depotItemService.buyOrSale("鍑哄簱", "閲囪喘閫€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal InSumPrice = depotItemService.buyOrSale("鍏ュ簱", "閲囪喘", diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal OutSumPrice = depotItemService.buyOrSale("鍑哄簱", "閲囪喘閫€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal InOutSumPrice = InSumPrice.subtract(OutSumPrice);
                    item.put("barCode", diEx.getBarCode());
                    item.put("materialName", diEx.getMName());
                    item.put("materialModel", diEx.getMModel());
                    item.put("materialStandard", diEx.getMStandard());
                    //鎵╁睍淇℃伅
                    item.put("otherField1", diEx.getMOtherField1());
                    item.put("otherField2", diEx.getMOtherField2());
                    item.put("otherField3", diEx.getMOtherField3());
                    item.put("materialColor", diEx.getMColor());
                    item.put("materialBrand", diEx.getBrand());
                    item.put("materialMfrs", diEx.getMMfrs());
                    item.put("materialUnit", diEx.getMaterialUnit());
                    item.put("unitName", diEx.getUnitName());
                    item.put("inSum", InSum);
                    item.put("outSum", OutSum);
                    item.put("inSumPrice", InSumPrice);
                    item.put("outSumPrice", OutSumPrice);
                    item.put("inOutSumPrice",InOutSumPrice);//瀹為檯閲囪喘閲戦
                    dataArray.add(item);
                }
            }
            BigDecimal inSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍏ュ簱", "閲囪喘", StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal outSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍑哄簱", "閲囪喘閫€璐?, StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal realityPriceTotal = inSumPriceTotal.subtract(outSumPriceTotal);
            map.put("rows", dataArray);
            map.put("realityPriceTotal", realityPriceTotal);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 闆跺敭缁熻
     * @param currentPage
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param materialParam
     * @param mpList
     * @param request
     * @return
     */
    @GetMapping(value = "/retailOut")
    @ApiOperation(value = "闆跺敭缁熻")
    public BaseResponseInfo retailOut(@RequestParam("currentPage") Integer currentPage,
                                      @RequestParam("pageSize") Integer pageSize,
                                      @RequestParam("beginTime") String beginTime,
                                      @RequestParam("endTime") String endTime,
                                      @RequestParam(value = "organId", required = false) Long organId,
                                      @RequestParam(value = "depotId", required = false) Long depotId,
                                      @RequestParam(value = "categoryId", required = false) Long categoryId,
                                      @RequestParam(value = "organizationId", required = false) Long organizationId,
                                      @RequestParam("materialParam") String materialParam,
                                      @RequestParam(value = "mpList", required = false) String mpList,
                                      HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
        endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
        try {
            String [] creatorArray = depotHeadService.getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = depotHeadService.getCreatorArrayByOrg(organizationId);
            }
            String [] organArray = null;
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            List<Long> depotList = depotService.parseDepotList(depotId);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            List<DepotItemVo4WithInfoEx> dataList = depotItemService.getListWithBuyOrSale(StringUtil.toNull(materialParam),
                    "retail", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag, (currentPage-1)*pageSize, pageSize);
            int total = depotItemService.getListWithBuyOrSaleCount(StringUtil.toNull(materialParam),
                    "retail", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    JSONObject item = new JSONObject();
                    BigDecimal OutSumRetail = depotItemService.buyOrSale("鍑哄簱", "闆跺敭", diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal InSumRetail = depotItemService.buyOrSale("鍏ュ簱", "闆跺敭閫€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal OutSumRetailPrice = depotItemService.buyOrSale("鍑哄簱", "闆跺敭", diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal InSumRetailPrice = depotItemService.buyOrSale("鍏ュ簱", "闆跺敭閫€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal OutInSumPrice = OutSumRetailPrice.subtract(InSumRetailPrice);
                    item.put("barCode", diEx.getBarCode());
                    item.put("materialName", diEx.getMName());
                    item.put("materialModel", diEx.getMModel());
                    item.put("materialStandard", diEx.getMStandard());
                    //鎵╁睍淇℃伅
                    item.put("otherField1", diEx.getMOtherField1());
                    item.put("otherField2", diEx.getMOtherField2());
                    item.put("otherField3", diEx.getMOtherField3());
                    item.put("materialColor", diEx.getMColor());
                    item.put("materialBrand", diEx.getBrand());
                    item.put("materialMfrs", diEx.getMMfrs());
                    item.put("materialUnit", diEx.getMaterialUnit());
                    item.put("unitName", diEx.getUnitName());
                    item.put("outSum", OutSumRetail);
                    item.put("inSum", InSumRetail);
                    item.put("outSumPrice", OutSumRetailPrice);
                    item.put("inSumPrice", InSumRetailPrice);
                    item.put("outInSumPrice",OutInSumPrice);//瀹為檯閿€鍞噾棰?                    dataArray.add(item);
                }
            }
            BigDecimal outSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍑哄簱", "闆跺敭", StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal inSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍏ュ簱", "闆跺敭閫€璐?, StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal realityPriceTotal = outSumPriceTotal.subtract(inSumPriceTotal);
            map.put("rows", dataArray);
            map.put("realityPriceTotal", realityPriceTotal);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }


    /**
     * 閿€鍞粺璁?     * @param currentPage
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param materialParam
     * @param mpList
     * @param request
     * @return
     */
    @GetMapping(value = "/saleOut")
    @ApiOperation(value = "閿€鍞粺璁?)
    public BaseResponseInfo saleOut(@RequestParam("currentPage") Integer currentPage,
                                    @RequestParam("pageSize") Integer pageSize,
                                    @RequestParam("beginTime") String beginTime,
                                    @RequestParam("endTime") String endTime,
                                    @RequestParam(value = "organId", required = false) Long organId,
                                    @RequestParam(value = "depotId", required = false) Long depotId,
                                    @RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "organizationId", required = false) Long organizationId,
                                    @RequestParam("materialParam") String materialParam,
                                    @RequestParam(value = "mpList", required = false) String mpList,
                                    HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
        endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
        try {
            String [] creatorArray = depotHeadService.getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = depotHeadService.getCreatorArrayByOrg(organizationId);
            }
            String [] organArray = depotHeadService.getOrganArray("閿€鍞?, "");
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            List<Long> depotList = depotService.parseDepotList(depotId);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            List<DepotItemVo4WithInfoEx> dataList = depotItemService.getListWithBuyOrSale(StringUtil.toNull(materialParam),
                    "sale", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag, (currentPage-1)*pageSize, pageSize);
            int total = depotItemService.getListWithBuyOrSaleCount(StringUtil.toNull(materialParam),
                    "sale", beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    JSONObject item = new JSONObject();
                    BigDecimal OutSum = depotItemService.buyOrSale("鍑哄簱", "閿€鍞?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal InSum = depotItemService.buyOrSale("鍏ュ簱", "閿€鍞€€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "number");
                    BigDecimal OutSumPrice = depotItemService.buyOrSale("鍑哄簱", "閿€鍞?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal InSumPrice = depotItemService.buyOrSale("鍏ュ簱", "閿€鍞€€璐?, diEx.getMaterialExtendId(), beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, "price");
                    BigDecimal OutInSumPrice = OutSumPrice.subtract(InSumPrice);
                    item.put("barCode", diEx.getBarCode());
                    item.put("materialName", diEx.getMName());
                    item.put("materialModel", diEx.getMModel());
                    item.put("materialStandard", diEx.getMStandard());
                    //鎵╁睍淇℃伅
                    item.put("otherField1", diEx.getMOtherField1());
                    item.put("otherField2", diEx.getMOtherField2());
                    item.put("otherField3", diEx.getMOtherField3());
                    item.put("materialColor", diEx.getMColor());
                    item.put("materialBrand", diEx.getBrand());
                    item.put("materialMfrs", diEx.getMMfrs());
                    item.put("materialUnit", diEx.getMaterialUnit());
                    item.put("unitName", diEx.getUnitName());
                    item.put("outSum", OutSum);
                    item.put("inSum", InSum);
                    item.put("outSumPrice", OutSumPrice);
                    item.put("inSumPrice", InSumPrice);
                    item.put("outInSumPrice",OutInSumPrice);//瀹為檯閿€鍞噾棰?                    dataArray.add(item);
                }
            }
            BigDecimal outSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍑哄簱", "閿€鍞?, StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal inSumPriceTotal = depotItemService.buyOrSalePriceTotal("鍏ュ簱", "閿€鍞€€璐?, StringUtil.toNull(materialParam),
                    beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
            BigDecimal realityPriceTotal = outSumPriceTotal.subtract(inSumPriceTotal);
            map.put("rows", dataArray);
            map.put("realityPriceTotal", realityPriceTotal);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鑾峰彇鍗曚綅
     * @param materialUnit
     * @param uName
     * @return
     */
    public String getUName(String materialUnit, String uName) {
        String unitName = null;
        if(StringUtil.isNotEmpty(materialUnit)) {
            unitName = materialUnit;
        } else if(StringUtil.isNotEmpty(uName)) {
            unitName = uName;
        }
        return unitName;
    }

    /**
     * 搴撳瓨棰勮鎶ヨ〃
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/findStockWarningCount")
    @ApiOperation(value = "搴撳瓨棰勮鎶ヨ〃")
    public BaseResponseInfo findStockWarningCount(@RequestParam("currentPage") Integer currentPage,
                                                  @RequestParam("pageSize") Integer pageSize,
                                                  @RequestParam(value = "materialParam", required = false) String materialParam,
                                                  @RequestParam(value = "depotId", required = false) Long depotId,
                                                  @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                  @RequestParam(value = "mpList", required = false) String mpList)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Long queryDepotId = (depotId != null && depotId.longValue() == -1L) ? null : depotId;
            List<Long> depotList = new ArrayList<>();
            User currentUser = userService.getCurrentUser();
            boolean isDefaultManager = PermissionUtil.isDefaultManager(currentUser);
            if(queryDepotId != null) {
                depotList.add(queryDepotId);
            } else if (isDefaultManager) {
                // 榛樿绠＄悊鍛樻煡鐪嬪叏閮ㄥ惎鐢ㄤ粨搴擄紝涓嶅彈 UserDepot 鎺堟潈闄愬埗
                List<Depot> allDepotList = depotService.getAllList();
                for (Depot depot : allDepotList) {
                    depotList.add(depot.getId());
                }
            } else {
                // 闈炵鐞嗗憳鏃讹紝榛樿褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
                JSONArray depotArr = depotService.findDepotByCurrentUser();
                for(Object obj: depotArr) {
                    JSONObject object = JSONObject.parseObject(obj.toString());
                    depotList.add(object.getLong("id"));
                }
            }
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            List<DepotItemStockWarningCount> list = depotItemService.findStockWarningCount((currentPage-1)*pageSize, pageSize, materialParam, queryDepotId, depotList, categoryList);
            //瀛樻斁鏁版嵁json鏁扮粍
            if (null != list) {
                for (DepotItemStockWarningCount disw : list) {
                    DepotItemVo4WithInfoEx diEx = new DepotItemVo4WithInfoEx();
                    diEx.setMOtherField1(disw.getMOtherField1());
                    diEx.setMOtherField2(disw.getMOtherField2());
                    diEx.setMOtherField3(disw.getMOtherField3());
                    disw.setMaterialUnit(getUName(disw.getMaterialUnit(), disw.getUnitName()));
                    disw.setLowCritical(BigDecimal.ZERO);
                    disw.setHighCritical(BigDecimal.ZERO);
                    if(null!=disw.getLowSafeStock() && disw.getCurrentNumber().compareTo(disw.getLowSafeStock())<0) {
                        disw.setLowCritical(disw.getLowSafeStock().subtract(disw.getCurrentNumber()));
                    }
                    if(null!=disw.getHighSafeStock() && disw.getCurrentNumber().compareTo(disw.getHighSafeStock())>0) {
                        disw.setHighCritical(disw.getCurrentNumber().subtract(disw.getHighSafeStock()));
                    }
                }
            }
            int total = depotItemService.findStockWarningCountTotal(materialParam, depotList, categoryList);
            map.put("total", total);
            map.put("rows", list);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 缁熻閲囪喘銆侀攢鍞€侀浂鍞殑鎬婚噾棰?     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/buyOrSalePrice")
    @ApiOperation(value = "缁熻閲囪喘銆侀攢鍞€侀浂鍞殑鎬婚噾棰?)
    public BaseResponseInfo buyOrSalePrice(HttpServletRequest request,
                                           HttpServletResponse response)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> map = new HashMap<>();
            String loginName = userService.getCurrentUser().getLoginName();
            String priceLimit = "";
            if (!PermissionUtil.isDefaultManager(loginName)) {
                Long userId = userService.getUserId(request);
                priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            }
            List<String> monthList = Tools.getLastMonths(6);
            String beginTime = Tools.firstDayOfMonth(monthList.get(0)) + BusinessConstants.DAY_FIRST_TIME;
            String endTime = Tools.getNow() + BusinessConstants.DAY_LAST_TIME;
            List<InOutPriceVo> inOrOutPriceList = depotItemService.inOrOutPriceList(beginTime, endTime);
            
            JSONArray buyPriceList = new JSONArray();
            for (String month : monthList) {
                JSONObject obj = new JSONObject();
                BigDecimal outPrice = BigDecimal.ZERO;
                BigDecimal inPrice = BigDecimal.ZERO;
                for (InOutPriceVo item : inOrOutPriceList) {
                    String billOperMonth = Tools.dateToStr(item.getOperTime(), "yyyy-MM");
                    if (month.equals(billOperMonth)) {
                        if ("鍏ュ簱".equals(item.getType()) && "閲囪喘".equals(item.getSubType())) {
                            outPrice = outPrice.add(item.getDiscountLastMoney());
                        }
                        if ("鍑哄簱".equals(item.getType()) && "閲囪喘閫€璐?.equals(item.getSubType())) {
                            inPrice = inPrice.add(item.getDiscountLastMoney());
                        }
                    }
                }
                obj.put("x", month);
                obj.put("y", roleService.parseHomePriceByLimit(outPrice.subtract(inPrice), "buy", priceLimit, "***", request));
                buyPriceList.add(obj);
            }
            map.put("buyPriceList", buyPriceList);
            JSONArray salePriceList = new JSONArray();
            for (String month : monthList) {
                JSONObject obj = new JSONObject();
                BigDecimal outPrice = BigDecimal.ZERO;
                BigDecimal inPrice = BigDecimal.ZERO;
                for (InOutPriceVo item : inOrOutPriceList) {
                    String billOperMonth = Tools.dateToStr(item.getOperTime(), "yyyy-MM");
                    if (month.equals(billOperMonth)) {
                        if ("鍑哄簱".equals(item.getType()) && "閿€鍞?.equals(item.getSubType())) {
                            outPrice = outPrice.add(item.getDiscountLastMoney());
                        }
                        if ("鍏ュ簱".equals(item.getType()) && "閿€鍞€€璐?.equals(item.getSubType())) {
                            inPrice = inPrice.add(item.getDiscountLastMoney());
                        }
                    }
                }
                obj.put("x", month);
                obj.put("y", roleService.parseHomePriceByLimit(outPrice.subtract(inPrice), "sale", priceLimit, "***", request));
                salePriceList.add(obj);
            }
            map.put("salePriceList", salePriceList);
            JSONArray retailPriceList = new JSONArray();
            for (String month : monthList) {
                JSONObject obj = new JSONObject();
                BigDecimal outPrice = BigDecimal.ZERO;
                BigDecimal inPrice = BigDecimal.ZERO;
                for (InOutPriceVo item : inOrOutPriceList) {
                    String billOperMonth = Tools.dateToStr(item.getOperTime(), "yyyy-MM");
                    if (month.equals(billOperMonth)) {
                        if ("鍑哄簱".equals(item.getType()) && "闆跺敭".equals(item.getSubType())) {
                            outPrice = outPrice.add(item.getTotalPrice().abs());
                        }
                        if ("鍏ュ簱".equals(item.getType()) && "闆跺敭閫€璐?.equals(item.getSubType())) {
                            inPrice = inPrice.add(item.getTotalPrice().abs());
                        }
                    }
                }
                obj.put("x", month);
                obj.put("y", roleService.parseHomePriceByLimit(outPrice.subtract(inPrice), "retail", priceLimit, "***", request));
                retailPriceList.add(obj);
            }
            map.put("retailPriceList", retailPriceList);
            
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "缁熻澶辫触";
        }
        return res;
    }

    /**
     * 鑾峰彇鎵规鍟嗗搧鍒楄〃淇℃伅
     * @param request
     * @return
     */
    @GetMapping(value = "/getBatchNumberList")
    @ApiOperation(value = "鑾峰彇鎵规鍟嗗搧鍒楄〃淇℃伅")
    public BaseResponseInfo getBatchNumberList(@RequestParam("name") String name,
                                               @RequestParam("depotItemId") Long depotItemId,
                                               @RequestParam("depotId") Long depotId,
                                               @RequestParam("barCode") String barCode,
                                               @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                               HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<>();
        try {
            String number = "";
            if(depotItemId != null) {
                DepotItem depotItem = depotItemService.getDepotItem(depotItemId);
                number = depotHeadService.getDepotHead(depotItem.getHeaderId()).getNumber();
            }
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
            List<DepotItemVoBatchNumberList> list = depotItemService.getBatchNumberList(number, name, depotId, barCode,
                    batchNumber, forceFlag, inOutManageFlag);
            map.put("rows", list);
            map.put("total", list.size());
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * Excel瀵煎叆鏄庣粏
     * @param file
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/importItemExcel")
    public BaseResponseInfo importItemExcel(MultipartFile file,
                                            @RequestParam(required = false, value = "prefixNo") String prefixNo,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> data = new HashMap<>();
        String message = "";
        try {
            String barCodes = "";
            //鏂囦欢鎵╁睍鍚嶅彧鑳戒负xls
            String fileName = file.getOriginalFilename();
            if(StringUtil.isNotEmpty(fileName)) {
                String fileExt = fileName.substring(fileName.indexOf(".")+1);
                if(!"xls".equals(fileExt)) {
                    throw new BusinessRunTimeException(ExceptionConstants.FILE_EXTENSION_ERROR_CODE,
                            ExceptionConstants.FILE_EXTENSION_ERROR_MSG);
                }
            }
            Workbook workbook = Workbook.getWorkbook(file.getInputStream());
            Sheet  src = workbook.getSheet(0);
            if(src.getRows()>1000) {
                message = "瀵煎叆澶辫触锛屾槑缁嗕笉鑳借秴鍑?000鏉?;
                res.code = 500;
                data.put("message", message);
                res.data = data;
            } else {
                List<Map<String, String>> detailList = new ArrayList<>();
                for (int i = 2; i < src.getRows(); i++) {
                    String depotName = "", barCode = "", num = "", unitPrice = "", taxRate = "", remark = "";
                    if("QGD".equals(prefixNo)) {
                        barCode = ExcelUtils.getContent(src, i, 0);
                        num = ExcelUtils.getContent(src, i, 2);
                        remark = ExcelUtils.getContent(src, i, 3);
                    }
                    if("CGDD".equals(prefixNo) || "XSDD".equals(prefixNo)) {
                        barCode = ExcelUtils.getContent(src, i, 0);
                        num = ExcelUtils.getContent(src, i, 2);
                        unitPrice = ExcelUtils.getContentNumber(src, i, 3);
                        taxRate = ExcelUtils.getContent(src, i, 4);
                        remark = ExcelUtils.getContent(src, i, 5);
                    }
                    if("CGRK".equals(prefixNo) || "XSCK".equals(prefixNo)) {
                        depotName = ExcelUtils.getContent(src, i, 0);
                        barCode = ExcelUtils.getContent(src, i, 1);
                        num = ExcelUtils.getContent(src, i, 3);
                        unitPrice = ExcelUtils.getContentNumber(src, i, 4);
                        taxRate = ExcelUtils.getContent(src, i, 5);
                        remark = ExcelUtils.getContent(src, i, 6);
                    }
                    if("QTRK".equals(prefixNo) || "QTCK".equals(prefixNo)) {
                        depotName = ExcelUtils.getContent(src, i, 0);
                        barCode = ExcelUtils.getContent(src, i, 1);
                        num = ExcelUtils.getContent(src, i, 3);
                        unitPrice = ExcelUtils.getContentNumber(src, i, 4);
                        remark = ExcelUtils.getContent(src, i, 5);
                    }
                    Map<String, String> materialMap = new HashMap<>();
                    materialMap.put("depotName", depotName);
                    materialMap.put("barCode", barCode);
                    materialMap.put("num", num);
                    materialMap.put("unitPrice", unitPrice);
                    materialMap.put("taxRate", taxRate);
                    materialMap.put("remark", remark);
                    detailList.add(materialMap);
                    barCodes += "'" + barCode + "',";
                }
                if (StringUtil.isNotEmpty(barCodes)) {
                    barCodes = barCodes.substring(0, barCodes.length() - 1);
                }
                JSONObject map = depotItemService.parseMapByExcelData(barCodes, detailList, prefixNo);
                if (map != null) {
                    res.code = 200;
                } else {
                    res.code = 500;
                }
                res.data = map;
            }
        } catch (BusinessRunTimeException e) {
            res.code = 500;
            data.put("message", e.getData().get("message"));
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            message = "瀵煎叆澶辫触锛岃妫€鏌ヨ〃鏍煎唴瀹?;
            res.code = 500;
            data.put("message", message);
            res.data = data;
        }
        return res;
    }
}
