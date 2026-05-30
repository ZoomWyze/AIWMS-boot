package com.jsh.erp.controller;


/**
 * 用户管理 Controller
 * 提供用户的 CRUD 接口，包括：登录/登出/查询用户列表/获取用户按钮权限
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.Tenant;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.entities.UserEx;
import com.jsh.erp.datasource.vo.TreeNodeEx;
import com.jsh.erp.exception.BusinessParamCheckingException;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.service.*;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji_sheng_hua 绠′紛浣砮rp
 */
@RestController
@RequestMapping(value = "/user")
@Api(tags = {"鐢ㄦ埛绠＄悊"})
public class UserController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${manage.roleId}")
    private Integer manageRoleId;

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private FunctionService functionService;

    @Resource
    private UserBusinessService userBusinessService;

    @Resource
    private TenantService tenantService;

    @Resource
    private RedisService redisService;

    @Resource
    private FeatureSwitchService featureSwitchService;

    private static String SUCCESS = "鎿嶄綔鎴愬姛";
    private static String ERROR = "鎿嶄綔澶辫触";

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        User user = userService.getUser(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(user != null) {
            objectMap.put("info", user);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String userName = StringUtil.getInfo(search, "userName");
        String loginName = StringUtil.getInfo(search, "loginName");
        List<UserEx> list = userService.select(userName, loginName);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = userService.insertUser(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = userService.updateUser(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = userService.deleteUser(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = userService.batchDeleteUser(ids, request);
        return returnStr(objectMap, delete);
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
        Byte status = jsonObject.getByte("status");
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = userService.batchSetStatus(status, ids, request);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = userService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @PostMapping(value = "/login")
    @ApiOperation(value = "鐧诲綍")
    public BaseResponseInfo login(@RequestBody UserEx userParam, HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            userService.validateCaptcha(userParam.getCode(), userParam.getUuid());
            Map<String, Object> data = userService.login(userParam.getLoginName().trim(), userParam.getPassword().trim(), request);
            res.code = 200;
            res.data = data;
        } catch (BusinessRunTimeException e) {
            throw new BusinessRunTimeException(e.getCode(), e.getMessage());
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鐢ㄦ埛鐧诲綍澶辫触";
        }
        return res;
    }

    @GetMapping(value = "/getUserBtnByCurrentUser")
    @ApiOperation(value = "鑾峰彇褰撳墠鐢ㄦ埛鐨勬寜閽潈闄?)
    public BaseResponseInfo getUserBtnByCurrentUser(HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userBtn", functionService.getCurrentUserBtnList());
            res.code = 200;
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鎸夐挳鏉冮檺澶辫触";
        }
        return res;
    }

    @PostMapping(value = "/weixinLogin")
    @ApiOperation(value = "寰俊鐧诲綍")
    public BaseResponseInfo weixinLogin(@RequestBody JSONObject jsonObject,
                                  HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String weixinCode = jsonObject.getString("weixinCode");
            User user = userService.getUserByWeixinCode(weixinCode);
            if(user == null) {
                res.code = 501;
                res.data = "寰俊鏈粦瀹?;
            } else {
                logger.info("寰俊鐧诲綍:" + user.getLoginName());
                Map<String, Object> data = userService.login(user.getLoginName().trim(), user.getPassword().trim(), request);
                res.code = 200;
                res.data = data;
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鐢ㄦ埛鐧诲綍澶辫触";
        }
        return res;
    }

    @PostMapping(value = "/weixinBind")
    @ApiOperation(value = "缁戝畾寰俊")
    public String weixinBind(@RequestBody JSONObject jsonObject,
                             HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String loginName = jsonObject.getString("loginName");
        String password = jsonObject.getString("password");
        String weixinCode = jsonObject.getString("weixinCode");
        int res = userService.weixinBind(loginName, password, weixinCode);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/getUserSession")
    @ApiOperation(value = "鑾峰彇鐢ㄦ埛淇℃伅")
    public BaseResponseInfo getSessionUser(HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<>();
            Long userId = Long.parseLong(redisService.getObjectFromSessionByKey(request,"userId").toString());
            User user = userService.getUser(userId);
            user.setPassword(null);
            data.put("user", user);
            res.code = 200;
            res.data = data;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇session澶辫触";
        }
        return res;
    }

    @GetMapping(value = "/logout")
    @ApiOperation(value = "閫€鍑?)
    public BaseResponseInfo logout(HttpServletRequest request, HttpServletResponse response)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            redisService.deleteObjectBySession(request,"userId");
            redisService.deleteObjectBySession(request,"clientIp");
            res.code = 200;
            res.data = "閫€鍑烘垚鍔?;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 200;
            res.data = "閫€鍑烘垚鍔?;
        }
        return res;
    }

    @PostMapping(value = "/resetPwd")
    @ApiOperation(value = "閲嶇疆瀵嗙爜")
    public String resetPwd(@RequestBody JSONObject jsonObject,
                                     HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        Long id = jsonObject.getLong("id");
        String password = "123456";
        String md5Pwd = Tools.md5Encryp(password);
        int update = userService.resetPwd(md5Pwd, id, request);
        if(update > 0) {
            return returnJson(objectMap, SUCCESS, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ERROR, ErpInfo.ERROR.code);
        }
    }

    @PutMapping(value = "/updatePwd")
    @ApiOperation(value = "鏇存柊瀵嗙爜")
    public String updatePwd(@RequestBody JSONObject jsonObject, HttpServletRequest request)throws Exception {
        Integer flag = 0;
        Map<String, Object> objectMap = new HashMap<String, Object>();
        try {
            String info = "";
            Long userId = jsonObject.getLong("userId");
            String oldpwd = jsonObject.getString("oldpassword");
            String password = jsonObject.getString("password");
            User user = userService.getUser(userId);
            //蹇呴』鍜屽師濮嬪瘑鐮佷竴鑷存墠鍙互鏇存柊瀵嗙爜
            if (oldpwd.equalsIgnoreCase(user.getPassword())) {
                user.setPassword(password);
                flag = userService.updateUserByObj(user, request); //1-鎴愬姛
                info = "淇敼鎴愬姛";
            } else {
                flag = 2; //鍘熷瀵嗙爜杈撳叆閿欒
                info = "鍘熷瀵嗙爜杈撳叆閿欒";
            }
            objectMap.put("status", flag);
            if(flag > 0) {
                return returnJson(objectMap, info, ErpInfo.OK.code);
            } else {
                return returnJson(objectMap, ERROR, ErpInfo.ERROR.code);
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>>>>>>淇敼鐢ㄦ埛ID涓?锛?" + jsonObject.getLong("userId") + "瀵嗙爜淇℃伅澶辫触", e);
            flag = 3;
            objectMap.put("status", flag);
            return returnJson(objectMap, ERROR, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鐢ㄦ埛鍒楄〃锛岀敤浜庣敤鎴蜂笅鎷夋
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getUserList")
    @ApiOperation(value = "鐢ㄦ埛鍒楄〃")
    public JSONArray getUserList(HttpServletRequest request)throws Exception {
        JSONArray dataArray = new JSONArray();
        try {
            List<User> dataList = userService.getUser(request);
            if (null != dataList) {
                for (User user : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", user.getId());
                    item.put("userName", user.getUsername());
                    dataArray.add(item);
                }
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return dataArray;
    }

    /**
     * create by: cjl
     * description:
     *  鏂板鐢ㄦ埛鍙婃満鏋勫拰鐢ㄦ埛鍏崇郴
     * create time: 2019/3/8 16:06
     * @Param: beanJson
     * @return java.lang.Object
     */
    @PostMapping("/addUser")
    @ApiOperation(value = "鏂板鐢ㄦ埛")
    @ResponseBody
    public Object addUser(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception{
        JSONObject result = ExceptionConstants.standardSuccess();
        User userInfo = userService.getCurrentUser();
        Tenant tenant = tenantService.getTenantByTenantId(userInfo.getTenantId());
        Long count = userService.countUser(null,null);
        if(tenant!=null) {
            if(count>= tenant.getUserNumLimit()) {
                throw new BusinessParamCheckingException(ExceptionConstants.USER_OVER_LIMIT_FAILED_CODE,
                        ExceptionConstants.USER_OVER_LIMIT_FAILED_MSG);
            } else {
                UserEx ue= JSONObject.parseObject(obj.toJSONString(), UserEx.class);
                userService.addUserAndOrgUserRel(ue, request);
            }
        }
        return result;
    }

    /**
     * create by: cjl
     * description:
     *  淇敼鐢ㄦ埛鍙婃満鏋勫拰鐢ㄦ埛鍏崇郴
     * create time: 2019/3/8 16:06
     * @Param: beanJson
     * @return java.lang.Object
     */
    @PutMapping("/updateUser")
    @ApiOperation(value = "淇敼鐢ㄦ埛")
    @ResponseBody
    public Object updateUser(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception{
        JSONObject result = ExceptionConstants.standardSuccess();
        UserEx ue= JSONObject.parseObject(obj.toJSONString(), UserEx.class);
        userService.updateUserAndOrgUserRel(ue, request);
        return result;
    }

    /**
     * 娉ㄥ唽鐢ㄦ埛
     * @param ue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/registerUser")
    @ApiOperation(value = "娉ㄥ唽鐢ㄦ埛")
    public Object registerUser(@RequestBody UserEx ue,
                               HttpServletRequest request)throws Exception{
        if (featureSwitchService.isGraduationMode()) {
            JSONObject result = new JSONObject();
            result.put(ExceptionConstants.GLOBAL_RETURNS_CODE, 403);
            result.put(ExceptionConstants.GLOBAL_RETURNS_MESSAGE, "姣曡妯″紡涓嬪凡鍏抽棴绉熸埛璇曠敤鍏ュ彛");
            return result;
        }
        JSONObject result = ExceptionConstants.standardSuccess();
        ue.setUsername(ue.getLoginName());
        userService.validateCaptcha(ue.getCode(), ue.getUuid());
        userService.checkLoginName(ue); //妫€鏌ョ櫥褰曞悕
        userService.registerUser(ue,manageRoleId,request);
        return result;
    }

    /**
     * 鑾峰彇鏈烘瀯鐢ㄦ埛鏍?     * @return
     * @throws Exception
     */
    @RequestMapping("/getOrganizationUserTree")
    @ApiOperation(value = "鑾峰彇鏈烘瀯鐢ㄦ埛鏍?)
    public JSONArray getOrganizationUserTree()throws Exception{
        JSONArray arr=new JSONArray();
        List<TreeNodeEx> organizationUserTree= userService.getOrganizationUserTree();
        if(organizationUserTree!=null&&organizationUserTree.size()>0){
            for(TreeNodeEx node:organizationUserTree){
                String str=JSON.toJSONString(node);
                JSONObject obj=JSON.parseObject(str);
                arr.add(obj) ;
            }
        }
        return arr;
    }

    @GetMapping(value = "/getCurrentPriceLimit")
    @ApiOperation(value = "鏌ヨ褰撳墠鐢ㄦ埛鐨勪环鏍煎睆钄?)
    public BaseResponseInfo getCurrentPriceLimit(HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<>();
            String priceLimit = roleService.getCurrentPriceLimit(request);
            data.put("priceLimit", priceLimit);
            res.code = 200;
            res.data = data;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇session澶辫触";
        }
        return res;
    }

    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛鐨勮鑹茬被鍨?     * @param request
     * @return
     */
    @GetMapping("/getRoleTypeByCurrentUser")
    @ApiOperation(value = "鑾峰彇褰撳墠鐢ㄦ埛鐨勮鑹茬被鍨?)
    public BaseResponseInfo getRoleTypeByCurrentUser(HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            Long userId = userService.getUserId(request);
            String roleType = userService.getRoleTypeByUserId(userId).getType(); //瑙掕壊绫诲瀷
            data.put("roleType", roleType);
            res.code = 200;
            res.data = data;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇澶辫触";
        }
        return res;
    }



    /**
     * 鑾峰彇瀵瑰簲鐨勭敤鎴锋樉绀?     * @param type
     * @param oneValue
     * @param request
     * @return
     */
    @GetMapping(value = "/getUserWithChecked")
    @ApiOperation(value = "鑾峰彇瀵瑰簲鐨勭敤鎴锋樉绀?)
    public JSONArray getUserWithChecked(@RequestParam("UBType") String type, @RequestParam("UBValue") String oneValue,
                                  HttpServletRequest request) throws Exception{
        JSONArray arr = new JSONArray();
        try {
            //鑾峰彇鏉冮檺淇℃伅
            List<Long> keyIdList = userBusinessService.getUBKeyIdByTypeAndOneValue(type, oneValue);
            Map<Long, Long> keyIdMap = keyIdList.stream().collect(Collectors.toMap(Function.identity(),Function.identity()));
            List<User> dataList = userService.getUser(request);
            //寮€濮嬫嫾鎺son鏁版嵁
            JSONObject outer = new JSONObject();
            outer.put("id", 0);
            outer.put("key", 0);
            outer.put("value", 0);
            outer.put("title", "鐢ㄦ埛鍒楄〃");
            outer.put("attributes", "鐢ㄦ埛鍒楄〃");
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (User user : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", user.getId());
                    item.put("key", user.getId());
                    item.put("value", user.getId());
                    item.put("title", user.getLoginName() + "(" + user.getUsername() + ")");
                    item.put("attributes", user.getLoginName());
                    if (keyIdMap.get(user.getId())!=null) {
                        item.put("checked", true);
                    }
                    dataArray.add(item);
                }
            }
            outer.put("children", dataArray);
            arr.add(outer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return arr;
    }

    /**
     * 鑾峰彇闅忔満鏍￠獙鐮?     * @param response
     * @return
     */
    @GetMapping(value = "/randomImage")
    @ApiOperation(value = "鑾峰彇闅忔満鏍￠獙鐮?)
    public BaseResponseInfo randomImage(HttpServletResponse response){
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<>();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "") + "";
            String verifyKey = BusinessConstants.CAPTCHA_CODE_KEY + uuid;
            String codeNum = Tools.getCharAndNum(4);
            redisService.storageCaptchaObject(verifyKey, codeNum);
            String base64 = RandImageUtil.generate(codeNum);
            data.put("uuid", uuid);
            data.put("base64", base64);
            res.code = 200;
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇澶辫触";
        }
        return res;
    }


    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛鐨勭敤鎴锋暟閲忓拰绉熸埛淇℃伅
     * @param request
     * @return
     */
    @GetMapping(value = "/infoWithTenant")
    @ApiOperation(value = "鑾峰彇褰撳墠鐢ㄦ埛鐨勭敤鎴锋暟閲忓拰绉熸埛淇℃伅")
    public BaseResponseInfo infoWithTenant(HttpServletRequest request){
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> data = new HashMap<>();
            Long userId = Long.parseLong(redisService.getObjectFromSessionByKey(request,"userId").toString());
            User user = userService.getUser(userId);
            //鑾峰彇褰撳墠鐢ㄦ埛鏁?            int userCurrentNum = userService.getUser(request).size();
            Tenant tenant = tenantService.getTenantByTenantId(user.getTenantId());
            if(tenant.getExpireTime()!=null && tenant.getExpireTime().getTime()<System.currentTimeMillis()){
                //绉熸埛宸茬粡杩囨湡锛岀Щ闄oken
                redisService.deleteObjectBySession(request,"userId");
                redisService.deleteObjectBySession(request,"clientIp");
            }
            data.put("type", tenant.getType()); //绉熸埛绫诲瀷锛?鍏嶈垂绉熸埛锛?浠樿垂绉熸埛
            data.put("expireTime", Tools.parseDateToStr(tenant.getExpireTime()));
            data.put("userCurrentNum", userCurrentNum);
            data.put("userNumLimit", tenant.getUserNumLimit());
            data.put("tenantId", tenant.getTenantId());
            res.code = 200;
            res.data = data;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇澶辫触";
        }
        return res;
    }
}
