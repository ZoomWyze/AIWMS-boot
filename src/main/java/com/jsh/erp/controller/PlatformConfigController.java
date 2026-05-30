package com.jsh.erp.controller;


/**
 * 平台参数配置 Controller
 * 提供平台级别配置参数的查询接口
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.PlatformConfig;
import com.jsh.erp.service.PlatformConfigService;
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
 * @author ji|sheng|hua 绠′紛浣砮rp QQ7827-18920
 */
@RestController
@RequestMapping(value = "/platformConfig")
@Api(tags = {"骞冲彴鍙傛暟"})
public class PlatformConfigController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(PlatformConfigController.class);

    @Resource
    private PlatformConfigService platformConfigService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        PlatformConfig platformConfig = platformConfigService.getPlatformConfig(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(platformConfig != null) {
            objectMap.put("info", platformConfig);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String platformKey = StringUtil.getInfo(search, "platformKey");
        List<PlatformConfig> list = platformConfigService.select(platformKey);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = platformConfigService.insertPlatformConfig(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = platformConfigService.updatePlatformConfig(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = platformConfigService.deletePlatformConfig(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = platformConfigService.batchDeletePlatformConfig(ids, request);
        return returnStr(objectMap, delete);
    }

    /**
     * 鑾峰彇骞冲彴鍚嶇О
     * @param request
     * @return
     */
    @GetMapping(value = "/getPlatform/name")
    @ApiOperation(value = "鑾峰彇骞冲彴鍚嶇О")
    public String getPlatformName(HttpServletRequest request)throws Exception {
        String res;
        try {
            String platformKey = "platform_name";
            PlatformConfig platformConfig = platformConfigService.getInfoByKey(platformKey);
            res = platformConfig.getPlatformValue();
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res = "ERP绯荤粺";
        }
        return res;
    }

    /**
     * 鑾峰彇瀹樻柟缃戠珯鍦板潃
     * @param request
     * @return
     */
    @GetMapping(value = "/getPlatform/url")
    @ApiOperation(value = "鑾峰彇瀹樻柟缃戠珯鍦板潃")
    public String getPlatformUrl(HttpServletRequest request)throws Exception {
        String res;
        try {
            String platformKey = "platform_url";
            PlatformConfig platformConfig = platformConfigService.getInfoByKey(platformKey);
            res = platformConfig.getPlatformValue();
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res = "#";
        }
        return res;
    }

    /**
     * 鑾峰彇鏄惁寮€鍚敞鍐?     * @param request
     * @return
     */
    @GetMapping(value = "/getPlatform/registerFlag")
    @ApiOperation(value = "鑾峰彇鏄惁寮€鍚敞鍐?)
    public String getPlatformRegisterFlag(HttpServletRequest request)throws Exception {
        String res;
        try {
            String platformKey = "register_flag";
            PlatformConfig platformConfig = platformConfigService.getInfoByKey(platformKey);
            res = platformConfig.getPlatformValue();
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res = "#";
        }
        return res;
    }

    /**
     * 鑾峰彇鏄惁寮€鍚獙璇佺爜
     * @param request
     * @return
     */
    @GetMapping(value = "/getPlatform/checkcodeFlag")
    @ApiOperation(value = "鑾峰彇鏄惁寮€鍚獙璇佺爜")
    public String getPlatformCheckcodeFlag(HttpServletRequest request)throws Exception {
        String res;
        try {
            String platformKey = "checkcode_flag";
            PlatformConfig platformConfig = platformConfigService.getInfoByKey(platformKey);
            res = platformConfig.getPlatformValue();
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res = "#";
        }
        return res;
    }

    /**
     * 鏍规嵁platformKey鏇存柊platformValue
     * @param object
     * @param request
     * @return
     */
    @PostMapping(value = "/updatePlatformConfigByKey")
    @ApiOperation(value = "鏍规嵁platformKey鏇存柊platformValue")
    public String updatePlatformConfigByKey(@RequestBody JSONObject object,
                                            HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String platformKey = object.getString("platformKey");
        String platformValue = object.getString("platformValue");
        int res = platformConfigService.updatePlatformConfigByKey(platformKey, platformValue);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鏍规嵁platformKey鏌ヨ淇℃伅
     * @param platformKey
     * @param request
     * @return
     */
    @GetMapping(value = "/getInfoByKey")
    @ApiOperation(value = "鏍规嵁platformKey鏌ヨ淇℃伅")
    public BaseResponseInfo getInfoByKey(@RequestParam("platformKey") String platformKey,
                                            HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            PlatformConfig platformConfig = platformConfigService.getInfoByKey(platformKey);
            res.code = 200;
            res.data = platformConfig;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }
}
