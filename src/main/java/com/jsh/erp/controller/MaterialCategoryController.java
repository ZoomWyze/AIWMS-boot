package com.jsh.erp.controller;


/**
 * 商品分类管理 Controller
 * 提供商品分类树的 CRUD 接口，支持树形结构查询
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.MaterialCategory;
import com.jsh.erp.datasource.vo.TreeNode;
import com.jsh.erp.service.MaterialCategoryService;
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
 * @author ji鈥攕heng鈥攈ua   jshERP
 */
@RestController
@RequestMapping(value = "/materialCategory")
@Api(tags = {"鍟嗗搧绫诲埆"})
public class MaterialCategoryController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(MaterialCategoryController.class);

    @Resource
    private MaterialCategoryService materialCategoryService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        MaterialCategory materialCategory = materialCategoryService.getMaterialCategory(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(materialCategory != null) {
            objectMap.put("info", materialCategory);
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
        Integer parentId = StringUtil.parseInteger(StringUtil.getInfo(search, "parentId"));
        List<MaterialCategory> list = materialCategoryService.select(name, parentId);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = materialCategoryService.insertMaterialCategory(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = materialCategoryService.updateMaterialCategory(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialCategoryService.deleteMaterialCategory(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = materialCategoryService.batchDeleteMaterialCategory(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id,
                                   @RequestParam(value ="name", required = false) String name,
                                   @RequestParam(value ="parentId", required = false) Long parentId,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = materialCategoryService.checkIsNameExist(id, name, parentId);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鑾峰彇鍏ㄩ儴鍟嗗搧绫诲埆
     * @param parentId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getAllList")
    @ApiOperation(value = "鑾峰彇鍏ㄩ儴鍟嗗搧绫诲埆")
    public BaseResponseInfo getAllList(@RequestParam("parentId") Long parentId, HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<MaterialCategory> materialCategoryList = materialCategoryService.getAllList(parentId);
            res.code = 200;
            res.data = materialCategoryList;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁id鏉ユ煡璇㈠晢鍝佸悕绉?     * @param id
     * @param request
     * @return
     */
    @GetMapping(value = "/findById")
    @ApiOperation(value = "鏍规嵁id鏉ユ煡璇㈠晢鍝佸悕绉?)
    public BaseResponseInfo findById(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<MaterialCategory> dataList = materialCategoryService.findById(id);
            JSONObject outer = new JSONObject();
            if (null != dataList) {
                for (MaterialCategory mc : dataList) {
                    outer.put("id", mc.getId());
                    outer.put("name", mc.getName());
                    outer.put("parentId", mc.getParentId());
                    List<MaterialCategory> dataParentList = materialCategoryService.findById(mc.getParentId());
                    if(dataParentList!=null&&dataParentList.size()>0){
                        outer.put("parentName", dataParentList.get(0).getName());
                    }
                    outer.put("sort", mc.getSort());
                    outer.put("serialNo", mc.getSerialNo());
                    outer.put("remark", mc.getRemark());
                }
            }
            res.code = 200;
            res.data = outer;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }
    /**
     * 鑾峰彇鍟嗗搧绫诲埆鏍戞暟鎹?     * @Param:
     * @return com.alibaba.fastjson.JSONArray
     */
    @RequestMapping(value = "/getMaterialCategoryTree")
    @ApiOperation(value = "鑾峰彇鍟嗗搧绫诲埆鏍戞暟鎹?)
    public JSONArray getMaterialCategoryTree(@RequestParam("id") Long id) throws Exception{
       JSONArray arr=new JSONArray();
       List<TreeNode> materialCategoryTree = materialCategoryService.getMaterialCategoryTree(id);
       if(materialCategoryTree!=null&&materialCategoryTree.size()>0){
           for(TreeNode node:materialCategoryTree){
               String str=JSON.toJSONString(node);
               JSONObject obj=JSON.parseObject(str);
               arr.add(obj) ;
           }
       }
        return arr;
    }
}
