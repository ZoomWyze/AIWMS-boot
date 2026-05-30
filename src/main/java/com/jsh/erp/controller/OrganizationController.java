package com.jsh.erp.controller;


/**
 * 机构管理 Controller
 * 提供组织机构树的 CRUD 接口，支持树形结构查询
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.Organization;
import com.jsh.erp.datasource.vo.TreeNode;
import com.jsh.erp.service.OrganizationService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.ErpInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * create by: jsh
 */
@RestController
@RequestMapping(value = "/organization")
@Api(tags = {"鏈烘瀯绠＄悊"})
public class OrganizationController {
    private Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    @Resource
    private OrganizationService organizationService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Organization organization = organizationService.getOrganization(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(organization != null) {
            objectMap.put("info", organization);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = organizationService.insertOrganization(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = organizationService.updateOrganization(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = organizationService.deleteOrganization(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = organizationService.batchDeleteOrganization(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = organizationService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏍规嵁id鏉ユ煡璇㈡満鏋勪俊鎭?     * @param id
     * @param request
     * @return
     */
    @GetMapping(value = "/findById")
    @ApiOperation(value = "鏍规嵁id鏉ユ煡璇㈡満鏋勪俊鎭?)
    public BaseResponseInfo findById(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<Organization> dataList = organizationService.findById(id);
            JSONObject outer = new JSONObject();
            if (null != dataList) {
                for (Organization org : dataList) {
                    outer.put("id", org.getId());
                    outer.put("orgAbr", org.getOrgAbr());
                    outer.put("parentId", org.getParentId());
                    List<Organization> dataParentList = organizationService.findByParentId(org.getParentId());
                    if(dataParentList!=null&&dataParentList.size()>0){
                        //鐖剁骇鏈烘瀯鍚嶇О鏄剧ず绠€绉?                        outer.put("orgParentName", dataParentList.get(0).getOrgAbr());
                    }
                    outer.put("orgNo", org.getOrgNo());
                    outer.put("sort", org.getSort());
                    outer.put("remark", org.getRemark());
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
     * 鑾峰彇鏈烘瀯鏍戞暟鎹?     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getOrganizationTree")
    @ApiOperation(value = "鑾峰彇鏈烘瀯鏍戞暟鎹?)
    public JSONArray getOrganizationTree(@RequestParam("id") Long id) throws Exception{
       JSONArray arr=new JSONArray();
       List<TreeNode> organizationTree= organizationService.getOrganizationTree(id);
       if(organizationTree!=null&&organizationTree.size()>0){
           for(TreeNode node:organizationTree){
               String str=JSON.toJSONString(node);
               JSONObject obj=JSON.parseObject(str);
               arr.add(obj);
           }
       }
        return arr;
    }

    /**
     * 鏍规嵁鐢ㄦ埛鑾峰彇鍏ㄩ儴鏈烘瀯鏍?     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getAllOrganizationTreeByUser")
    @ApiOperation(value = "鏍规嵁鐢ㄦ埛鑾峰彇鍏ㄩ儴鏈烘瀯鏍?)
    public JSONArray getAllOrganizationTreeByUser(HttpServletRequest request) throws Exception{
        JSONArray arr = new JSONArray();
        Long userId = userService.getUserId(request);
        String roleType = userService.getRoleTypeByUserId(userId).getType();
        if(BusinessConstants.ROLE_TYPE_PUBLIC.equals(roleType)) {
            List<TreeNode> organizationTree = organizationService.getOrganizationTree(null);
            if(organizationTree!=null && organizationTree.size()>0){
                for(TreeNode node: organizationTree){
                    String str = JSON.toJSONString(node);
                    JSONObject obj = JSON.parseObject(str);
                    arr.add(obj);
                }
            }
        }
        return arr;
    }
}
