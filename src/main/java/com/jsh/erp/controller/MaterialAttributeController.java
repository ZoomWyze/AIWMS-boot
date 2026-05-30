package com.jsh.erp.controller;


/**
 * 商品多属性管理 Controller
 * 提供商品多属性（如颜色、尺码）的 CRUD 接口，支持属性名列表和属性值列表查询
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.MaterialAttribute;
import com.jsh.erp.service.MaterialAttributeService;
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
 * @author ji sheng hua jshERP
 */
@RestController
@RequestMapping(value = "/materialAttribute")
@Api(tags = {"鍟嗗搧灞炴€?})
public class MaterialAttributeController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(MaterialAttributeController.class);

    @Resource
    private MaterialAttributeService materialAttributeService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        MaterialAttribute materialAttribute = materialAttributeService.getMaterialAttribute(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(materialAttribute != null) {
            objectMap.put("info", materialAttribute);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String attributeName = StringUtil.getInfo(search, "attributeName");
        String attributeValue = StringUtil.getInfo(search, "attributeValue");
        List<MaterialAttribute> list = materialAttributeService.select(attributeName, attributeValue);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = materialAttributeService.insertMaterialAttribute(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = materialAttributeService.updateMaterialAttribute(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialAttributeService.deleteMaterialAttribute(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialAttributeService.batchDeleteMaterialAttribute(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = materialAttributeService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鑾峰彇鍟嗗搧灞炴€х殑鍚嶇О鍒楄〃
     * @param request
     * @return
     */
    @GetMapping(value = "/getNameList")
    @ApiOperation(value = "鑾峰彇鍟嗗搧灞炴€х殑鍚嶇О鍒楄〃")
    public JSONArray getNameList(HttpServletRequest request)throws Exception {
        JSONArray dataArray = new JSONArray();
        try {
            List<MaterialAttribute> materialAttributeList = materialAttributeService.getMaterialAttribute();
            if (null != materialAttributeList) {
                for (MaterialAttribute materialAttribute : materialAttributeList) {
                    JSONObject item = new JSONObject();
                    item.put("value", materialAttribute.getId().toString());
                    item.put("name", materialAttribute.getAttributeName());
                    dataArray.add(item);
                }
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return dataArray;
    }

    /**
     * 鑾峰彇id鏌ヨ灞炴€х殑鍊煎垪琛?
     * @param request
     * @return
     */
    @GetMapping(value = "/getValueListById")
    @ApiOperation(value = "鑾峰彇id鏌ヨ灞炴€х殑鍊煎垪琛?)
    public JSONArray getValueListById(@RequestParam("id") Long id,
                                     HttpServletRequest request)throws Exception {
        JSONArray dataArray = new JSONArray();
        try {
            dataArray = materialAttributeService.getValueArrById(id);
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return dataArray;
    }
}
