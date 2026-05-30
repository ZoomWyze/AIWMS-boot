package com.jsh.erp.controller;


/**
 * 插件管理 Controller
 * 提供系统插件的管理接口
 *
 * @author jishenghua
 */
import com.gitee.starblues.integration.application.PluginApplication;
import com.gitee.starblues.integration.operator.PluginOperator;
import com.gitee.starblues.integration.operator.module.PluginInfo;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.service.FeatureSwitchService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.ComputerInfo;
import com.jsh.erp.utils.PermissionUtil;
import com.jsh.erp.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Paths;
import java.util.*;

/**
 * 鎻掍欢jar 鍖呮祴璇曞姛鑳? * @author jishenghua
 * @version 1.0
 */
@RestController
@RequestMapping("/plugin")
@Api(tags = {"鎻掍欢绠＄悊"})
public class PluginController {
    private Logger logger = LoggerFactory.getLogger(PluginController.class);

    @Resource
    private UserService userService;

    @Resource
    private FeatureSwitchService featureSwitchService;

    private final PluginOperator pluginOperator;

    @Autowired
    public PluginController(PluginApplication pluginApplication) {
        this.pluginOperator = pluginApplication.getPluginOperator();
    }
    /**
     * 鑾峰彇鎻掍欢淇℃伅
     * @return 杩斿洖鎻掍欢淇℃伅
     */
    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇鎻掍欢淇℃伅")
    public BaseResponseInfo getPluginInfo(@RequestParam(value = "name",required = false) String name,
                                          @RequestParam("currentPage") Integer currentPage,
                                          @RequestParam("pageSize") Integer pageSize,
                                          HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            List<PluginInfo> resList = new ArrayList<>();
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                List<PluginInfo> list = pluginOperator.getPluginInfo();
                if (StringUtil.isEmpty(name)) {
                    resList = list;
                } else {
                    for (PluginInfo pi : list) {
                        String desc = pi.getPluginDescriptor().getPluginDescription();
                        if (desc.contains(name)) {
                            resList.add(pi);
                        }
                    }
                }
            }
            map.put("rows", resList);
            map.put("total", resList.size());
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
     * 鑾峰彇鎻掍欢jar鏂囦欢鍚?     * @return 鑾峰彇鎻掍欢鏂囦欢鍚嶃€傚彧鍦ㄧ敓浜х幆澧冩樉绀?     */
    @GetMapping("/files")
    @ApiOperation(value = "鑾峰彇鎻掍欢jar鏂囦欢鍚?)
    public Set<String> getPluginFilePaths(){
        try {
            if (isCommercialEntryDisabled()) {
                return Collections.emptySet();
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                return pluginOperator.getPluginFilePaths();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 鏍规嵁鎻掍欢id鍋滄鎻掍欢
     * @param id 鎻掍欢id
     * @return 杩斿洖鎿嶄綔缁撴灉
     */
    @PostMapping("/stop/{id}")
    @ApiOperation(value = "鏍规嵁鎻掍欢id鍋滄鎻掍欢")
    public BaseResponseInfo stop(@PathVariable("id") String id){
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        String message = "";
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.stop(id)) {
                    message = "plugin '" + id + "' stop success";
                } else {
                    message = "plugin '" + id + "' stop failure";
                }
            } else {
                message = "power is limit";
            }
            map.put("message", message);
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            map.put("message", "plugin '" + id +"' stop failure. " + e.getMessage());
            res.code = 500;
            res.data = map;
        }
        return res;
    }

    /**
     * 鏍规嵁鎻掍欢id鍚姩鎻掍欢
     * @param id 鎻掍欢id
     * @return 杩斿洖鎿嶄綔缁撴灉
     */
    @PostMapping("/start/{id}")
    @ApiOperation(value = "鏍规嵁鎻掍欢id鍚姩鎻掍欢")
    public BaseResponseInfo start(@PathVariable("id") String id){
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        String message = "";
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.start(id)) {
                    message = "plugin '" + id + "' start success";
                } else {
                    message = "plugin '" + id + "' start failure";
                }
            } else {
                message = "power is limit";
            }
            map.put("message", message);
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            map.put("message", "plugin '" + id +"' start failure. " + e.getMessage());
            res.code = 500;
            res.data = map;
        }
        return res;
    }


    /**
     * 鏍规嵁鎻掍欢id鍗歌浇鎻掍欢
     * @param id 鎻掍欢id
     * @return 杩斿洖鎿嶄綔缁撴灉
     */
    @PostMapping("/uninstall/{id}")
    @ApiOperation(value = "鏍规嵁鎻掍欢id鍗歌浇鎻掍欢")
    public BaseResponseInfo uninstall(@PathVariable("id") String id){
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        String message = "";
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.uninstall(id, true)) {
                    message = "plugin '" + id + "' uninstall success";
                } else {
                    message = "plugin '" + id + "' uninstall failure";
                }
            } else {
                message = "power is limit";
            }
            map.put("message", message);
            res.code = 200;
            res.data = map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            map.put("message", "plugin '" + id +"' uninstall failure. " + e.getMessage());
            res.code = 500;
            res.data = map;
        }
        return res;
    }


    /**
     * 鏍规嵁鎻掍欢璺緞瀹夎鎻掍欢銆傝鎻掍欢jar蹇呴』鍦ㄦ湇鍔″櫒涓婂瓨鍦ㄣ€傛敞鎰? 璇ユ搷浣滃彧閫傜敤浜庣敓浜х幆澧?     * @param path 鎻掍欢璺緞鍚嶇О
     * @return 鎿嶄綔缁撴灉
     */
    @PostMapping("/installByPath")
    @ApiOperation(value = "鏍规嵁鎻掍欢璺緞瀹夎鎻掍欢")
    public String install(@RequestParam("path") String path){
        try {
            if (isCommercialEntryDisabled()) {
                return "姣曡妯″紡涓嬪凡鍏抽棴鎻掍欢鍟嗕笟鍖栧叆鍙?;
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.install(Paths.get(path))) {
                    return "installByPath success";
                } else {
                    return "installByPath failure";
                }
            } else {
                return "installByPath failure";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "installByPath failure : " + e.getMessage();
        }
    }


    /**
     * 涓婁紶骞跺畨瑁呮彃浠躲€傛敞鎰? 璇ユ搷浣滃彧閫傜敤浜庣敓浜х幆澧?     * @param file 涓婁紶鏂囦欢 multipartFile
     * @return 鎿嶄綔缁撴灉
     */
    @PostMapping("/uploadInstallPluginJar")
    @ApiOperation(value = "涓婁紶骞跺畨瑁呮彃浠?)
    public BaseResponseInfo install(MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                pluginOperator.uploadPluginAndStart(file);
                res.code = 200;
                res.data = "瀵煎叆鎴愬姛";
            } else {
                res.code = 500;
                res.data = "鎶辨瓑锛屾棤鎿嶄綔鏉冮檺锛?;
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "瀵煎叆澶辫触";
        }
        return res;
    }

    /**
     * 涓婁紶鎻掍欢鐨勯厤缃枃浠躲€傛敞鎰? 璇ユ搷浣滃彧閫傜敤浜庣敓浜х幆澧?     * @param multipartFile 涓婁紶鏂囦欢 multipartFile
     * @return 鎿嶄綔缁撴灉
     */
    @PostMapping("/uploadPluginConfigFile")
    @ApiOperation(value = "涓婁紶鎻掍欢鐨勯厤缃枃浠?)
    public String uploadConfig(@RequestParam("configFile") MultipartFile multipartFile){
        try {
            if (isCommercialEntryDisabled()) {
                return "姣曡妯″紡涓嬪凡鍏抽棴鎻掍欢鍟嗕笟鍖栧叆鍙?;
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.uploadConfigFile(multipartFile)) {
                    return "uploadConfig success";
                } else {
                    return "uploadConfig failure";
                }
            } else {
                return "installByPath failure";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "uploadConfig failure : " + e.getMessage();
        }
    }


    /**
     * 澶囦唤鎻掍欢銆傛敞鎰? 璇ユ搷浣滃彧閫傜敤浜庣敓浜х幆澧?     * @param pluginId 鎻掍欢id
     * @return 鎿嶄綔缁撴灉
     */
    @PostMapping("/back/{pluginId}")
    @ApiOperation(value = "澶囦唤鎻掍欢")
    public String backupPlugin(@PathVariable("pluginId") String pluginId){
        try {
            if (isCommercialEntryDisabled()) {
                return "姣曡妯″紡涓嬪凡鍏抽棴鎻掍欢鍟嗕笟鍖栧叆鍙?;
            }
            User userInfo = userService.getCurrentUser();
            if (PermissionUtil.isDefaultManager(userInfo)) {
                if (pluginOperator.backupPlugin(pluginId, "testBack")) {
                    return "backupPlugin success";
                } else {
                    return "backupPlugin failure";
                }
            } else {
                return "backupPlugin failure";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "backupPlugin failure : " + e.getMessage();
        }
    }

    /**
     * 鑾峰彇鍔犲瘑鍚庣殑mac
     * @return
     */
    @GetMapping("/getMacWithSecret")
    @ApiOperation(value = "鑾峰彇鍔犲瘑鍚庣殑mac")
    public BaseResponseInfo getMacWithSecret(){
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            String mac = ComputerInfo.getMacAddress();
            res.code = 200;
            res.data = DigestUtils.md5DigestAsHex(mac.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁鎻掍欢鏍囪瘑鍒ゆ柇鏄惁瀛樺湪
     * @param pluginIds 澶氫釜鐢ㄩ€楀彿闅斿紑
     * @return
     */
    @GetMapping("/checkByPluginId")
    @ApiOperation(value = "鏍规嵁鎻掍欢鏍囪瘑鍒ゆ柇鏄惁瀛樺湪")
    public BaseResponseInfo checkByTag(@RequestParam("pluginIds") String pluginIds){
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            if (isCommercialEntryDisabled()) {
                return buildCommercialDisabledResponse();
            }
            boolean data = false;
            if(StringUtil.isNotEmpty(pluginIds)) {
                String[] pluginIdList = pluginIds.split(",");
                List<PluginInfo> list = pluginOperator.getPluginInfo();
                for (PluginInfo pi : list) {
                    String info = pi.getPluginDescriptor().getPluginId();
                    for (int i = 0; i < pluginIdList.length; i++) {
                        if (pluginIdList[i].equals(info)) {
                            data = true;
                        }
                    }
                }
            }
            res.code = 200;
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    private boolean isCommercialEntryDisabled() {
        return !featureSwitchService.isCommercialEntryEnabled();
    }

    private BaseResponseInfo buildCommercialDisabledResponse() {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 403;
        res.data = "姣曡妯″紡涓嬪凡鍏抽棴鎻掍欢鍟嗕笟鍖栧叆鍙?;
        return res;
    }
}
