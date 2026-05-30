package com.jsh.erp.controller;


/**
 * 商品属性管理 Controller
 * 提供商品属性（如基本属性、扩展属性）的 CRUD 接口
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.MaterialProperty;
import com.jsh.erp.service.MaterialPropertyService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.Constants;
import com.jsh.erp.utils.ErpInfo;
import com.jsh.erp.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * Description
 *
 * @Author: jsh
 * @Date: 2025/3/25 15:24
 */
@RestController
@RequestMapping(value = "/materialProperty")
@Api(tags = {"鍟嗗搧鎵╁睍瀛楁"})
public class MaterialPropertyController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(MaterialPropertyController.class);

    @Resource
    private MaterialPropertyService materialPropertyService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        MaterialProperty materialProperty = materialPropertyService.getMaterialProperty(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(materialProperty != null) {
            objectMap.put("info", materialProperty);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String name = StringUtil.getInfo(search, "name");
        List<MaterialProperty> list = materialPropertyService.select(name);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = materialPropertyService.insertMaterialProperty(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = materialPropertyService.updateMaterialProperty(obj, request);
        return returnStr(objectMap, update);
    }

    @PostMapping(value = "/addOrUpdate")
    @ApiOperation(value = "鏂板鎴栦慨鏀?)
    public String addOrUpdate(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String nativeName = obj.getString("nativeName");
        String anotherName = obj.getString("anotherName");
        boolean exist = materialPropertyService.checkIsNativeNameExist(nativeName);
        int res;
        if(!exist) {
            obj.put("id", null);
            res = materialPropertyService.insertMaterialProperty(obj, request);
        } else {
            res = materialPropertyService.updateMaterialPropertyByNativeName(nativeName, anotherName);
        }
        return returnStr(objectMap, res);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialPropertyService.deleteMaterialProperty(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialPropertyService.batchDeleteMaterialProperty(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = materialPropertyService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @GetMapping(value = "/getAllList")
    @ApiOperation(value = "鏌ヨ鍏ㄩ儴鍟嗗搧鎵╁睍瀛楁淇℃伅")
    public BaseResponseInfo getAllList(HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<MaterialProperty> list = materialPropertyService.getMaterialProperty();
            res.code = 200;
            res.data = list;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

}
