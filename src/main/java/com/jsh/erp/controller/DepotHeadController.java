package com.jsh.erp.controller;


/**
 * 单据主表 Controller
 * 提供单据（采购入库/销售出库/退货/调拨等）的主表操作接口，包括：新增/编辑/删除/审核/反审核/查询/Excel导出
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.DepotHead;
import com.jsh.erp.datasource.entities.DepotHeadVo4Body;
import com.jsh.erp.datasource.vo.DepotHeadVo4InDetail;
import com.jsh.erp.datasource.vo.DepotHeadVo4InOutMCount;
import com.jsh.erp.datasource.vo.DepotHeadVo4List;
import com.jsh.erp.datasource.vo.DepotHeadVo4StatementAccount;
import com.jsh.erp.service.DepotService;
import com.jsh.erp.service.DepotHeadService;
import com.jsh.erp.service.MaterialService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji-sheng-hua 752*718*920
 */
@RestController
@RequestMapping(value = "/depotHead")
@Api(tags = {"鍗曟嵁绠＄悊"})
public class DepotHeadController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DepotHeadController.class);

    @Resource
    private DepotHeadService depotHeadService;

    @Resource
    private DepotService depotService;

    @Resource
    private MaterialService materialService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        DepotHead depotHead = depotHeadService.getDepotHead(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(depotHead != null) {
            objectMap.put("info", depotHead);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String type = StringUtil.getInfo(search, "type");
        String subType = StringUtil.getInfo(search, "subType");
        String hasDebt = StringUtil.getInfo(search, "hasDebt");
        String status = StringUtil.getInfo(search, "status");
        String purchaseStatus = StringUtil.getInfo(search, "purchaseStatus");
        String number = StringUtil.getInfo(search, "number");
        String linkApply = StringUtil.getInfo(search, "linkApply");
        String linkNumber = StringUtil.getInfo(search, "linkNumber");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        String materialParam = StringUtil.getInfo(search, "materialParam");
        Long organId = StringUtil.parseStrLong(StringUtil.getInfo(search, "organId"));
        Long creator = StringUtil.parseStrLong(StringUtil.getInfo(search, "creator"));
        Long depotId = StringUtil.parseStrLong(StringUtil.getInfo(search, "depotId"));
        Long accountId = StringUtil.parseStrLong(StringUtil.getInfo(search, "accountId"));
        String salesMan = StringUtil.getInfo(search, "salesMan");
        String remark = StringUtil.getInfo(search, "remark");
        List<DepotHeadVo4List> list = depotHeadService.select(type, subType, hasDebt, status, purchaseStatus, number, linkApply, linkNumber,
                beginTime, endTime, materialParam, organId, creator, depotId, accountId, salesMan, remark);
        return getDataTable(list);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = depotHeadService.deleteDepotHead(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = depotHeadService.batchDeleteDepotHead(ids, request);
        return returnStr(objectMap, delete);
    }

    @PostMapping(value = "/forceCloseBatch")
    @ApiOperation(value = "寮哄埗缁撳崟")
    public String forceCloseBatch(@RequestBody JSONObject jsonObject, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String ids = jsonObject.getString("ids");
        int res = depotHeadService.batchForceClose(ids, request);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @PostMapping(value = "/forceClosePurchaseBatch")
    @ApiOperation(value = "寮哄埗缁撳崟-浠ラ攢瀹氳喘")
    public String forceClosePurchaseBatch(@RequestBody JSONObject jsonObject, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String ids = jsonObject.getString("ids");
        int res = depotHeadService.batchForceClosePurchase(ids, request);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鎵归噺璁剧疆鐘舵€?瀹℃牳鎴栬€呭弽瀹℃牳
     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "鎵归噺璁剧疆鐘舵€?瀹℃牳鎴栬€呭弽瀹℃牳")
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request) throws Exception{
        Map<String, Object> objectMap = new HashMap<>();
        String status = jsonObject.getString("status");
        String ids = jsonObject.getString("ids");
        int res = depotHeadService.batchSetStatus(status, ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鍏ュ簱鍑哄簱鏄庣粏鎺ュ彛
     * @param currentPage
     * @param pageSize
     * @param oId
     * @param number
     * @param materialParam
     * @param depotId
     * @param beginTime
     * @param endTime
     * @param type
     * @param request
     * @return
     */
    @GetMapping(value = "/findInOutDetail")
    @ApiOperation(value = "鍏ュ簱鍑哄簱鏄庣粏鎺ュ彛")
    public BaseResponseInfo findInOutDetail(@RequestParam("currentPage") Integer currentPage,
                                            @RequestParam("pageSize") Integer pageSize,
                                            @RequestParam(value = "organId", required = false) Integer oId,
                                            @RequestParam("number") String number,
                                            @RequestParam("materialParam") String materialParam,
                                            @RequestParam(value = "depotId", required = false) Long depotId,
                                            @RequestParam("beginTime") String beginTime,
                                            @RequestParam("endTime") String endTime,
                                            @RequestParam("type") String type,
                                            @RequestParam(value = "creator", required = false) Long creator,
                                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                                            @RequestParam(value = "organizationId", required = false) Long organizationId,
                                            @RequestParam("remark") String remark,
                                            @RequestParam(value = "column", required = false, defaultValue = "createTime") String column,
                                            @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                            HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Long> depotList = new ArrayList<>();
            if(depotId != null) {
                depotList.add(depotId);
            } else {
                //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
                JSONArray depotArr = depotService.findDepotByCurrentUser();
                for(Object obj: depotArr) {
                    JSONObject object = JSONObject.parseObject(obj.toString());
                    depotList.add(object.getLong("id"));
                }
            }
            List<DepotHeadVo4InDetail> resList = new ArrayList<DepotHeadVo4InDetail>();
            String [] creatorArray = depotHeadService.getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = depotHeadService.getCreatorArrayByOrg(organizationId);
            }
            String subType = "鍑哄簱".equals(type)? "閿€鍞? : "";
            String [] organArray = depotHeadService.getOrganArray(subType, "");
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
            List<DepotHeadVo4InDetail> list = depotHeadService.findInOutDetail(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, oId, StringUtil.toNull(number), creator, remark,
                    StringUtil.safeSqlParse(column), StringUtil.safeSqlParse(order), (currentPage-1)*pageSize, pageSize);
            int total = depotHeadService.findInOutDetailCount(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, oId, StringUtil.toNull(number), creator, remark);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            if (null != list) {
                resList.addAll(list);
            }
            map.put("rows", resList);
            DepotHeadVo4InDetail statistic = depotHeadService.findInOutDetailStatistic(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, oId, StringUtil.toNull(number), creator, remark);
            map.put("operNumberTotal", statistic.getOperNumber());
            map.put("allPriceTotal", statistic.getAllPrice());
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
     * 鍏ュ簱鍑哄簱姹囨€绘帴鍙?     * @param currentPage
     * @param pageSize
     * @param oId
     * @param materialParam
     * @param depotId
     * @param beginTime
     * @param endTime
     * @param type
     * @param request
     * @return
     */
    @GetMapping(value = "/findInOutMaterialCount")
    @ApiOperation(value = "鍏ュ簱鍑哄簱姹囨€绘帴鍙?)
    public BaseResponseInfo findInOutMaterialCount(@RequestParam("currentPage") Integer currentPage,
                                                   @RequestParam("pageSize") Integer pageSize,
                                                   @RequestParam(value = "organId", required = false) Integer oId,
                                                   @RequestParam("materialParam") String materialParam,
                                                   @RequestParam(value = "depotId", required = false) Long depotId,
                                                   @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                   @RequestParam(value = "organizationId", required = false) Long organizationId,
                                                   @RequestParam("beginTime") String beginTime,
                                                   @RequestParam("endTime") String endTime,
                                                   @RequestParam("type") String type,
                                                   @RequestParam(value = "column", required = false, defaultValue = "createTime") String column,
                                                   @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                                   HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Long> depotList = new ArrayList<>();
            if(depotId != null) {
                depotList.add(depotId);
            } else {
                //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
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
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
            List<DepotHeadVo4InOutMCount> list = depotHeadService.findInOutMaterialCount(beginTime, endTime, type, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, organizationId, oId, StringUtil.safeSqlParse(column), StringUtil.safeSqlParse(order),
                    (currentPage-1)*pageSize, pageSize);
            int total = depotHeadService.findInOutMaterialCountTotal(beginTime, endTime, type, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, organizationId, oId);
            map.put("total", total);
            map.put("rows", list);
            DepotHeadVo4InOutMCount statistic = depotHeadService.findInOutMaterialCountStatistic(beginTime, endTime, type, categoryList, forceFlag, inOutManageFlag,
                    StringUtil.toNull(materialParam), depotList, organizationId, oId);
            map.put("numSumTotal", statistic.getNumSum());
            map.put("priceSumTotal", statistic.getPriceSum());
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
     * 璋冩嫧鏄庣粏缁熻
     * @param currentPage
     * @param pageSize
     * @param number
     * @param materialParam
     * @param depotIdF  璋冨嚭浠撳簱
     * @param depotId  璋冨叆浠撳簱
     * @param beginTime
     * @param endTime
     * @param subType
     * @param request
     * @return
     */
    @GetMapping(value = "/findAllocationDetail")
    @ApiOperation(value = "璋冩嫧鏄庣粏缁熻")
    public BaseResponseInfo findallocationDetail(@RequestParam("currentPage") Integer currentPage,
                                                 @RequestParam("pageSize") Integer pageSize,
                                                 @RequestParam("number") String number,
                                                 @RequestParam("materialParam") String materialParam,
                                                 @RequestParam(value = "depotId", required = false) Long depotId,
                                                 @RequestParam(value = "depotIdF", required = false) Long depotIdF,
                                                 @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                 @RequestParam(value = "organizationId", required = false) Long organizationId,
                                                 @RequestParam("beginTime") String beginTime,
                                                 @RequestParam("endTime") String endTime,
                                                 @RequestParam("subType") String subType,
                                                 @RequestParam("remark") String remark,
                                                 @RequestParam(value = "column", required = false, defaultValue = "createTime") String column,
                                                 @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                                 HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Long> depotList = new ArrayList<>();
            List<Long> depotFList = new ArrayList<>();
            if(depotId != null) {
                depotList.add(depotId);
            } else {
                //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
                JSONArray depotArr = depotService.findDepotByCurrentUser();
                for(Object obj: depotArr) {
                    JSONObject object = JSONObject.parseObject(obj.toString());
                    depotList.add(object.getLong("id"));
                }
            }
            if(depotIdF != null) {
                depotFList.add(depotIdF);
            } else {
                //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
                JSONArray depotArr = depotService.findDepotByCurrentUser();
                for(Object obj: depotArr) {
                    JSONObject object = JSONObject.parseObject(obj.toString());
                    depotFList.add(object.getLong("id"));
                }
            }
            String [] creatorArray = depotHeadService.getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = depotHeadService.getCreatorArrayByOrg(organizationId);
            }
            List<Long> categoryList = new ArrayList<>();
            if(categoryId != null){
                categoryList = materialService.getListByParentId(categoryId);
            }
            beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            List<DepotHeadVo4InDetail> list = depotHeadService.findAllocationDetail(beginTime, endTime, subType, StringUtil.toNull(number),
                    creatorArray, categoryList, forceFlag, StringUtil.toNull(materialParam), depotList, depotFList, remark,
                    StringUtil.safeSqlParse(column), StringUtil.safeSqlParse(order), (currentPage-1)*pageSize, pageSize);
            int total = depotHeadService.findAllocationDetailCount(beginTime, endTime, subType, StringUtil.toNull(number),
                    creatorArray, categoryList, forceFlag, StringUtil.toNull(materialParam), depotList, depotFList, remark);
            map.put("rows", list);
            map.put("total", total);
            DepotHeadVo4InDetail statistic = depotHeadService.findAllocationStatistic(beginTime, endTime, subType, StringUtil.toNull(number),
                    creatorArray, categoryList, forceFlag, StringUtil.toNull(materialParam), depotList, depotFList, remark);
            map.put("operNumberTotal", statistic.getOperNumber());
            map.put("allPriceTotal", statistic.getAllPrice());
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
     * 瀵硅处鍗曟帴鍙?     * @param currentPage
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param organId
     * @param hasDebt 1-鏈夋瑺娆?0-鏃犳瑺娆?     * @param supplierType
     * @param request
     * @return
     */
    @GetMapping(value = "/getStatementAccount")
    @ApiOperation(value = "瀵硅处鍗曟帴鍙?)
    public BaseResponseInfo getStatementAccount(@RequestParam("currentPage") Integer currentPage,
                                                 @RequestParam("pageSize") Integer pageSize,
                                                 @RequestParam("beginTime") String beginTime,
                                                 @RequestParam("endTime") String endTime,
                                                 @RequestParam(value = "organId", required = false) Integer organId,
                                                 @RequestParam(value = "hasDebt", required = false) Integer hasDebt,
                                                 @RequestParam("supplierType") String supplierType,
                                                 HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String type = "";
            String subType = "";
            String typeBack = "";
            String subTypeBack = "";
            String billType = "";
            if (("渚涘簲鍟?).equals(supplierType)) {
                type = "鍏ュ簱";
                subType = "閲囪喘";
                typeBack = "鍑哄簱";
                subTypeBack = "閲囪喘閫€璐?;
                billType = "浠樻";
            } else if (("瀹㈡埛").equals(supplierType)) {
                type = "鍑哄簱";
                subType = "閿€鍞?;
                typeBack = "鍏ュ簱";
                subTypeBack = "閿€鍞€€璐?;
                billType = "鏀舵";
            }
            String [] organArray = depotHeadService.getOrganArray(subType, "");
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            List<DepotHeadVo4StatementAccount> list = depotHeadService.getStatementAccount(beginTime, endTime, organId, organArray,
                    hasDebt, supplierType, type, subType,typeBack, subTypeBack, billType, (currentPage-1)*pageSize, pageSize);
            int total = depotHeadService.getStatementAccountCount(beginTime, endTime, organId, organArray,
                    hasDebt, supplierType, type, subType,typeBack, subTypeBack, billType);
            for(DepotHeadVo4StatementAccount item: list) {
                //鏈熷垵 = 璧峰鏈熷垵閲戦+涓婃湡娆犳閲戦-涓婃湡閫€璐х殑娆犳閲戦-涓婃湡鏀朵粯娆?                BigDecimal preNeed = item.getBeginNeed().add(item.getPreDebtMoney()).subtract(item.getPreReturnDebtMoney()).subtract(item.getPreBackMoney());
                item.setPreNeed(preNeed);
                //瀹為檯娆犳 = 鏈湡娆犳-鏈湡閫€璐х殑娆犳閲戦
                BigDecimal realDebtMoney = item.getDebtMoney().subtract(item.getReturnDebtMoney());
                item.setDebtMoney(realDebtMoney);
                //鏈熸湯 = 鏈熷垵+瀹為檯娆犳-鏈湡鏀舵
                BigDecimal allNeedGet = preNeed.add(realDebtMoney).subtract(item.getBackMoney());
                item.setAllNeed(allNeedGet);
            }
            map.put("rows", list);
            map.put("total", total);
            List<DepotHeadVo4StatementAccount> totalPayList = depotHeadService.getStatementAccountTotalPay(beginTime, endTime, organId, organArray,
                    hasDebt, supplierType, type, subType, typeBack, subTypeBack, billType);
            if(totalPayList.size()>0) {
                DepotHeadVo4StatementAccount totalPayItem = totalPayList.get(0);
                BigDecimal firstMoney = BigDecimal.ZERO;
                BigDecimal lastMoney = BigDecimal.ZERO;
                if(totalPayItem!=null) {
                    //鏈熷垵 = 璧峰鏈熷垵閲戦+涓婃湡娆犳閲戦-涓婃湡閫€璐х殑娆犳閲戦-涓婃湡鏀朵粯娆?                    firstMoney = totalPayItem.getBeginNeed().add(totalPayItem.getPreDebtMoney()).subtract(totalPayItem.getPreReturnDebtMoney()).subtract(totalPayItem.getPreBackMoney());
                    //鏈熸湯 = 鏈熷垵+鏈湡娆犳-鏈湡閫€璐х殑娆犳閲戦-鏈湡鏀舵
                    lastMoney = firstMoney.add(totalPayItem.getDebtMoney()).subtract(totalPayItem.getReturnDebtMoney()).subtract(totalPayItem.getBackMoney());
                }
                map.put("firstMoney", firstMoney); //鏈熷垵
                map.put("lastMoney", lastMoney);  //鏈熸湯
            }
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
     * 鑾峰彇寰呮敹娆炬垨浠樻鐨勬潯鏁?     * @param request
     * @return
     */
    @GetMapping(value = "/getNeedCount")
    @ApiOperation(value = "鑾峰彇寰呮敹娆炬垨浠樻鐨勬潯鏁?)
    public BaseResponseInfo getNeedCount(@RequestParam("type") String type, HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<>();
        try {
            String supplierType = "";
            if (("vendor").equals(type)) {
                supplierType = "渚涘簲鍟?;
            } else if (("customer").equals(type)) {
                supplierType = "瀹㈡埛";
            }
            int needCount = depotHeadService.getNeedCount(supplierType);
            map.put("needCount", needCount);
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
     * 鏍规嵁缂栧彿鏌ヨ鍗曟嵁淇℃伅
     * @param number
     * @param request
     * @return
     */
    @GetMapping(value = "/getDetailByNumber")
    @ApiOperation(value = "鏍规嵁缂栧彿鏌ヨ鍗曟嵁淇℃伅")
    public BaseResponseInfo getDetailByNumber(@RequestParam("number") String number,
                                         HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        DepotHeadVo4List dhl = new DepotHeadVo4List();
        try {
            List<DepotHeadVo4List> list = depotHeadService.getDetailByNumber(number, request);
            if(list.size()>0) {
                dhl = list.get(0);
            }
            res.code = 200;
            res.data = dhl;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁鍘熷崟鍙锋煡璇㈠叧鑱旂殑鍗曟嵁鍒楄〃
     * @param number
     * @param request
     * @return
     */
    @GetMapping(value = "/getBillListByLinkNumber")
    @ApiOperation(value = "鏍规嵁鍘熷崟鍙锋煡璇㈠叧鑱旂殑鍗曟嵁鍒楄〃")
    public BaseResponseInfo getBillListByLinkNumber(@RequestParam("number") String number,
                                              HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        DepotHead dh = new DepotHead();
        try {
            List<DepotHead> list = depotHeadService.getBillListByLinkNumber(number);
            res.code = 200;
            res.data = list;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏂板鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?     * @param body
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addDepotHeadAndDetail")
    @ApiOperation(value = "鏂板鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?)
    public Object addDepotHeadAndDetail(@RequestBody DepotHeadVo4Body body, HttpServletRequest request) throws  Exception{
        JSONObject result = ExceptionConstants.standardSuccess();
        String beanJson = body.getInfo();
        String rows = body.getRows();
        depotHeadService.addDepotHeadAndDetail(beanJson, rows, request);
        return result;
    }

    /**
     * 鏇存柊鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?     * @param body
     * @param request
     * @return
     * @throws Exception
     */
    @PutMapping(value = "/updateDepotHeadAndDetail")
    @ApiOperation(value = "鏇存柊鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?)
    public Object updateDepotHeadAndDetail(@RequestBody DepotHeadVo4Body body, HttpServletRequest request) throws Exception{
        JSONObject result = ExceptionConstants.standardSuccess();
        String beanJson = body.getInfo();
        String rows = body.getRows();
        depotHeadService.updateDepotHeadAndDetail(beanJson,rows,request);
        return result;
    }

    /**
     * 缁熻浠婃棩閲囪喘棰濄€佹槰鏃ラ噰璐銆佹湰鏈堥噰璐銆佷粖骞撮噰璐|閿€鍞|闆跺敭棰?     * @param request
     * @return
     */
    @GetMapping(value = "/getBuyAndSaleStatistics")
    @ApiOperation(value = "缁熻浠婃棩閲囪喘棰濄€佹槰鏃ラ噰璐銆佹湰鏈堥噰璐銆佷粖骞撮噰璐|閿€鍞|闆跺敭棰?)
    public BaseResponseInfo getBuyAndSaleStatistics(HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> map = new HashMap<>();
            String loginName = userService.getCurrentUser().getLoginName();
            
            String today = Tools.getNow() + BusinessConstants.DAY_FIRST_TIME;
            String monthFirstDay = Tools.firstDayOfMonth(Tools.getCurrentMonth()) + BusinessConstants.DAY_FIRST_TIME;
            String yesterdayBegin = Tools.getYesterday() + BusinessConstants.DAY_FIRST_TIME;
            String yesterdayEnd = Tools.getYesterday() + BusinessConstants.DAY_LAST_TIME;
            String yearBegin = Tools.getYearBegin() + BusinessConstants.DAY_FIRST_TIME;
            String yearEnd = Tools.getYearEnd() + BusinessConstants.DAY_LAST_TIME;
            map = depotHeadService.getBuyAndSaleStatistics(today, monthFirstDay, yesterdayBegin, yesterdayEnd, yearBegin, yearEnd, request);
            
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
     * 鏍规嵁褰撳墠鐢ㄦ埛鑾峰彇鎿嶄綔鍛樻暟缁勶紝鐢ㄤ簬鎺у埗褰撳墠鐢ㄦ埛鐨勬暟鎹潈闄愶紝闄愬埗鍙互鐪嬪埌鐨勫崟鎹寖鍥?     * 娉ㄦ剰锛氳鎺ュ彛鎻愪緵缁欓儴鍒嗘彃浠朵娇鐢紝鍕垮垹
     * @param request
     * @return
     */
    @GetMapping(value = "/getCreatorByCurrentUser")
    @ApiOperation(value = "鏍规嵁褰撳墠鐢ㄦ埛鑾峰彇鎿嶄綔鍛樻暟缁?)
    public BaseResponseInfo getCreatorByCurrentUser(HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String creator = depotHeadService.getCreatorByCurrentUser();
            res.code = 200;
            res.data = creator;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏌ヨ瀛樺湪娆犳鐨勫崟鎹?     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/debtList")
    @ApiOperation(value = "鏌ヨ瀛樺湪娆犳鐨勫崟鎹?)
    public String debtList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                           @RequestParam("currentPage") Integer currentPage,
                           @RequestParam("pageSize") Integer pageSize,
                           HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String organIdStr = StringUtil.getInfo(search, "organId");
        Long organId = Long.parseLong(organIdStr);
        String materialParam = StringUtil.getInfo(search, "materialParam");
        String number = StringUtil.getInfo(search, "number");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        String status = StringUtil.getInfo(search, "status");
        List<DepotHeadVo4List> list = depotHeadService.debtList(organId, materialParam, number, beginTime, endTime,
                status, (currentPage-1)*pageSize, pageSize);
        int total = depotHeadService.debtListCount(organId, materialParam, number, beginTime, endTime, status);
        if (list != null) {
            objectMap.put("rows", list);
            objectMap.put("total", total);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            objectMap.put("rows", new ArrayList<>());
            objectMap.put("total", 0);
            return returnJson(objectMap, "鏌ユ壘涓嶅埌鏁版嵁", ErpInfo.OK.code);
        }
    }

    /**
     * 瀵煎嚭瀛樺湪娆犳鐨勫崟鎹?     * @param organId
     * @param materialParam
     * @param number
     * @param type
     * @param subType
     * @param beginTime
     * @param endTime
     * @param status
     * @param mpList
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping(value = "/debtExport")
    @ApiOperation(value = "瀵煎嚭瀛樺湪娆犳鐨勫崟鎹?)
    public void debtExport(@RequestParam(value = "organId", required = false) Long organId,
                           @RequestParam(value = "materialParam", required = false) String materialParam,
                           @RequestParam(value = "number", required = false) String number,
                           @RequestParam(value = "type", required = false) String type,
                           @RequestParam(value = "subType", required = false) String subType,
                           @RequestParam(value = "beginTime", required = false) String beginTime,
                           @RequestParam(value = "endTime", required = false) String endTime,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "mpList", required = false) String mpList,
                           HttpServletRequest request, HttpServletResponse response)throws Exception {
        try {
            depotHeadService.debtExport(organId, materialParam, number, type, subType, beginTime, endTime,
                    status, mpList, request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 鏌ヨ绛夊緟鍏ュ簱鎴栧嚭搴撶殑鍗曟嵁
     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/waitBillList")
    @ApiOperation(value = "鏌ヨ绛夊緟鍏ュ簱鎴栧嚭搴撶殑鍗曟嵁")
    public String waitBillList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                           @RequestParam("currentPage") Integer currentPage,
                           @RequestParam("pageSize") Integer pageSize,
                           HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String number = StringUtil.getInfo(search, "number");
        String materialParam = StringUtil.getInfo(search, "materialParam");
        String type = StringUtil.getInfo(search, "type");
        String subType = StringUtil.getInfo(search, "subType");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        String status = StringUtil.getInfo(search, "status");
        List<DepotHeadVo4List> list = depotHeadService.waitBillList(number, materialParam, type, subType, beginTime, endTime,
                status, (currentPage-1)*pageSize, pageSize);
        long total = depotHeadService.waitBillCount(number, materialParam, type, subType, beginTime, endTime, status);
        if (list != null) {
            objectMap.put("rows", list);
            objectMap.put("total", total);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            objectMap.put("rows", new ArrayList<>());
            objectMap.put("total", 0);
            return returnJson(objectMap, "鏌ユ壘涓嶅埌鏁版嵁", ErpInfo.OK.code);
        }
    }

    /**
     * 鏌ヨ绛夊緟鍏ュ簱鎴栧嚭搴撶殑鍗曟嵁鏁伴噺
     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/waitBillCount")
    @ApiOperation(value = "鏌ヨ绛夊緟鍏ュ簱鎴栧嚭搴撶殑鍗曟嵁鏁伴噺")
    public String waitBillCount(@RequestParam(value = Constants.SEARCH, required = false) String search,
                               HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String number = StringUtil.getInfo(search, "number");
        String materialParam = StringUtil.getInfo(search, "materialParam");
        String type = StringUtil.getInfo(search, "type");
        String subType = StringUtil.getInfo(search, "subType");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        String status = StringUtil.getInfo(search, "status");
        long total = depotHeadService.waitBillCount(number, materialParam, type, subType, beginTime, endTime, status);
        objectMap.put("total", total);
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鎵归噺鏂板鍏ュ簱鎴栧嚭搴撳崟鎹?     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchAddDepotHeadAndDetail")
    @ApiOperation(value = "鎵归噺鏂板鍏ュ簱鎴栧嚭搴撳崟鎹?)
    public Object batchAddDepotHeadAndDetail(@RequestBody JSONObject jsonObject,
                                             HttpServletRequest request) throws  Exception{
        JSONObject result = ExceptionConstants.standardSuccess();
        String ids = jsonObject.getString("ids");
        depotHeadService.batchAddDepotHeadAndDetail(ids, request);
        return result;
    }
}
