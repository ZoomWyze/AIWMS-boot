package com.jsh.erp.controller;


/**
 * 商品（物料）管理 Controller
 * 提供商品信息的 CRUD 接口，包括：新增/编辑/查询/删除/批量更新/条码校验/拼音转换
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.vo.MaterialDepotStock;
import com.jsh.erp.service.DepotService;
import com.jsh.erp.service.DepotItemService;
import com.jsh.erp.service.MaterialService;
import com.jsh.erp.service.RoleService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UnitService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji|sheng|hua jshERP
 */
@RestController
@RequestMapping(value = "/material")
@Api(tags = {"鍟嗗搧绠＄悊"})
public class MaterialController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(MaterialController.class);

    @Resource
    private MaterialService materialService;

    @Resource
    private DepotItemService depotItemService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UnitService unitService;

    @Resource
    private DepotService depotService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @Value(value="${file.uploadType}")
    private Long fileUploadType;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Material material = materialService.getMaterial(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(material != null) {
            objectMap.put("info", material);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String categoryId = StringUtil.getInfo(search, "categoryId");
        String materialParam = StringUtil.getInfo(search, "materialParam");
        String standard = StringUtil.getInfo(search, "standard");
        String model = StringUtil.getInfo(search, "model");
        String color = StringUtil.getInfo(search, "color");
        String brand = StringUtil.getInfo(search, "brand");
        String mfrs = StringUtil.getInfo(search, "mfrs");
        String otherField1 = StringUtil.getInfo(search, "otherField1");
        String otherField2 = StringUtil.getInfo(search, "otherField2");
        String otherField3 = StringUtil.getInfo(search, "otherField3");
        String weight = StringUtil.getInfo(search, "weight");
        String expiryNum = StringUtil.getInfo(search, "expiryNum");
        String enableSerialNumber = StringUtil.getInfo(search, "enableSerialNumber");
        String enableBatchNumber = StringUtil.getInfo(search, "enableBatchNumber");
        String position = StringUtil.getInfo(search, "position");
        String enabled = StringUtil.getInfo(search, "enabled");
        String remark = StringUtil.getInfo(search, "remark");
        String mpList = StringUtil.getInfo(search, "mpList");
        List<MaterialVo4Unit> list = materialService.select(materialParam, standard, model, color, brand, mfrs, otherField1, otherField2,
                otherField3, weight, expiryNum, enableSerialNumber, enableBatchNumber, position, enabled, remark, categoryId, mpList);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = materialService.insertMaterial(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = materialService.updateMaterial(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialService.deleteMaterial(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialService.batchDeleteMaterial(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = materialService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 妫€鏌ュ晢鍝佹槸鍚﹀瓨鍦?     * @param id
     * @param name
     * @param model
     * @param color
     * @param standard
     * @param mfrs
     * @param otherField1
     * @param otherField2
     * @param otherField3
     * @param unit
     * @param unitId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/checkIsExist")
    @ApiOperation(value = "妫€鏌ュ晢鍝佹槸鍚﹀瓨鍦?)
    public String checkIsExist(@RequestParam("id") Long id, @RequestParam("name") String name,
                               @RequestParam("model") String model, @RequestParam("color") String color,
                               @RequestParam("standard") String standard, @RequestParam("mfrs") String mfrs,
                               @RequestParam("otherField1") String otherField1, @RequestParam("otherField2") String otherField2,
                               @RequestParam("otherField3") String otherField3, @RequestParam("unit") String unit,@RequestParam("unitId") Long unitId,
                               HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        int exist = materialService.checkIsExist(id, name, StringUtil.toNull(model), StringUtil.toNull(color),
                StringUtil.toNull(standard), StringUtil.toNull(mfrs), StringUtil.toNull(otherField1),
                StringUtil.toNull(otherField2), StringUtil.toNull(otherField3), StringUtil.toNull(unit), unitId);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鎵归噺璁剧疆鐘舵€?鍚敤鎴栬€呯鐢?     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "鎵归噺璁剧疆鐘舵€?鍚敤鎴栬€呯鐢?)
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request)throws Exception {
        Boolean status = jsonObject.getBoolean("status");
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = materialService.batchSetStatus(status, ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鏍规嵁id鏉ユ煡璇㈠晢鍝佸悕绉?     * @param id
     * @param request
     * @return
     */
    @GetMapping(value = "/findById")
    @ApiOperation(value = "鏍规嵁id鏉ユ煡璇㈠晢鍝佸悕绉?)
    public BaseResponseInfo findById(@RequestParam("id") Long id, HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<MaterialVo4Unit> list = materialService.findById(id);
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
     * 鏍规嵁meId鏉ユ煡璇㈠晢鍝佸悕绉?     * @param meId
     * @param request
     * @return
     */
    @GetMapping(value = "/findByIdWithBarCode")
    @ApiOperation(value = "鏍规嵁meId鏉ユ煡璇㈠晢鍝佸悕绉?)
    public BaseResponseInfo findByIdWithBarCode(@RequestParam("meId") Long meId,
                                                @RequestParam("mpList") String mpList,
                                                HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String[] mpArr = mpList.split(",");
            MaterialVo4Unit mu = new MaterialVo4Unit();
            List<MaterialVo4Unit> list = materialService.findByIdWithBarCode(meId);
            if(list!=null && list.size()>0) {
                mu = list.get(0);
                mu.setMaterialOther(materialService.getMaterialOtherByParam(mpArr, mu));
            }
            res.code = 200;
            res.data = mu;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁鍏抽敭璇嶆煡鎵惧晢鍝佷俊鎭?鏉＄爜銆佸悕绉般€佽鏍笺€佸瀷鍙?     * @param q
     * @param request
     * @return
     */
    @GetMapping(value = "/getMaterialByParam")
    @ApiOperation(value = "鏍规嵁鍏抽敭璇嶆煡鎵惧晢鍝佷俊鎭?)
    public BaseResponseInfo getMaterialByParam(@RequestParam("q") String q,
                                   HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            JSONArray arr = materialService.getMaterialByParam(q);
            res.code = 200;
            res.data = arr;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏌ユ壘鍟嗗搧淇℃伅-涓嬫媺妗?     * @param mpList
     * @param request
     * @return
     */
    @GetMapping(value = "/findBySelect")
    @ApiOperation(value = "鏌ユ壘鍟嗗搧淇℃伅")
    public JSONObject findBySelect(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                  @RequestParam(value = "q", required = false) String q,
                                  @RequestParam(value = "standardOrModel", required = false) String standardOrModel,
                                  @RequestParam(value = "mpList", required = false) String mpList,
                                  @RequestParam(value = "depotId", required = false) Long depotId,
                                  @RequestParam(value = "color", required = false) String color,
                                  @RequestParam(value = "brand", required = false) String brand,
                                  @RequestParam(value = "mfrs", required = false) String mfrs,
                                  @RequestParam(value = "otherField1", required = false) String otherField1,
                                  @RequestParam(value = "otherField2", required = false) String otherField2,
                                  @RequestParam(value = "otherField3", required = false) String otherField3,
                                  @RequestParam(value = "enableSerialNumber", required = false) String enableSerialNumber,
                                  @RequestParam(value = "enableBatchNumber", required = false) String enableBatchNumber,
                                  @RequestParam("page") Integer currentPage,
                                  @RequestParam("rows") Integer pageSize,
                                  HttpServletRequest request) throws Exception{
        JSONObject object = new JSONObject();
        try {
            String[] mpArr = new String[]{};
            if(StringUtil.isNotEmpty(mpList)){
                mpArr= mpList.split(",");
            }
            List<MaterialVo4Unit> dataList = materialService.findBySelectWithBarCode(categoryId, q, StringUtil.toNull(standardOrModel),
                    StringUtil.toNull(color), StringUtil.toNull(brand), StringUtil.toNull(mfrs), StringUtil.toNull(otherField1), StringUtil.toNull(otherField2), StringUtil.toNull(otherField3),
                    enableSerialNumber, enableBatchNumber, (currentPage-1)*pageSize, pageSize);
            int total = materialService.findBySelectWithBarCodeCount(categoryId, q, StringUtil.toNull(standardOrModel),
                    StringUtil.toNull(color), StringUtil.toNull(brand), StringUtil.toNull(mfrs), StringUtil.toNull(otherField1), StringUtil.toNull(otherField2), StringUtil.toNull(otherField3),
                    enableSerialNumber, enableBatchNumber);
            object.put("total", total);
            JSONArray dataArray = new JSONArray();
            //瀛樻斁鏁版嵁json鏁扮粍
            if (null != dataList) {
                for (MaterialVo4Unit material : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", material.getMeId()); //鍟嗗搧鎵╁睍琛ㄧ殑id
                    String ratioStr = ""; //姣斾緥
                    Unit unit = new Unit();
                    if (material.getUnitId() == null) {
                        ratioStr = "";
                    } else {
                        unit = unitService.getUnit(material.getUnitId());
                        //鎷兼帴鍓崟浣嶇殑姣斾緥
                        String commodityUnit = material.getCommodityUnit();
                        if(StringUtil.isNotEmpty(commodityUnit) && unit!=null) {
                            if(commodityUnit.equals(unit.getBasicUnit())) {
                                ratioStr = "[鍩烘湰]";
                            }
                            if(commodityUnit.equals(unit.getOtherUnit()) && unit.getRatio()!=null) {
                                ratioStr = "[" + unit.getRatio().stripTrailingZeros().toPlainString() + unit.getBasicUnit() + "]";
                            }
                            if(commodityUnit.equals(unit.getOtherUnitTwo()) && unit.getRatioTwo()!=null) {
                                ratioStr = "[" + unit.getRatioTwo().stripTrailingZeros().toPlainString() + unit.getBasicUnit() + "]";
                            }
                            if(commodityUnit.equals(unit.getOtherUnitThree()) && unit.getRatioThree()!=null) {
                                ratioStr = "[" + unit.getRatioThree().stripTrailingZeros().toPlainString() + unit.getBasicUnit() + "]";
                            }
                        }
                    }
                    item.put("mBarCode", material.getmBarCode());
                    item.put("name", material.getName());
                    item.put("mnemonic", material.getMnemonic());
                    item.put("categoryName", material.getCategoryName());
                    item.put("standard", material.getStandard());
                    item.put("model", material.getModel());
                    item.put("color", material.getColor());
                    item.put("brand", material.getBrand());
                    item.put("mfrs", material.getMfrs());
                    item.put("unit", material.getCommodityUnit() + ratioStr);
                    item.put("sku", material.getSku());
                    item.put("enableSerialNumber", material.getEnableSerialNumber());
                    item.put("enableBatchNumber", material.getEnableBatchNumber());
                    BigDecimal stock;
                    if(StringUtil.isNotEmpty(material.getSku())){
                        stock = depotItemService.getSkuStockByParam(depotId,material.getMeId(),null,null);
                    } else {
                        stock = depotItemService.getCurrentStockByParam(depotId, material.getId());
                        if (material.getUnitId()!=null){
                            String commodityUnit = material.getCommodityUnit();
                            stock = unitService.parseStockByUnit(stock, unit, commodityUnit);
                        }
                    }
                    item.put("stock", stock);
                    item.put("expand", materialService.getMaterialOtherByParam(mpArr, material));
                    item.put("otherField1", material.getOtherField1());
                    item.put("otherField2", material.getOtherField2());
                    item.put("otherField3", material.getOtherField3());
                    item.put("imgName", material.getImgName());
                    if(fileUploadType == 2) {
                        item.put("imgSmall", "small");
                        item.put("imgLarge", "large");
                    } else {
                        item.put("imgSmall", "");
                        item.put("imgLarge", "");
                    }
                    dataArray.add(item);
                }
            }
            object.put("rows", dataArray);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return object;
    }

    /**
     * 鏍规嵁鍟嗗搧id鏌ユ壘鍟嗗搧淇℃伅
     * @param meId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getMaterialByMeId")
    @ApiOperation(value = "鏍规嵁鍟嗗搧id鏌ユ壘鍟嗗搧淇℃伅")
    public JSONObject getMaterialByMeId(@RequestParam(value = "meId", required = false) Long meId,
                                        @RequestParam("mpList") String mpList,
                                        HttpServletRequest request) throws Exception{
        JSONObject item = new JSONObject();
        try {
            String[] mpArr = mpList.split(",");
            List<MaterialVo4Unit> materialList = materialService.getMaterialByMeId(meId);
            if(materialList!=null && materialList.size()!=1) {
                return item;
            } else if(materialList.size() == 1) {
                MaterialVo4Unit material = materialList.get(0);
                item.put("Id", material.getMeId()); //鍟嗗搧鎵╁睍琛ㄧ殑id
                String ratio; //姣斾緥
                if (material.getUnitId() == null || material.getUnitId().equals("")) {
                    ratio = "";
                } else {
                    ratio = material.getUnitName();
                    ratio = ratio.substring(ratio.indexOf("("));
                }
                //鍚嶇О/鍨嬪彿/鎵╁睍淇℃伅/鍖呰
                String MaterialName = "";
                MaterialName = MaterialName + material.getmBarCode() + "_" + material.getName()
                        + ((material.getStandard() == null || material.getStandard().equals("")) ? "" : "(" + material.getStandard() + ")");
                String expand = materialService.getMaterialOtherByParam(mpArr, material); //鎵╁睍淇℃伅
                MaterialName = MaterialName + expand + ((material.getUnit() == null || material.getUnit().equals("")) ? "" : "(" + material.getUnit() + ")") + ratio;
                item.put("MaterialName", MaterialName);
                item.put("name", material.getName());
                item.put("expand", expand);
                item.put("model", material.getModel());
                item.put("standard", material.getStandard());
                item.put("unit", material.getUnit() + ratio);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return item;
    }

    /**
     * 鐢熸垚excel琛ㄦ牸
     * @param categoryId
     * @param materialParam
     * @param color
     * @param weight
     * @param expiryNum
     * @param enabled
     * @param enableSerialNumber
     * @param enableBatchNumber
     * @param remark
     * @param mpList
     * @param request
     * @param response
     */
    @GetMapping(value = "/exportExcel")
    @ApiOperation(value = "鐢熸垚excel琛ㄦ牸")
    public void exportExcel(@RequestParam(value = "categoryId", required = false) String categoryId,
                            @RequestParam(value = "materialParam", required = false) String materialParam,
                            @RequestParam(value = "color", required = false) String color,
                            @RequestParam(value = "materialOther", required = false) String materialOther,
                            @RequestParam(value = "weight", required = false) String weight,
                            @RequestParam(value = "expiryNum", required = false) String expiryNum,
                            @RequestParam(value = "enabled", required = false) String enabled,
                            @RequestParam(value = "enableSerialNumber", required = false) String enableSerialNumber,
                            @RequestParam(value = "enableBatchNumber", required = false) String enableBatchNumber,
                            @RequestParam(value = "remark", required = false) String remark,
                            @RequestParam(value = "mpList", required = false) String mpList,
                            HttpServletRequest request, HttpServletResponse response) {
        try {
            materialService.exportExcel(StringUtil.toNull(categoryId), StringUtil.toNull(materialParam), StringUtil.toNull(color),
                    StringUtil.toNull(materialOther), StringUtil.toNull(weight),
                    StringUtil.toNull(expiryNum), StringUtil.toNull(enabled), StringUtil.toNull(enableSerialNumber),
                    StringUtil.toNull(enableBatchNumber), StringUtil.toNull(remark), mpList, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * excel琛ㄦ牸瀵煎叆浜у搧锛堝惈鍒濆搴撳瓨锛?     * @param file
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/importExcel")
    @ApiOperation(value = "excel琛ㄦ牸瀵煎叆浜у搧")
    public BaseResponseInfo importExcel(MultipartFile file,
                            HttpServletRequest request, HttpServletResponse response) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            res = materialService.importExcel(file, request);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    /**
     * 鑾峰彇鍟嗗搧搴忓垪鍙?     * @param q
     * @param currentPage
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getMaterialEnableSerialNumberList")
    @ApiOperation(value = "鑾峰彇鍟嗗搧搴忓垪鍙?)
    public JSONObject getMaterialEnableSerialNumberList(
                                @RequestParam(value = "q", required = false) String q,
                                @RequestParam("page") Integer currentPage,
                                @RequestParam("rows") Integer pageSize,
                                HttpServletRequest request,
                                HttpServletResponse response)throws Exception {
        JSONObject object= new JSONObject();
        try {
            List<MaterialVo4Unit> list = materialService.getMaterialEnableSerialNumberList(q, (currentPage-1)*pageSize, pageSize);
            Long count = materialService.getMaterialEnableSerialNumberCount(q);
            object.put("rows", list);
            object.put("total", count);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return object;
    }

    /**
     * 鑾峰彇鏈€澶ф潯鐮?     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getMaxBarCode")
    @ApiOperation(value = "鑾峰彇鏈€澶ф潯鐮?)
    public BaseResponseInfo getMaxBarCode() throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        String barCode = materialService.getMaxBarCode();
        map.put("barCode", barCode);
        res.code = 200;
        res.data = map;
        return res;
    }

    /**
     * 鍟嗗搧鍚嶇О妯＄硦鍖归厤
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getMaterialNameList")
    @ApiOperation(value = "鍟嗗搧鍚嶇О妯＄硦鍖归厤")
    public JSONArray getMaterialNameList() throws Exception {
        JSONArray arr = new JSONArray();
        try {
            List<String> list = materialService.getMaterialNameList();
            for (String s : list) {
                JSONObject item = new JSONObject();
                item.put("value", s);
                item.put("text", s);
                arr.add(item);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鏍规嵁鏉＄爜鏌ヨ鍟嗗搧淇℃伅
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getMaterialByBarCode")
    @ApiOperation(value = "鏍规嵁鏉＄爜鏌ヨ鍟嗗搧淇℃伅")
    public BaseResponseInfo getMaterialByBarCode(@RequestParam("barCode") String barCode,
                                          @RequestParam(value = "organId", required = false) Long organId,
                                          @RequestParam(value = "depotId", required = false) Long depotId,
                                          @RequestParam("mpList") String mpList,
                                          @RequestParam(required = false, value = "prefixNo") String prefixNo,
                                          HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Long userId = userService.getUserId(request);
            String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            String[] mpArr = mpList.split(",");
            //鏀寔搴忓垪鍙锋煡璇紝鍏堟牴鎹簭鍒楀彿鏌ヨ鏉＄爜锛屽鏋滄煡涓嶅埌灏辩洿鎺ユ煡鏉＄爜
            MaterialExtend materialExtend = materialService.getMaterialExtendBySerialNumber(barCode);
            if(materialExtend!=null && StringUtil.isNotEmpty(materialExtend.getBarCode())) {
                barCode = materialExtend.getBarCode();
            }
            List<MaterialVo4Unit> list = materialService.getMaterialByBarCode(barCode);
            if(list!=null && list.size()>0) {
                for(MaterialVo4Unit mvo: list) {
                    mvo.setMaterialOther(materialService.getMaterialOtherByParam(mpArr, mvo));
                    if ("LSCK".equals(prefixNo) || "LSTH".equals(prefixNo)) {
                        //闆跺敭浠?                        mvo.setBillPrice(mvo.getCommodityDecimal());
                    } else if ("CGDD".equals(prefixNo) || "CGRK".equals(prefixNo) || "CGTH".equals(prefixNo)) {
                        //閲囪喘浠?                        mvo.setBillPrice(mvo.getPurchaseDecimal());
                    } else if("QTRK".equals(prefixNo) || "DBCK".equals(prefixNo) || "ZZD".equals(prefixNo) || "CXD".equals(prefixNo)
                            || "PDLR".equals(prefixNo) || "PDFP".equals(prefixNo)) {
                        //閲囪喘浠?缁欏綍鍏ョ晫闈㈡寜鏉冮檺灞忚斀
                        mvo.setBillPrice(roleService.parseBillPriceByLimit(mvo.getPurchaseDecimal(), "buy", priceLimit, request));
                    } else if ("XSDD".equals(prefixNo) || "XSCK".equals(prefixNo) || "XSTH".equals(prefixNo) || "QTCK".equals(prefixNo)) {
                        //閿€鍞环
                        if(organId == null) {
                            mvo.setBillPrice(mvo.getWholesaleDecimal());
                        } else {
                            if(systemConfigService.getCustomerStaticPriceFlag()) {
                                //宸茬粡寮€鍚簡瀹㈡埛闈欐€佸崟浠风殑寮€鍏?                                mvo.setBillPrice(mvo.getWholesaleDecimal());
                            } else {
                                //鏌ヨ鏈€鍚庝竴鍗曠殑閿€鍞环,瀹炵幇涓嶅悓鐨勫鎴蜂笉鍚岀殑閿€鍞环
                                BigDecimal lastUnitPrice = depotItemService.getLastUnitPriceByParam(organId, mvo.getMeId(), prefixNo);
                                mvo.setBillPrice(lastUnitPrice!=null? lastUnitPrice : mvo.getWholesaleDecimal());
                            }
                        }
                        //閿€鍞环-缁欏綍鍏ョ晫闈㈡寜鏉冮檺灞忚斀浠锋牸
                        if("QTCK".equals(prefixNo)) {
                            mvo.setBillPrice(roleService.parseBillPriceByLimit(mvo.getWholesaleDecimal(), "sale", priceLimit, request));
                        }
                    } else if ("other".equals(prefixNo)) {
                        //鍏跺畠闇€瑕佸～鍏呭晢鍝侀噰璐环鎴栬€呮垚鏈环鐨勫満鏅?                        if(systemConfigService.getMoveAvgPriceFlag()) {
                            BigDecimal currentUnitPrice = materialService.getCurrentUnitPriceByMaterialId(mvo.getId());
                            mvo.setBillPrice(currentUnitPrice);
                        } else {
                            mvo.setBillPrice(mvo.getPurchaseDecimal());
                        }
                    }
                    //浠撳簱id
                    if (depotId == null) {
                        JSONArray depotArr = depotService.findDepotByCurrentUser();
                        for (Object obj : depotArr) {
                            JSONObject depotObj = JSONObject.parseObject(obj.toString());
                            if (depotObj.get("isDefault") != null) {
                                Boolean isDefault = depotObj.getBoolean("isDefault");
                                if (isDefault) {
                                    Long id = depotObj.getLong("id");
                                    if (!"CGDD".equals(prefixNo) && !"XSDD".equals(prefixNo)) {
                                        //闄よ鍗曚箣澶栫殑鍗曟嵁鎵嶆湁浠撳簱
                                        mvo.setDepotId(id);
                                    }
                                    getStockByMaterialInfo(mvo);
                                }
                            }
                        }
                    } else {
                        mvo.setDepotId(depotId);
                        getStockByMaterialInfo(mvo);
                    }
                }
            }
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
     * 鏍规嵁鍟嗗搧淇℃伅鑾峰彇搴撳瓨锛岃繘琛岃祴鍊?     * @param mvo
     * @throws Exception
     */
    private void getStockByMaterialInfo(MaterialVo4Unit mvo) throws Exception {
        BigDecimal stock;
        if (StringUtil.isNotEmpty(mvo.getSku())) {
            stock = depotItemService.getSkuStockByParam(mvo.getDepotId(), mvo.getMeId(), null, null);
        } else {
            stock = depotItemService.getCurrentStockByParam(mvo.getDepotId(), mvo.getId());
            if (mvo.getUnitId() != null) {
                Unit unit = unitService.getUnit(mvo.getUnitId());
                String commodityUnit = mvo.getCommodityUnit();
                stock = unitService.parseStockByUnit(stock, unit, commodityUnit);
            }
        }
        mvo.setStock(stock);
    }

    /**
     * 鍟嗗搧搴撳瓨鏌ヨ
     * @param currentPage
     * @param pageSize
     * @param depotIds
     * @param categoryId
     * @param materialParam
     * @param zeroStock
     * @param column
     * @param order
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getListWithStock")
    @ApiOperation(value = "鍟嗗搧搴撳瓨鏌ヨ")
    public BaseResponseInfo getListWithStock(@RequestParam("currentPage") Integer currentPage,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam(value = "depotIds", required = false) String depotIds,
                                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                                             @RequestParam(value = "position", required = false) String position,
                                             @RequestParam("materialParam") String materialParam,
                                             @RequestParam("zeroStock") Integer zeroStock,
                                             @RequestParam(value = "column", required = false, defaultValue = "createTime") String column,
                                             @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                             HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<>();
        try {
            List<Long> idList = new ArrayList<>();
            List<Long> depotList = new ArrayList<>();
            if(categoryId != null){
                idList = materialService.getListByParentId(categoryId);
            }
            if(StringUtil.isNotEmpty(depotIds)) {
                depotList = StringUtil.strToLongList(depotIds);
            } else {
                //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
                JSONArray depotArr = depotService.findDepotByCurrentUser();
                for(Object obj: depotArr) {
                    JSONObject object = JSONObject.parseObject(obj.toString());
                    depotList.add(object.getLong("id"));
                }
            }
            Boolean moveAvgPriceFlag = systemConfigService.getMoveAvgPriceFlag();
            List<MaterialVo4Unit> dataList = materialService.getListWithStock(depotList, idList, StringUtil.toNull(position), StringUtil.toNull(materialParam),
                    moveAvgPriceFlag, zeroStock, StringUtil.safeSqlParse(column), StringUtil.safeSqlParse(order), (currentPage-1)*pageSize, pageSize);
            int total = materialService.getListWithStockCount(depotList, idList, StringUtil.toNull(position), StringUtil.toNull(materialParam), zeroStock);
            MaterialVo4Unit materialVo4Unit= materialService.getTotalStockAndPrice(depotList, idList, StringUtil.toNull(position), StringUtil.toNull(materialParam));
            map.put("total", total);
            map.put("currentStock", materialVo4Unit.getCurrentStock()!=null?materialVo4Unit.getCurrentStock():BigDecimal.ZERO);
            if(moveAvgPriceFlag) {
                map.put("currentStockPrice", materialVo4Unit.getCurrentStockMovePrice()!=null?materialVo4Unit.getCurrentStockMovePrice():BigDecimal.ZERO);
            } else {
                map.put("currentStockPrice", materialVo4Unit.getCurrentStockPrice()!=null?materialVo4Unit.getCurrentStockPrice():BigDecimal.ZERO);
            }
            map.put("currentWeight", materialVo4Unit.getCurrentWeight()!=null?materialVo4Unit.getCurrentWeight():BigDecimal.ZERO);
            map.put("rows", dataList);
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
     * 鎵归噺璁剧疆鍟嗗搧褰撳墠鐨勫疄鏃跺簱瀛橈紙鎸夋瘡涓粨搴擄級
     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchSetMaterialCurrentStock")
    @ApiOperation(value = "鎵归噺璁剧疆鍟嗗搧褰撳墠鐨勫疄鏃跺簱瀛橈紙鎸夋瘡涓粨搴擄級")
    public String batchSetMaterialCurrentStock(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request)throws Exception {
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        List<Depot> depotList = depotService.getAllList();
        if(depotList.isEmpty()) {
            return returnJson(objectMap, "璇峰厛鍒涘缓浠撳簱鍚庡啀鎿嶄綔", ErpInfo.WARING_MSG.code);
        }
        int res = materialService.batchSetMaterialCurrentStock(ids, depotList);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鎵归噺璁剧疆鍟嗗搧褰撳墠鐨勬垚鏈环
     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchSetMaterialCurrentUnitPrice")
    @ApiOperation(value = "鎵归噺璁剧疆鍟嗗搧褰撳墠鐨勬垚鏈环")
    public String batchSetMaterialCurrentUnitPrice(@RequestBody JSONObject jsonObject,
                                               HttpServletRequest request)throws Exception {
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = materialService.batchSetMaterialCurrentUnitPrice(ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鎵归噺鏇存柊鍟嗗搧淇℃伅
     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchUpdate")
    @ApiOperation(value = "鎵归噺鏇存柊鍟嗗搧淇℃伅")
    public String batchUpdate(@RequestBody JSONObject jsonObject,
                              HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int res = materialService.batchUpdate(jsonObject);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 杞崲鍚嶇О涓烘嫾闊?     * @param jsonObject
     */
    @PostMapping(value = "/changeNameToPinYin")
    @ApiOperation(value = "杞崲鍚嶇О涓烘嫾闊?)
    public BaseResponseInfo changeNameToPinYin(@RequestBody JSONObject jsonObject)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String name = jsonObject.getString("name");
            res.code = 200;
            res.data = PinYinUtil.getFirstLettersLo(name);
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠簱瀛樺垎甯冩儏鍐?     * @param mId
     * @param request
     * @return
     */
    @GetMapping(value = "/getMaterialDepotStock")
    @ApiOperation(value = "鏍规嵁浠撳簱鍜屽晢鍝佹煡璇㈠簱瀛樺垎甯冩儏鍐?)
    public TableDataInfo getMaterialDepotStock(
            @RequestParam(value = "depotIds",required = false) String depotIds,
            @RequestParam("materialId") Long mId,
            HttpServletRequest request)throws Exception {
        List<MaterialDepotStock> list = materialService.getMaterialDepotStock(depotIds, mId);
        return getDataTable(list);
    }
}