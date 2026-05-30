package com.jsh.erp.controller;


/**
 * 供应商/客户/会员管理 Controller
 * 提供供应商、客户、会员的 CRUD 接口，支持按类型分类查询
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.Supplier;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.vo.SupplierSimple;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.service.SupplierService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.service.UserBusinessService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji|sheng|hua 绠′紛浣砮rp
 */
@RestController
@RequestMapping(value = "/supplier")
@Api(tags = {"鍟嗗绠＄悊"})
public class SupplierController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Resource
    private SupplierService supplierService;

    @Resource
    private UserBusinessService userBusinessService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Supplier supplier = supplierService.getSupplier(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(supplier != null) {
            objectMap.put("info", supplier);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String supplier = StringUtil.getInfo(search, "supplier");
        String type = StringUtil.getInfo(search, "type");
        String contacts = StringUtil.getInfo(search, "contacts");
        String phonenum = StringUtil.getInfo(search, "phonenum");
        String telephone = StringUtil.getInfo(search, "telephone");
        List<Supplier> list = supplierService.select(supplier, type, contacts, phonenum, telephone);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = supplierService.insertSupplier(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = supplierService.updateSupplier(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierService.deleteSupplier(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierService.batchDeleteSupplier(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = supplierService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @GetMapping(value = "/checkIsNameAndTypeExist")
    @ApiOperation(value = "妫€鏌ュ悕绉板拰绫诲瀷鏄惁瀛樺湪")
    public String checkIsNameAndTypeExist(@RequestParam Long id,
                                          @RequestParam(value ="name", required = false) String name,
                                          @RequestParam(value ="type") String type,
                                          HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = supplierService.checkIsNameAndTypeExist(id, name, type);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏌ユ壘瀹㈡埛淇℃伅-涓嬫媺妗?     * @param request
     * @return
     */
    @PostMapping(value = "/findBySelect_cus")
    @ApiOperation(value = "鏌ユ壘瀹㈡埛淇℃伅")
    public JSONArray findBySelectCus(@RequestBody JSONObject jsonObject,
                                     HttpServletRequest request) {
        JSONArray arr = new JSONArray();
        try {
            String key = jsonObject.get("key")!=null ? jsonObject.getString("key") : null;
            Long organId = jsonObject.get("organId")!=null ? jsonObject.getLong("organId") : null;
            Integer limit = jsonObject.get("limit")!=null ? jsonObject.getInteger("limit") : null;
            String type = "UserCustomer";
            Long userId = userService.getUserId(request);
            //鑾峰彇鏉冮檺淇℃伅
            String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, userId.toString());
            List<Supplier> supplierList = supplierService.findBySelectCus(key, organId, limit);
            JSONArray dataArray = new JSONArray();
            if (null != supplierList) {
                boolean customerFlag = systemConfigService.getCustomerFlag();
                for (Supplier supplier : supplierList) {
                    JSONObject item = new JSONObject();
                    Boolean flag = ubValue.contains("[" + supplier.getId().toString() + "]");
                    if (!customerFlag || flag) {
                        item.put("id", supplier.getId());
                        item.put("supplier", supplier.getSupplier()); //瀹㈡埛鍚嶇О
                        dataArray.add(item);
                    }
                }
            }
            arr = dataArray;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鏌ユ壘渚涘簲鍟嗕俊鎭?涓嬫媺妗?     * @param request
     * @return
     */
    @PostMapping(value = "/findBySelect_sup")
    @ApiOperation(value = "鏌ユ壘渚涘簲鍟嗕俊鎭?)
    public JSONArray findBySelectSup(@RequestBody JSONObject jsonObject,
                                     HttpServletRequest request) throws Exception{
        JSONArray arr = new JSONArray();
        try {
            String key = jsonObject.get("key")!=null ? jsonObject.getString("key") : null;
            Long organId = jsonObject.get("organId")!=null ? jsonObject.getLong("organId") : null;
            Integer limit = jsonObject.get("limit")!=null ? jsonObject.getInteger("limit") : null;
            List<Supplier> supplierList = supplierService.findBySelectSup(key, organId, limit);
            JSONArray dataArray = new JSONArray();
            if (null != supplierList) {
                for (Supplier supplier : supplierList) {
                    JSONObject item = new JSONObject();
                    item.put("id", supplier.getId());
                    //渚涘簲鍟嗗悕绉?                    item.put("supplier", supplier.getSupplier());
                    dataArray.add(item);
                }
            }
            arr = dataArray;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鏌ユ壘寰€鏉ュ崟浣嶏紝鍚緵搴斿晢鍜屽鎴蜂俊鎭?涓嬫媺妗?     * @param request
     * @return
     */
    @PostMapping(value = "/findBySelect_organ")
    @ApiOperation(value = "鏌ユ壘寰€鏉ュ崟浣嶏紝鍚緵搴斿晢鍜屽鎴蜂俊鎭?)
    public JSONArray findBySelectOrgan(@RequestBody JSONObject jsonObject,
                                       HttpServletRequest request) throws Exception{
        JSONArray arr = new JSONArray();
        try {
            String key = jsonObject.get("key")!=null ? jsonObject.getString("key") : null;
            Long organId = jsonObject.get("organId")!=null ? jsonObject.getLong("organId") : null;
            Integer limit = jsonObject.get("limit")!=null ? jsonObject.getInteger("limit") : null;
            JSONArray dataArray = new JSONArray();
            //1銆佽幏鍙栦緵搴斿晢淇℃伅
            List<Supplier> supplierList = supplierService.findBySelectSup(key, organId, limit);
            if (null != supplierList) {
                for (Supplier supplier : supplierList) {
                    JSONObject item = new JSONObject();
                    item.put("id", supplier.getId());
                    item.put("supplier", supplier.getSupplier() + "[渚涘簲鍟哴"); //渚涘簲鍟嗗悕绉?                    dataArray.add(item);
                }
            }
            //2銆佽幏鍙栧鎴蜂俊鎭?            String type = "UserCustomer";
            Long userId = userService.getUserId(request);
            String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, userId.toString());
            List<Supplier> customerList = supplierService.findBySelectCus(key, organId, limit);
            if (null != customerList) {
                boolean customerFlag = systemConfigService.getCustomerFlag();
                for (Supplier supplier : customerList) {
                    JSONObject item = new JSONObject();
                    Boolean flag = ubValue.contains("[" + supplier.getId().toString() + "]");
                    if (!customerFlag || flag) {
                        item.put("id", supplier.getId());
                        item.put("supplier", supplier.getSupplier() + "[瀹㈡埛]"); //瀹㈡埛鍚嶇О
                        dataArray.add(item);
                    }
                }
            }
            arr = dataArray;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鏌ユ壘浼氬憳淇℃伅-涓嬫媺妗?     * @param request
     * @return
     */
    @PostMapping(value = "/findBySelect_retail")
    @ApiOperation(value = "鏌ユ壘浼氬憳淇℃伅")
    public JSONArray findBySelectRetail(@RequestBody JSONObject jsonObject,
                                        HttpServletRequest request)throws Exception {
        JSONArray arr = new JSONArray();
        try {
            String key = jsonObject.get("key")!=null ? jsonObject.getString("key") : null;
            Long organId = jsonObject.get("organId")!=null ? jsonObject.getLong("organId") : null;
            Integer limit = jsonObject.get("limit")!=null ? jsonObject.getInteger("limit") : null;
            List<Supplier> supplierList = supplierService.findBySelectRetail(key, organId, limit);
            JSONArray dataArray = new JSONArray();
            if (null != supplierList) {
                for (Supplier supplier : supplierList) {
                    JSONObject item = new JSONObject();
                    item.put("id", supplier.getId());
                    //瀹㈡埛鍚嶇О
                    item.put("supplier", supplier.getSupplier());
                    item.put("advanceIn", supplier.getAdvanceIn()); //棰勪粯娆鹃噾棰?                    dataArray.add(item);
                }
            }
            arr = dataArray;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鎵归噺璁剧疆鐘舵€?鍚敤鎴栬€呯鐢?     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "鎵归噺璁剧疆鐘舵€?)
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request)throws Exception {
        Boolean status = jsonObject.getBoolean("status");
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = supplierService.batchSetStatus(status, ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鑾峰彇鍏ㄩ儴瀹㈡埛淇℃伅
     * @param search
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getAllCustomer")
    @ApiOperation(value = "鑾峰彇鍏ㄩ儴瀹㈡埛淇℃伅")
    public TableDataInfo getAllCustomer(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                        HttpServletRequest request)throws Exception {
        List<SupplierSimple> list = supplierService.getAllCustomer();
        return getDataTable(list);
    }

    /**
     * 鑾峰彇鐢ㄦ埛瀵瑰簲瀹㈡埛鐨勫叧绯绘暟缁?     * @param type
     * @param keyId
     * @param request
     * @return
     */
    @GetMapping(value = "/getUserCustomerValue")
    @ApiOperation(value = "鑾峰彇鐢ㄦ埛瀵瑰簲瀹㈡埛鐨勫叧绯绘暟缁?)
    public JSONObject getUserCustomerValue(@RequestParam("UBType") String type, @RequestParam("UBKeyId") String keyId,
                                           HttpServletRequest request) throws Exception{
        JSONObject obj = new JSONObject();
        try {
            //鑾峰彇鏉冮檺淇℃伅
            String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, keyId);
            if(StringUtil.isNotEmpty(ubValue)) {
                String ubStr = ubValue.substring(1, ubValue.length()-1);
                String [] ubArr = ubStr.split("]\\[");
                Long[] ubLongArray = new Long[ubArr.length];
                for (int i = 0; i < ubArr.length; i++) {
                    ubLongArray[i] = Long.parseLong(ubArr[i]);
                }
                obj.put("data", ubLongArray);
            } else {
                obj.put("data", null);
            }
            obj.put("code", 200);
        } catch (Exception e) {
            obj.put("code", 500);
            obj.put("data", "鏈嶅姟鍐呴儴閿欒");
            logger.error(e.getMessage(), e);
        }
        return obj;
    }

    /**
     * 鏍规嵁瀹㈡埛鎴栦緵搴斿晢鏌ヨ鏈熷垵銆佹湡鍒濆凡鏀剁瓑淇℃伅
     * @param organId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getBeginNeedByOrganId")
    @ApiOperation(value = "鏍规嵁瀹㈡埛鎴栦緵搴斿晢鏌ヨ鏈熷垵銆佹湡鍒濆凡鏀剁瓑淇℃伅")
    public BaseResponseInfo getBeginNeedByOrganId(@RequestParam("organId") Long organId,
                                        HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> map = supplierService.getBeginNeedByOrganId(organId);
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
     * 瀵煎叆渚涘簲鍟?     * @param file
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/importVendor")
    @ApiOperation(value = "瀵煎叆渚涘簲鍟?)
    public BaseResponseInfo importVendor(MultipartFile file,
                            HttpServletRequest request, HttpServletResponse response) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            supplierService.checkFileExt(file);
            supplierService.importVendor(file, request);
            res.code = 200;
            res.data = "瀵煎叆鎴愬姛";
        } catch(BusinessRunTimeException e) {
            res.code = e.getCode();
            res.data = e.getData().get("message");
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "瀵煎叆澶辫触";
        }
        return res;
    }

    /**
     * 瀵煎叆瀹㈡埛
     * @param file
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/importCustomer")
    @ApiOperation(value = "瀵煎叆瀹㈡埛")
    public BaseResponseInfo importCustomer(MultipartFile file,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            supplierService.checkFileExt(file);
            supplierService.importCustomer(file, request);
            res.code = 200;
            res.data = "瀵煎叆鎴愬姛";
        } catch(BusinessRunTimeException e) {
            res.code = e.getCode();
            res.data = e.getData().get("message");
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "瀵煎叆澶辫触";
        }
        return res;
    }

    /**
     * 瀵煎叆浼氬憳
     * @param file
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/importMember")
    @ApiOperation(value = "瀵煎叆浼氬憳")
    public BaseResponseInfo importMember(MultipartFile file,
                                           HttpServletRequest request, HttpServletResponse response) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            supplierService.checkFileExt(file);
            supplierService.importMember(file, request);
            res.code = 200;
            res.data = "瀵煎叆鎴愬姛";
        } catch(BusinessRunTimeException e) {
            res.code = e.getCode();
            res.data = e.getData().get("message");
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "瀵煎叆澶辫触";
        }
        return res;
    }

    /**
     * 鐢熸垚excel琛ㄦ牸
     * @param supplier
     * @param type
     * @param phonenum
     * @param telephone
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/exportExcel")
    public void exportExcel(@RequestParam(value = "supplier", required = false) String supplier,
                            @RequestParam("type") String type,
                            @RequestParam(value = "phonenum", required = false) String phonenum,
                            @RequestParam(value = "telephone", required = false) String telephone,
                            HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Supplier> dataList = supplierService.findByAll(supplier, type, phonenum, telephone);
            File file = supplierService.exportExcel(dataList, type);
            ExcelUtils.downloadExcel(file, file.getName(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 鎵归噺璁剧疆浼氬憳褰撳墠鐨勯浠樻
     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/batchSetAdvanceIn")
    @ApiOperation(value = "鎵归噺璁剧疆浼氬憳褰撳墠鐨勯浠樻")
    public String batchSetAdvanceIn(@RequestBody JSONObject jsonObject,
                                    HttpServletRequest request)throws Exception {
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = supplierService.batchSetAdvanceIn(ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/getInfoByName")
    @ApiOperation(value = "鏍规嵁鍚嶇О鑾峰彇淇℃伅")
    public String getInfoByName(@RequestParam("name") String name,
                                @RequestParam("type") String type,
                                HttpServletRequest request) throws Exception {
        Supplier supplier = supplierService.getInfoByName(name, type);
        Map<String, Object> objectMap = new HashMap<>();
        if(supplier != null) {
            objectMap.put("info", supplier);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

}
