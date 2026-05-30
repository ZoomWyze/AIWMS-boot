package com.jsh.erp.controller;


/**
 * 用户业务关系 Controller
 * 提供用户与角色/模块的关联关系管理接口
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.entities.UserBusiness;
import com.jsh.erp.service.UserBusinessService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.ErpInfo;
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
 * @author ji_sheng_hua jshERP
 */
@RestController
@RequestMapping(value = "/userBusiness")
@Api(tags = {"鐢ㄦ埛瑙掕壊妯″潡鐨勫叧绯?})
public class UserBusinessController {
    private Logger logger = LoggerFactory.getLogger(UserBusinessController.class);

    @Resource
    private UserBusinessService userBusinessService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        UserBusiness userBusiness = userBusinessService.getUserBusiness(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(userBusiness != null) {
            objectMap.put("info", userBusiness);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = userBusinessService.insertUserBusiness(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = userBusinessService.updateUserBusiness(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = userBusinessService.deleteUserBusiness(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = userBusinessService.batchDeleteUserBusiness(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = userBusinessService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }
    
    /**
     * 鑾峰彇淇℃伅
     * @param keyId
     * @param type
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getBasicData")
    @ApiOperation(value = "鑾峰彇淇℃伅")
    public BaseResponseInfo getBasicData(@RequestParam(value = "KeyId") String keyId,
                                         @RequestParam(value = "Type") String type,
                                         HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            List<UserBusiness> list = userBusinessService.getBasicData(keyId, type);
            Map<String, List> mapData = new HashMap<String, List>();
            mapData.put("userBusinessList", list);
            res.code = 200;
            res.data = mapData;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鏌ヨ鏉冮檺澶辫触";
        }
        return res;
    }

    /**
     * 鏍￠獙瀛樺湪
     * @param type
     * @param keyId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/checkIsValueExist")
    @ApiOperation(value = "鏍￠獙瀛樺湪")
    public String checkIsValueExist(@RequestParam(value ="type", required = false) String type,
                                   @RequestParam(value ="keyId", required = false) String keyId,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        Long id = userBusinessService.checkIsValueExist(type, keyId);
        objectMap.put("id", id);
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏇存柊瑙掕壊鐨勬寜閽潈闄?
     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/updateBtnStr")
    @ApiOperation(value = "鏇存柊瑙掕壊鐨勬寜閽潈闄?)
    public BaseResponseInfo updateBtnStr(@RequestBody JSONObject jsonObject,
                                         HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String roleId = jsonObject.getString("roleId");
            String btnStr = jsonObject.getString("btnStr");
            String keyId = roleId;
            String type = "RoleFunctions";
            int back = userBusinessService.updateBtnStr(keyId, type, btnStr);
            if(back > 0) {
                res.code = 200;
                res.data = "鎴愬姛";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鏇存柊鏉冮檺澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁KeyId鍜岀被鍨嬫洿鏂颁竴涓€?
     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/updateOneValueByKeyIdAndType")
    @ApiOperation(value = "鏍规嵁KeyId鍜岀被鍨嬫洿鏂颁竴涓€?)
    public BaseResponseInfo updateOneValueByKeyIdAndType(@RequestBody JSONObject jsonObject,
                                                         HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String type = jsonObject.getString("type");
            JSONArray keyIdArr = jsonObject.getJSONArray("keyIds");
            String oneValue = jsonObject.getString("oneValue");
            int back = userBusinessService.updateOneValueByKeyIdAndType(type, keyIdArr, oneValue);
            if(back > 0) {
                res.code = 200;
                res.data = "鎴愬姛";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鏇存柊鏉冮檺澶辫触";
        }
        return res;
    }
}
