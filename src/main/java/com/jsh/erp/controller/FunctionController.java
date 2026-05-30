package com.jsh.erp.controller;


/**
 * 功能菜单管理 Controller
 * 提供系统功能/菜单的 CRUD 接口，用于权限管理和菜单树构建
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.service.FunctionService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UserBusinessService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.*;
import com.jsh.erp.utils.PermissionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji-sheng-hua  jshERP
 */
@RestController
@RequestMapping(value = "/function")
@Api(tags = {"鍔熻兘绠＄悊"})
public class FunctionController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(FunctionController.class);

    @Resource
    private FunctionService functionService;

    @Resource
    private UserService userService;

    @Resource
    private UserBusinessService userBusinessService;

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Function function = functionService.getFunction(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(function != null) {
            objectMap.put("info", function);
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
        String type = StringUtil.getInfo(search, "type");
        List<FunctionEx> list = functionService.select(name, type);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = functionService.insertFunction(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = functionService.updateFunction(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = functionService.deleteFunction(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = functionService.batchDeleteFunction(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = functionService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @GetMapping(value = "/checkIsNumberExist")
    @ApiOperation(value = "妫€鏌ョ紪鍙锋槸鍚﹀瓨鍦?)
    public String checkIsNumberExist(@RequestParam Long id,
                                     @RequestParam(value ="number", required = false) String number,
                                     HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        int exist = functionService.checkIsNumberExist(id, number);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏍规嵁鐖剁紪鍙锋煡璇㈣彍鍗?     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/findMenuByPNumber")
    @ApiOperation(value = "鏍规嵁鐖剁紪鍙锋煡璇㈣彍鍗?)
    public JSONArray findMenuByPNumber(@RequestBody JSONObject jsonObject,
                              HttpServletRequest request)throws Exception {
        String pNumber = jsonObject.getString("pNumber");
        String userId = jsonObject.getString("userId");
        //瀛樻斁鏁版嵁json鏁扮粍
        JSONArray dataArray = new JSONArray();
        try {
            Long roleId = 0L;
            String fc = "";
            List<UserBusiness> roleList = userBusinessService.getBasicData(userId, "UserRole");
            if(roleList!=null && roleList.size()>0){
                String value = roleList.get(0).getValue();
                if(StringUtil.isNotEmpty(value)){
                    String roleIdStr = value.replace("[", "").replace("]", "");
                    roleId = Long.parseLong(roleIdStr);
                }
            }
            //褰撳墠鐢ㄦ埛鎵€鎷ユ湁鐨勫姛鑳藉垪琛紝鏍煎紡濡傦細[1][2][5]
            List<UserBusiness> funList = userBusinessService.getBasicData(roleId.toString(), "RoleFunctions");
            if(funList!=null && funList.size()>0){
                fc = funList.get(0).getValue();
            }
            //鑾峰彇绯荤粺閰嶇疆淇℃伅-鏄惁寮€鍚绾у鏍?            String approvalFlag = "0";
            List<SystemConfig> list = systemConfigService.getSystemConfig();
            if(list.size()>0) {
                approvalFlag = list.get(0).getMultiLevelApprovalFlag();
            }

            List<Function> dataList = functionService.getRoleFunction(pNumber);
            if (dataList.size() != 0) {
                User userInfo = userService.getCurrentUser();
                //鑾峰彇褰撳墠鐢ㄦ埛鎵€灞炵殑绉熸埛鎵€鎷ユ湁鐨勫姛鑳絠d鐨刴ap
                Map<Long, Long> funIdMap = functionService.getCurrentTenantFunIdMap();
                dataArray = getMenuByFunction(dataList, fc, approvalFlag, funIdMap, userInfo);
                //澧炲姞棣栭〉鑿滃崟椤?                JSONObject homeItem = new JSONObject();
                homeItem.put("id", 0);
                homeItem.put("text", "棣栭〉");
                homeItem.put("icon", "home");
                homeItem.put("url", "/dashboard/analysis");
                homeItem.put("component", "/layouts/TabLayout");
                dataArray.add(0,homeItem);
            }
        } catch (DataAccessException e) {
            logger.error(">>>>>>>>>>>>>>>>>>>鏌ユ壘寮傚父", e);
        }
        return dataArray;
    }

    public JSONArray getMenuByFunction(List<Function> dataList, String fc, String approvalFlag, Map<Long, Long> funIdMap, User userInfo) throws Exception {
        JSONArray dataArray = new JSONArray();
        for (Function function : dataList) {
            //濡傛灉涓嶆槸瓒呯涔熶笉鏄鎴峰氨闇€瑕佹牎楠岋紝闃叉鍒嗛厤涓嬬骇鐢ㄦ埛鐨勫姛鑳芥潈闄愶紝澶т簬绉熸埛鐨勬潈闄?            if (PermissionUtil.isDefaultManager(userInfo) || userInfo.getId().equals(userInfo.getTenantId()) || funIdMap.get(function.getId())!=null) {
                //濡傛灉鍏抽棴澶氱骇瀹℃牳锛岄亣鍒颁换鍔″鏍歌彍鍗曠洿鎺ヨ烦杩?                if("0".equals(approvalFlag) && "/workflow".equals(function.getUrl())) {
                    continue;
                }
                JSONObject item = new JSONObject();
                List<Function> newList = functionService.getRoleFunction(function.getNumber());
                item.put("id", function.getId());
                item.put("text", function.getName());
                item.put("icon", function.getIcon());
                item.put("url", function.getUrl());
                item.put("component", function.getComponent());
                if (newList.size()>0) {
                    JSONArray childrenArr = getMenuByFunction(newList, fc, approvalFlag, funIdMap, userInfo);
                    if(childrenArr.size()>0) {
                        item.put("children", childrenArr);
                        dataArray.add(item);
                    }
                } else {
                    if (fc.indexOf("[" + function.getId().toString() + "]") != -1) {
                        dataArray.add(item);
                    }
                }
            }
        }
        return dataArray;
    }

    /**
     * 瑙掕壊瀵瑰簲鍔熻兘鏄剧ず
     * @param request
     * @return
     */
    @GetMapping(value = "/findRoleFunction")
    @ApiOperation(value = "瑙掕壊瀵瑰簲鍔熻兘鏄剧ず")
    public JSONArray findRoleFunction(@RequestParam("UBType") String type, @RequestParam("UBKeyId") String keyId,
                                 HttpServletRequest request)throws Exception {
        JSONArray arr = new JSONArray();
        try {
            User userInfo = userService.getCurrentUser();
            //鑾峰彇褰撳墠鐢ㄦ埛鎵€鎷ユ湁鐨勫姛鑳絠d鍒楄〃
            List<Long> funIdList = functionService.getCurrentUserFunIdList();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                funIdList = null;
            }
            List<Function> dataListFun = functionService.findRoleFunction("0", funIdList);
            //寮€濮嬫嫾鎺son鏁版嵁
            JSONObject outer = new JSONObject();
            outer.put("id", 0);
            outer.put("key", 0);
            outer.put("value", 0);
            outer.put("title", "鍔熻兘鍒楄〃");
            outer.put("attributes", "鍔熻兘鍒楄〃");
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataListFun) {
                //鏍规嵁鏉′欢浠庡垪琛ㄩ噷闈㈢Щ闄?绯荤粺绠＄悊"
                List<Function> dataList = new ArrayList<>();
                for (Function fun : dataListFun) {
                    String token = request.getHeader("X-Access-Token");
                    Long tenantId = Tools.getTenantIdByToken(token);
                    if (tenantId!=0L) {
                        if(!("绯荤粺绠＄悊").equals(fun.getName())) {
                            dataList.add(fun);
                        }
                    } else {
                        //瓒呯
                        dataList.add(fun);
                    }
                }
                dataArray = getFunctionList(dataList, type, keyId, funIdList);
                outer.put("children", dataArray);
            }
            arr.add(outer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    public JSONArray getFunctionList(List<Function> dataList, String type, String keyId, List<Long> funIdList) throws Exception {
        JSONArray dataArray = new JSONArray();
        //鑾峰彇鏉冮檺淇℃伅
        String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, keyId);
        if (null != dataList) {
            for (Function function : dataList) {
                JSONObject item = new JSONObject();
                item.put("id", function.getId());
                item.put("key", function.getId());
                item.put("value", function.getId());
                item.put("title", function.getName());
                item.put("attributes", function.getName());
                List<Function> funList = functionService.findRoleFunction(function.getNumber(), funIdList);
                if(funList.size()>0) {
                    JSONArray funArr = getFunctionList(funList, type, keyId, funIdList);
                    item.put("children", funArr);
                    dataArray.add(item);
                } else {
                    Boolean flag = ubValue.contains("[" + function.getId().toString() + "]");
                    item.put("checked", flag);
                    dataArray.add(item);
                }
            }
        }
        return dataArray;
    }

    /**
     * 鏍规嵁id鍒楄〃鏌ユ壘鍔熻兘淇℃伅
     * @param roleId
     * @param request
     * @return
     */
    @GetMapping(value = "/findRoleFunctionsById")
    @ApiOperation(value = "鏍规嵁id鍒楄〃鏌ユ壘鍔熻兘淇℃伅")
    public BaseResponseInfo findByIds(@RequestParam("roleId") Long roleId,
                                      HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<UserBusiness> list = userBusinessService.getBasicData(roleId.toString(), "RoleFunctions");
            if(null!=list && list.size()>0) {
                //鎸夐挳
                Map<Long,String> btnMap = new HashMap<>();
                String btnStr = list.get(0).getBtnStr();
                if(StringUtil.isNotEmpty(btnStr)) {
                    JSONArray btnArr = JSONArray.parseArray(btnStr);
                    for(Object obj: btnArr) {
                        JSONObject btnObj = JSONObject.parseObject(obj.toString());
                        if(btnObj.get("funId")!=null && btnObj.get("btnStr")!=null) {
                            btnMap.put(btnObj.getLong("funId"), btnObj.getString("btnStr"));
                        }
                    }
                }
                //鑿滃崟
                String funIds = list.get(0).getValue();
                funIds = funIds.substring(1, funIds.length() - 1);
                funIds = funIds.replace("][",",");
                List<Function> dataList = functionService.findByIds(funIds);
                JSONObject outer = new JSONObject();
                User userInfo = userService.getCurrentUser();
                Map<Long, Long> funIdMap = functionService.getCurrentUserFunIdMap();
                //瀛樻斁鏁版嵁json鏁扮粍
                JSONArray dataArray = new JSONArray();
                if (null != dataList) {
                    for (Function function : dataList) {
                        //濡傛灉涓嶆槸瓒呯闇€瑕佹牎楠岋紝闃叉鍒嗛厤涓嬬骇鐢ㄦ埛鐨勬寜閽潈闄愶紝澶т簬鑷韩鐨勬潈闄?                        if (PermissionUtil.isDefaultManager(userInfo) || funIdMap.get(function.getId())!=null) {
                            JSONObject item = new JSONObject();
                            item.put("id", function.getId());
                            item.put("name", function.getName());
                            item.put("pushBtn", function.getPushBtn());
                            item.put("btnStr", btnMap.get(function.getId()));
                            dataArray.add(item);
                        }
                    }
                }
                outer.put("rows", dataArray);
                outer.put("total", dataArray.size());
                res.code = 200;
                res.data = outer;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }
}
