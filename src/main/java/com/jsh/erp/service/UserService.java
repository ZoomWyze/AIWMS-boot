package com.jsh.erp.service;


/**
 * 用户 Service
 * 提供用户的业务逻辑：登录/登出/新增/编辑/删除/查询/密码加密/用户权限
 *
 * @author jishenghua
 */
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.TenantMapper;
import com.jsh.erp.exception.BusinessParamCheckingException;
import com.jsh.erp.utils.*;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.mappers.UserMapper;
import com.jsh.erp.datasource.mappers.UserMapperEx;
import com.jsh.erp.datasource.vo.TreeNodeEx;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private UserMapperEx userMapperEx;
    @Resource
    private OrgaUserRelService orgaUserRelService;
    @Resource
    private LogService logService;
    @Resource
    private UserService userService;
    @Resource
    private TenantService tenantService;
    @Resource
    private UserBusinessService userBusinessService;
    @Resource
    private RoleService roleService;
    @Resource
    private FunctionService functionService;
    @Resource
    private PlatformConfigService platformConfigService;
    @Resource
    private RedisService redisService;

    @Value("${tenant.userNumLimit}")
    private Integer userNumLimit;

    @Value("${tenant.tryDayLimit}")
    private Integer tryDayLimit;

    public User getUser(long id)throws Exception {
        User result=null;
        try{
            //鍏堟牎楠屾槸鍚︾櫥褰曪紝鐒跺悗鎵嶈兘鏌ヨ鐢ㄦ埛鏁版嵁
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            Long userId = this.getUserId(request);
            if(userId!=null) {
                result = userMapper.selectByPrimaryKey(id);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<User> getUserListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<User> list = new ArrayList<>();
        try{
            UserExample example = new UserExample();
            example.createCriteria().andIdIn(idList);
            list = userMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<User> getUser(HttpServletRequest request) throws Exception {
        List<User> list=null;
        try{
            //鍏堟牎楠屾槸鍚︾櫥褰曪紝鐒跺悗鎵嶈兘鏌ヨ鐢ㄦ埛鏁版嵁
            Long userId = this.getUserId(request);
            if(userId!=null) {
                UserExample example = new UserExample();
                example.createCriteria().andStatusEqualTo(BusinessConstants.USER_STATUS_NORMAL).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                list = userMapper.selectByExample(example);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<UserEx> select(String userName, String loginName)throws Exception {
        List<UserEx> list=null;
        try {
            //鍏堟牎楠屾槸鍚︾櫥褰曪紝鐒跺悗鎵嶈兘鏌ヨ鐢ㄦ埛鏁版嵁
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            Long userId = this.getUserId(request);
            if(userId!=null) {
                PageUtils.startPage();
                list = userMapperEx.selectByConditionUser(userName, loginName);
                for (UserEx ue : list) {
                    String userType = "";
                    if (ue.getId().equals(ue.getTenantId())) {
                        userType = "绉熸埛";
                    } else if (ue.getTenantId() == null) {
                        userType = "瓒呯";
                    } else {
                        userType = "鏅€?;
                    }
                    ue.setUserType(userType);
                    //鏄惁缁忕悊
                    String leaderFlagStr = "";
                    if ("1".equals(ue.getLeaderFlag())) {
                        leaderFlagStr = "鏄?;
                    } else {
                        leaderFlagStr = "鍚?;
                    }
                    ue.setLeaderFlagStr(leaderFlagStr);
                }
            }
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long countUser(String userName, String loginName)throws Exception {
        Long result=null;
        try{
            result=userMapperEx.countsByUser(userName, loginName);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertUser(JSONObject obj, HttpServletRequest request)throws Exception {
        User user = JSONObject.parseObject(obj.toJSONString(), User.class);
        String password = "123456";
        //鍥犲瘑鐮佺敤MD5鍔犲瘑锛岄渶瑕佸瀵嗙爜杩涜杞寲
        try {
            password = Tools.md5Encryp(password);
            user.setPassword(password);
        } catch (NoSuchAlgorithmException e) {
            logger.error(">>>>>>>>>>>>>>杞寲MD5瀛楃涓查敊璇?锛? + e.getMessage());
        }
        int result=0;
        try{
            result=userMapper.insertSelective(user);
            logService.insertLog("鐢ㄦ埛",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(user.getLoginName()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateUser(JSONObject obj, HttpServletRequest request) throws Exception{
        User user = JSONObject.parseObject(obj.toJSONString(), User.class);
        int result=0;
        try{
            //鍒ゆ柇鏄惁鐧诲綍杩?            Object userId = redisService.getObjectFromSessionByKey(request,"userId");
            if (userId != null) {
                result = userMapper.updateByPrimaryKeySelective(user);
                logService.insertLog("鐢ㄦ埛",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(user.getLoginName()).toString(), request);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateUserByObj(User user, HttpServletRequest request) throws Exception{
        int result=0;
        try{
            //鍒ゆ柇鏄惁鐧诲綍杩?            Object userId = redisService.getObjectFromSessionByKey(request,"userId");
            if (userId != null) {
                result = userMapper.updateByPrimaryKeySelective(user);
                logService.insertLog("鐢ㄦ埛",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(user.getId()).toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int resetPwd(String md5Pwd, Long id, HttpServletRequest request) throws Exception{
        int result=0;
        User u = getUser(id);
        String loginName = u.getLoginName();
        if (PermissionUtil.isDefaultManager(loginName)) {
            logger.info("绂佹閲嶇疆瓒呯瀵嗙爜");
        } else {
            User user = new User();
            user.setId(id);
            user.setPassword(md5Pwd);
            try{
                //鍒ゆ柇鏄惁鐧诲綍杩?                Object userId = redisService.getObjectFromSessionByKey(request,"userId");
                if (userId != null) {
                    result = userMapper.updateByPrimaryKeySelective(user);
                    logService.insertLog("鐢ㄦ埛",
                            new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(id).toString(),
                            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
                }
            }catch(Exception e){
                JshException.writeFail(logger, e);
            }
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteUser(Long id, HttpServletRequest request)throws Exception {
        return batDeleteUser(id.toString(), request);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteUser(String ids, HttpServletRequest request)throws Exception {
        return batDeleteUser(ids, request);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batDeleteUser(String ids, HttpServletRequest request) throws Exception{
        int result=0;
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<User> list = getUserListByIds(ids);
        for(User user: list){
            if(user.getId().equals(user.getTenantId())) {
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,ids:[{}]",
                        ExceptionConstants.USER_LIMIT_TENANT_DELETE_CODE,ExceptionConstants.USER_LIMIT_TENANT_DELETE_MSG,ids);
                throw new BusinessRunTimeException(ExceptionConstants.USER_LIMIT_TENANT_DELETE_CODE,
                        ExceptionConstants.USER_LIMIT_TENANT_DELETE_MSG);
            }
            sb.append("[").append(user.getLoginName()).append("]");
        }
        String[] idsArray =ids.split(",");
        try{
            //鍒ゆ柇鏄惁鐧诲綍杩?            Object userId = redisService.getObjectFromSessionByKey(request,"userId");
            if (userId != null) {
                result = userMapperEx.batDeleteOrUpdateUser(idsArray);
                if(result>0) {
                    //浠巖edis涓Щ闄よ繖浜涚敤鎴风殑鐧诲綍鐘舵€?                    for (String idStr : idsArray) {
                        redisService.deleteObjectByUser(Long.valueOf(idStr));
                    }
                }
                logService.insertLog("鐢ㄦ埛", sb.toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        if(result<1){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,ids:[{}]",
                    ExceptionConstants.USER_DELETE_FAILED_CODE,ExceptionConstants.USER_DELETE_FAILED_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.USER_DELETE_FAILED_CODE,
                    ExceptionConstants.USER_DELETE_FAILED_MSG);
        }
        return result;
    }

    /**
     * 鏍￠獙楠岃瘉鐮?     * @param code 楠岃瘉鐮?     * @param uuid 鍞竴鏍囪瘑
     * @return 缁撴灉
     */
    public void validateCaptcha(String code, String uuid) throws Exception {
        PlatformConfig platformConfig = platformConfigService.getInfoByKey("checkcode_flag");
        if(platformConfig!=null && "1".equals(platformConfig.getPlatformValue())) {
            if(StringUtil.isNotEmpty(code) && StringUtil.isNotEmpty(uuid)) {
                code = code.trim();
                uuid = uuid.trim();
                String verifyKey = BusinessConstants.CAPTCHA_CODE_KEY + uuid;
                String captcha = redisService.getCacheObject(verifyKey);
                redisService.deleteObject(verifyKey);
                if (captcha == null) {
                    logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}]", ExceptionConstants.USER_JCAPTCHA_EXPIRE_CODE, ExceptionConstants.USER_JCAPTCHA_EXPIRE_MSG);
                    throw new BusinessRunTimeException(ExceptionConstants.USER_JCAPTCHA_EXPIRE_CODE, ExceptionConstants.USER_JCAPTCHA_EXPIRE_MSG);
                }
                if (!code.equalsIgnoreCase(captcha)) {
                    logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}]", ExceptionConstants.USER_JCAPTCHA_ERROR_CODE, ExceptionConstants.USER_JCAPTCHA_ERROR_MSG);
                    throw new BusinessRunTimeException(ExceptionConstants.USER_JCAPTCHA_ERROR_CODE, ExceptionConstants.USER_JCAPTCHA_ERROR_MSG);
                }
            } else {
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}]", ExceptionConstants.USER_JCAPTCHA_EMPTY_CODE, ExceptionConstants.USER_JCAPTCHA_EMPTY_MSG);
                throw new BusinessRunTimeException(ExceptionConstants.USER_JCAPTCHA_EMPTY_CODE, ExceptionConstants.USER_JCAPTCHA_EMPTY_MSG);
            }
        }
    }

    /**
     * 鐢ㄦ埛鐧诲綍
     * @param loginName
     * @param password
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, Object> login(String loginName, String password, HttpServletRequest request) throws Exception {
        Map<String, Object> data = new HashMap<>();
        String msgTip = "";
        User user = null;
        //鍒ゆ柇鐢ㄦ埛鏄惁宸茬粡鐧诲綍杩囷紝鐧诲綍杩囦笉鍐嶅鐞?        Object userId = redisService.getObjectFromSessionByKey(request,"userId");
        if (userId != null) {
            logger.info("====鐢ㄦ埛宸茬粡鐧诲綍杩? login 鏂规硶璋冪敤缁撴潫====");
            msgTip = "user already login";
        }
        //鑾峰彇鐢ㄦ埛鐘舵€?        int userStatus = -1;
        try {
            redisService.deleteObjectBySession(request,"userId");
            userStatus = validateUser(loginName, password);
        } catch (Exception e) {
            logger.error(">>>>>>>>>>>>>鐢ㄦ埛  " + loginName + " 鐧诲綍 login 鏂规硶 璁块棶鏈嶅姟灞傚紓甯?===", e);
            msgTip = "access service exception";
        }
        String token = UUID.randomUUID().toString().replaceAll("-", "") + "";
        switch (userStatus) {
            case ExceptionCodeConstants.UserExceptionCode.USER_NOT_EXIST:
                msgTip = "user is not exist";
                break;
            case ExceptionCodeConstants.UserExceptionCode.USER_PASSWORD_ERROR:
                msgTip = "user password error";
                break;
            case ExceptionCodeConstants.UserExceptionCode.BLACK_USER:
                msgTip = "user is black";
                break;
            case ExceptionCodeConstants.UserExceptionCode.USER_ACCESS_EXCEPTION:
                msgTip = "access service error";
                break;
            case ExceptionCodeConstants.UserExceptionCode.BLACK_TENANT:
                msgTip = "tenant is black";
                break;
            case ExceptionCodeConstants.UserExceptionCode.EXPIRE_TENANT:
                msgTip = "tenant is expire";
                break;
            case ExceptionCodeConstants.UserExceptionCode.USER_CONDITION_FIT:
                msgTip = "user can login";
                //楠岃瘉閫氳繃 锛屽彲浠ョ櫥褰曪紝鏀惧叆session锛岃褰曠櫥褰曟棩蹇?                user = getUserByLoginName(loginName);
                if(user.getTenantId()!=null) {
                    token = token + "_" + user.getTenantId();
                }
                redisService.storageObjectBySession(token,"userId",user.getId());
                break;
            default:
                break;
        }
        data.put("msgTip", msgTip);
        if(user!=null){
            //鏍￠獙涓嬪瘑鐮佹槸涓嶆槸杩囦簬绠€鍗?            boolean pwdSimple = false;
            if(user.getPassword().equals(Tools.md5Encryp(BusinessConstants.USER_DEFAULT_PASSWORD))) {
                pwdSimple = true;
            }
            user.setPassword(null);
            if (PermissionUtil.isDefaultManager(user.getLoginName())) {
                //濡傛灉鏄鐞嗗憳锛屽垯鍙戦€佺櫥褰曢偖浠?                sendEmailToCurrentUser(request, user);
            }
            redisService.storageObjectBySession(token,"clientIp", Tools.getLocalIp(request));
            logService.insertLogWithUserId(user.getId(), user.getTenantId(), "鐢ㄦ埛",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_LOGIN).append(user.getLoginName()).toString(),
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            data.put("token", token);
            data.put("user", user);
            data.put("pwdSimple", pwdSimple);
        }
        return data;
    }

    public int validateUser(String loginName, String password) throws Exception {
        /**榛樿鏄彲浠ョ櫥褰曠殑*/
        List<User> list = null;
        try {
            UserExample example = new UserExample();
            example.createCriteria().andLoginNameEqualTo(loginName).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            list = userMapper.selectByExample(example);
            if (null != list && list.size() == 0) {
                return ExceptionCodeConstants.UserExceptionCode.USER_NOT_EXIST;
            } else if(list.size() ==1) {
                if(list.get(0).getStatus()!=0) {
                    return ExceptionCodeConstants.UserExceptionCode.BLACK_USER;
                }
                Long tenantId = list.get(0).getTenantId();
                Tenant tenant = tenantService.getTenantByTenantId(tenantId);
                if(tenant!=null) {
                    if(tenant.getEnabled()!=null && !tenant.getEnabled()) {
                        return ExceptionCodeConstants.UserExceptionCode.BLACK_TENANT;
                    }
                    if(tenant.getExpireTime()!=null && tenant.getExpireTime().getTime()<System.currentTimeMillis()){
                        return ExceptionCodeConstants.UserExceptionCode.EXPIRE_TENANT;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>璁块棶楠岃瘉鐢ㄦ埛濮撳悕鏄惁瀛樺湪鍚庡彴淇℃伅寮傚父", e);
            return ExceptionCodeConstants.UserExceptionCode.USER_ACCESS_EXCEPTION;
        }
        try {
            UserExample example = new UserExample();
            example.createCriteria().andLoginNameEqualTo(loginName).andPasswordEqualTo(password)
                    .andStatusEqualTo(BusinessConstants.USER_STATUS_NORMAL).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            list = userMapper.selectByExample(example);
            if (null != list && list.size() == 0) {
                return ExceptionCodeConstants.UserExceptionCode.USER_PASSWORD_ERROR;
            }
        } catch (Exception e) {
            logger.error(">>>>>>>>>>璁块棶楠岃瘉鐢ㄦ埛瀵嗙爜鍚庡彴淇℃伅寮傚父", e);
            return ExceptionCodeConstants.UserExceptionCode.USER_ACCESS_EXCEPTION;
        }
        return ExceptionCodeConstants.UserExceptionCode.USER_CONDITION_FIT;
    }

    public User getUserByLoginName(String loginName)throws Exception {
        UserExample example = new UserExample();
        example.createCriteria().andLoginNameEqualTo(loginName).andStatusEqualTo(BusinessConstants.USER_STATUS_NORMAL)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<User> list=null;
        try{
            list= userMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        User user =null;
        if(list!=null&&list.size()>0){
            user = list.get(0);
        }
        return user;
    }

    /**
     * 鍙戦€侀偖浠剁粰褰撳墠鐢ㄦ埛
     * @param request
     * @param user
     * @throws Exception
     */
    private void sendEmailToCurrentUser(HttpServletRequest request, User user) throws Exception {
        String platformName = platformConfigService.getPlatformConfigByKey("platform_name").getPlatformValue();
        String emailFrom = platformConfigService.getPlatformConfigByKey("email_from").getPlatformValue();
        String emailAuthCode = platformConfigService.getPlatformConfigByKey("email_auth_code").getPlatformValue();
        String emailSmtpHost = platformConfigService.getPlatformConfigByKey("email_smtp_host").getPlatformValue();
        if(StringUtil.isNotEmpty(emailFrom) && StringUtil.isNotEmpty(emailAuthCode) && StringUtil.isNotEmpty(emailSmtpHost)
                && StringUtil.isNotEmpty(user.getEmail())) {
            String emailSubject = "鐢ㄦ埛" + user.getLoginName() + "鎴愬姛鐧诲綍" + platformName;
            String emailBody = "鐢ㄦ埛" + user.getLoginName() + "鎴愬姛鐧诲綍" + platformName + "锛岀櫥褰曟椂闂达細" + Tools.getCenternTime(new Date())
                    + "锛岀櫥褰旾P锛? + Tools.getLocalIp(request);
            platformConfigService.sendEmail(emailFrom, emailAuthCode, emailSmtpHost, user.getEmail(), emailSubject, emailBody);
        }
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        UserExample example = new UserExample();
        List<Byte> userStatus = new ArrayList<>();
        userStatus.add(BusinessConstants.USER_STATUS_NORMAL);
        example.createCriteria().andIdNotEqualTo(id).andLoginNameEqualTo(name).andStatusIn(userStatus)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<User> list=null;
        try{
            list= userMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }
    /**
     * create by: cjl
     * description:
     *  鑾峰彇褰撳墠鐢ㄦ埛淇℃伅
     * create time: 2019/1/24 10:01
     * @Param:
     * @return com.jsh.erp.datasource.entities.User
     */
    public User getCurrentUser()throws Exception{
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Long userId = Long.parseLong(redisService.getObjectFromSessionByKey(request,"userId").toString());
        return getUser(userId);
    }

    /**
     * 鏍规嵁鐢ㄦ埛鍚嶆煡璇d
     * @param loginName
     * @return
     */
    public Long getIdByLoginName(String loginName) {
        Long userId = 0L;
        UserExample example = new UserExample();
        example.createCriteria().andLoginNameEqualTo(loginName).andStatusEqualTo(BusinessConstants.USER_STATUS_NORMAL)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<User> list = userMapper.selectByExample(example);
        if(list!=null) {
            userId = list.get(0).getId();
        }
        return userId;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void addUserAndOrgUserRel(UserEx ue, HttpServletRequest request) throws Exception{
        if (PermissionUtil.isDefaultManager(ue.getLoginName())) {
            throw new BusinessRunTimeException(ExceptionConstants.USER_NAME_LIMIT_USE_CODE,
                    ExceptionConstants.USER_NAME_LIMIT_USE_MSG);
        } else {
            logService.insertLog("鐢ㄦ埛",
                    BusinessConstants.LOG_OPERATION_TYPE_ADD,
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            //妫€鏌ョ敤鎴峰悕鍜岀櫥褰曞悕
            checkLoginName(ue);
            //鏂板鐢ㄦ埛淇℃伅
            ue= this.addUser(ue);
            if(ue==null){
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,[{}]",
                        ExceptionConstants.USER_ADD_FAILED_CODE,ExceptionConstants.USER_ADD_FAILED_MSG);
                throw new BusinessRunTimeException(ExceptionConstants.USER_ADD_FAILED_CODE,
                        ExceptionConstants.USER_ADD_FAILED_MSG);
            }
            //鐢ㄦ埛id锛屾牴鎹敤鎴峰悕鏌ヨid
            Long userId = getIdByLoginName(ue.getLoginName());
            if(ue.getRoleId()!=null){
                JSONObject ubObj = new JSONObject();
                ubObj.put("type", "UserRole");
                ubObj.put("keyid", userId);
                ubObj.put("value", "[" + ue.getRoleId() + "]");
                userBusinessService.insertUserBusiness(ubObj, request);
            }
            if(ue.getOrgaId()==null){
                //濡傛灉娌℃湁閫夋嫨鏈烘瀯锛屽氨涓嶅缓鏈烘瀯鍜岀敤鎴风殑鍏宠仈鍏崇郴
                return;
            }
            if(ue.getOrgaId()!=null && "1".equals(ue.getLeaderFlag())){
                //妫€鏌ュ綋鍓嶆満鏋勬槸鍚﹀瓨鍦ㄧ粡鐞?                List<User> checkList = userMapperEx.getListByOrgaId(ue.getId(), ue.getOrgaId());
                if(checkList.size()>0) {
                    throw new BusinessRunTimeException(ExceptionConstants.USER_LEADER_IS_EXIST_CODE,
                            ExceptionConstants.USER_LEADER_IS_EXIST_MSG);
                }
            }
            //鏂板鐢ㄦ埛鍜屾満鏋勫叧鑱斿叧绯?            OrgaUserRel oul=new OrgaUserRel();
            //鏈烘瀯id
            oul.setOrgaId(ue.getOrgaId());
            oul.setUserId(userId);
            //鐢ㄦ埛鍦ㄦ満鏋勪腑鐨勬帓搴?            oul.setUserBlngOrgaDsplSeq(ue.getUserBlngOrgaDsplSeq());
            oul=orgaUserRelService.addOrgaUserRel(oul);
            if(oul==null){
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,[{}]",
                        ExceptionConstants.ORGA_USER_REL_ADD_FAILED_CODE,ExceptionConstants.ORGA_USER_REL_ADD_FAILED_MSG);
                throw new BusinessRunTimeException(ExceptionConstants.ORGA_USER_REL_ADD_FAILED_CODE,
                        ExceptionConstants.ORGA_USER_REL_ADD_FAILED_MSG);
            }
        }
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public UserEx addUser(UserEx ue) throws Exception{
        /**
         * 鏂板鐢ㄦ埛榛樿璁剧疆
         * 1銆佸瘑鐮侀粯璁?23456
         * 2鏄惁绯荤粺鑷甫榛樿涓洪潪绯荤粺鑷甫
         * 3鏄惁绠＄悊鑰呴粯璁や负鍛樺伐
         * 4榛樿鐢ㄦ埛鐘舵€佷负姝ｅ父
         * */
        ue.setPassword(Tools.md5Encryp(BusinessConstants.USER_DEFAULT_PASSWORD));
        ue.setIsystem(BusinessConstants.USER_NOT_SYSTEM);
        if(ue.getIsmanager()==null){
            ue.setIsmanager(BusinessConstants.USER_NOT_MANAGER);
        }
        ue.setStatus(BusinessConstants.USER_STATUS_NORMAL);
        int result=0;
        try{
            result= userMapper.insertSelective(ue);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        if(result>0){
            return ue;
        }
        return null;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void registerUser(UserEx ue, Integer manageRoleId, HttpServletRequest request) throws Exception{
        /**
         * 澶氭鍒涘缓浜嬪姟锛屼簨鐗╀箣闂存棤娉曞崗鍚岋紝搴旇鍦ㄥ叆鍙ｅ鍒涘缓涓€涓簨鍔′互鍋氬崗璋?         */
        if (PermissionUtil.isDefaultManager(ue.getLoginName())) {
            throw new BusinessRunTimeException(ExceptionConstants.USER_NAME_LIMIT_USE_CODE,
                    ExceptionConstants.USER_NAME_LIMIT_USE_MSG);
        } else {
            ue.setPassword(ue.getPassword());
            ue.setIsystem(BusinessConstants.USER_NOT_SYSTEM);
            if (ue.getIsmanager() == null) {
                ue.setIsmanager(BusinessConstants.USER_NOT_MANAGER);
            }
            ue.setStatus(BusinessConstants.USER_STATUS_NORMAL);
            try{
                userMapper.insertSelective(ue);
                Long userId = getIdByLoginName(ue.getLoginName());
                ue.setId(userId);
            }catch(Exception e){
                JshException.writeFail(logger, e);
            }
            //鏇存柊绉熸埛id
            User user = new User();
            user.setId(ue.getId());
            user.setTenantId(ue.getId());
            userService.updateUserTenant(user);
            //鏂板鐢ㄦ埛涓庤鑹茬殑鍏崇郴
            JSONObject ubObj = new JSONObject();
            ubObj.put("type", "UserRole");
            ubObj.put("keyid", ue.getId());
            JSONArray ubArr = new JSONArray();
            ubArr.add(manageRoleId);
            ubObj.put("value", ubArr.toString());
            ubObj.put("tenantId", ue.getId());
            userBusinessService.insertUserBusiness(ubObj, null);
            //鍒涘缓绉熸埛淇℃伅
            JSONObject tenantObj = new JSONObject();
            tenantObj.put("tenantId", ue.getId());
            tenantObj.put("loginName",ue.getLoginName());
            tenantObj.put("userNumLimit", ue.getUserNumLimit());
            tenantObj.put("expireTime", ue.getExpireTime());
            tenantObj.put("remark", ue.getRemark());
            Tenant tenant = JSONObject.parseObject(tenantObj.toJSONString(), Tenant.class);
            tenant.setCreateTime(new Date());
            if(tenant.getUserNumLimit()==null) {
                tenant.setUserNumLimit(userNumLimit); //榛樿鐢ㄦ埛闄愬埗鏁伴噺
            }
            if(tenant.getExpireTime()==null) {
                tenant.setExpireTime(Tools.addDays(new Date(), tryDayLimit)); //绉熸埛鍏佽璇曠敤鐨勫ぉ鏁?            }
            tenantMapper.insertSelective(tenant);
            logger.info("===============鍒涘缓绉熸埛淇℃伅瀹屾垚===============");
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateUserTenant(User user) throws Exception{
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(user.getId());
        try{
            userMapper.updateByPrimaryKeySelective(user);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateUserAndOrgUserRel(UserEx ue, HttpServletRequest request) throws Exception{
        if (PermissionUtil.isDefaultManager(ue.getLoginName())) {
            throw new BusinessRunTimeException(ExceptionConstants.USER_NAME_LIMIT_USE_CODE,
                    ExceptionConstants.USER_NAME_LIMIT_USE_MSG);
        } else {
            logService.insertLog("鐢ㄦ埛",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(ue.getId()).toString(),
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            //妫€鏌ョ敤鎴峰悕鍜岀櫥褰曞悕
            checkLoginName(ue);
            //鏇存柊鐢ㄦ埛淇℃伅
            ue = this.updateUser(ue);
            if (ue == null) {
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,[{}]",
                        ExceptionConstants.USER_EDIT_FAILED_CODE, ExceptionConstants.USER_EDIT_FAILED_MSG);
                throw new BusinessRunTimeException(ExceptionConstants.USER_EDIT_FAILED_CODE,
                        ExceptionConstants.USER_EDIT_FAILED_MSG);
            }
            if(ue.getRoleId()!=null){
                JSONObject ubObj = new JSONObject();
                ubObj.put("type", "UserRole");
                ubObj.put("keyid", ue.getId());
                ubObj.put("value", "[" + ue.getRoleId() + "]");
                Long ubId = userBusinessService.checkIsValueExist("UserRole", ue.getId().toString());
                if(ubId!=null) {
                    ubObj.put("id", ubId);
                    userBusinessService.updateUserBusiness(ubObj, request);
                } else {
                    userBusinessService.insertUserBusiness(ubObj, request);
                }
            }
            if (ue.getOrgaId() == null) {
                //濡傛灉娌℃湁閫夋嫨鏈烘瀯锛屽氨涓嶅缓鏈烘瀯鍜岀敤鎴风殑鍏宠仈鍏崇郴
                return;
            }
            if(ue.getOrgaId()!=null && "1".equals(ue.getLeaderFlag())){
                //妫€鏌ュ綋鍓嶆満鏋勬槸鍚﹀瓨鍦ㄧ粡鐞?                List<User> checkList = userMapperEx.getListByOrgaId(ue.getId(), ue.getOrgaId());
                if(checkList.size()>0) {
                    throw new BusinessRunTimeException(ExceptionConstants.USER_LEADER_IS_EXIST_CODE,
                            ExceptionConstants.USER_LEADER_IS_EXIST_MSG);
                }
            }
            //鏇存柊鐢ㄦ埛鍜屾満鏋勫叧鑱斿叧绯?            OrgaUserRel oul = new OrgaUserRel();
            //鏈烘瀯鍜岀敤鎴峰叧鑱斿叧绯籭d
            oul.setId(ue.getOrgaUserRelId());
            //鏈烘瀯id
            oul.setOrgaId(ue.getOrgaId());
            //鐢ㄦ埛id
            oul.setUserId(ue.getId());
            //鐢ㄦ埛鍦ㄦ満鏋勪腑鐨勬帓搴?            oul.setUserBlngOrgaDsplSeq(ue.getUserBlngOrgaDsplSeq());
            if (oul.getId() != null) {
                //宸插瓨鍦ㄦ満鏋勫拰鐢ㄦ埛鐨勫叧鑱斿叧绯伙紝鏇存柊
                oul = orgaUserRelService.updateOrgaUserRel(oul);
            } else {
                //涓嶅瓨鍦ㄦ満鏋勫拰鐢ㄦ埛鐨勫叧鑱斿叧绯伙紝鏂板缓
                oul = orgaUserRelService.addOrgaUserRel(oul);
            }
            if (oul == null) {
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,[{}]",
                        ExceptionConstants.ORGA_USER_REL_EDIT_FAILED_CODE, ExceptionConstants.ORGA_USER_REL_EDIT_FAILED_MSG);
                throw new BusinessRunTimeException(ExceptionConstants.ORGA_USER_REL_EDIT_FAILED_CODE,
                        ExceptionConstants.ORGA_USER_REL_EDIT_FAILED_MSG);
            }
        }
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public UserEx updateUser(UserEx ue)throws Exception{
        int result =0;
        try{
            result=userMapper.updateByPrimaryKeySelective(ue);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        if(result>0){
            return ue;
        }
        return null;
    }
    /**
     *  妫€鏌ョ櫥褰曞悕涓嶈兘閲嶅
     * create time: 2019/3/12 11:36
     * @Param: userEx
     * @return void
     */
    public void checkLoginName(UserEx userEx)throws Exception{
        List<User> list=null;
        if(userEx==null){
            return;
        }
        Long userId=userEx.getId();
        //妫€鏌ョ櫥褰曞悕
        if(!StringUtils.isEmpty(userEx.getLoginName())){
            String loginName=userEx.getLoginName();
            list=this.getUserListByloginName(loginName);
            if(list!=null&&list.size()>0){
                if(list.size()>1){
                    //瓒呰繃涓€鏉℃暟鎹瓨鍦紝璇ョ櫥褰曞悕宸插瓨鍦?                    logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,loginName:[{}]",
                            ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_CODE,ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_MSG,loginName);
                    throw new BusinessRunTimeException(ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_CODE,
                            ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_MSG);
                }
                //涓€鏉℃暟鎹紝鏂板鏃舵姏鍑哄紓甯革紝淇敼鏃跺拰褰撳墠鐨刬d涓嶅悓鏃舵姏鍑哄紓甯?                if(list.size()==1){
                    if(userId==null||(userId!=null&&!userId.equals(list.get(0).getId()))){
                        logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,loginName:[{}]",
                                ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_CODE,ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_MSG,loginName);
                        throw new BusinessRunTimeException(ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_CODE,
                                ExceptionConstants.USER_LOGIN_NAME_ALREADY_EXISTS_MSG);
                    }
                }
            }
        }
    }
    /**
     * 閫氳繃鐧诲綍鍚嶈幏鍙栫敤鎴峰垪琛?     * */
    public List<User> getUserListByloginName(String loginName){
        List<User> list =null;
        try{
            list=userMapperEx.getUserListByUserNameOrLoginName(null,loginName);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<TreeNodeEx> getOrganizationUserTree()throws Exception {
        List<TreeNodeEx> list =null;
        try{
            list=userMapperEx.getNodeTree();
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 鏍规嵁鐢ㄦ埛id鏌ヨ瑙掕壊淇℃伅
     * @param userId
     * @return
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Role getRoleTypeByUserId(long userId) throws Exception {
        Role role = new Role();
        List<UserBusiness> list = userBusinessService.getBasicData(String.valueOf(userId), "UserRole");
        UserBusiness ub = null;
        if(list.size() > 0) {
            ub = list.get(0);
            String values = ub.getValue();
            String roleId = null;
            if(values!=null) {
                values = values.replaceAll("\\[\\]",",").replace("[","").replace("]","");
            }
            String [] valueArray=values.split(",");
            if(valueArray.length>0) {
                roleId = valueArray[0];
            }
            role = roleService.getRoleWithoutTenant(Long.parseLong(roleId));
        }
        return role;
    }

    /**
     * 鑾峰彇鐢ㄦ埛id
     * @param request
     * @return
     */
    public Long getUserId(HttpServletRequest request) throws Exception{
        Object userIdObj = redisService.getObjectFromSessionByKey(request,"userId");
        Long userId = null;
        if(userIdObj != null) {
            userId = Long.parseLong(userIdObj.toString());
        }
        return userId;
    }

    /**
     * 鐢ㄦ埛鐨勬寜閽潈闄?     * @param userId
     * @return
     * @throws Exception
     */
    public JSONArray getBtnStrArrById(Long userId) throws Exception {
        JSONArray btnStrArr = new JSONArray();
        List<UserBusiness> userRoleList = userBusinessService.getBasicData(userId.toString(), "UserRole");
        if(userRoleList!=null && userRoleList.size()>0) {
            String roleValue = userRoleList.get(0).getValue();
            if(StringUtil.isNotEmpty(roleValue) && roleValue.indexOf("[")>-1 && roleValue.indexOf("]")>-1){
                roleValue = roleValue.replace("[", "").replace("]", ""); //瑙掕壊id-鍗曚釜
                List<UserBusiness> roleFunctionsList = userBusinessService.getBasicData(roleValue, "RoleFunctions");
                if(roleFunctionsList!=null && roleFunctionsList.size()>0) {
                    String btnStr = roleFunctionsList.get(0).getBtnStr();
                    if(StringUtil.isNotEmpty(btnStr)){
                        btnStrArr = JSONArray.parseArray(btnStr);
                    }
                }
            }
        }
        //灏嗘暟缁勪腑鐨刦unId杞负url
        JSONArray btnStrWithUrlArr = new JSONArray();
        if(btnStrArr.size()>0) {
            List<Function> functionList = functionService.getFunction();
            Map<Long, String> functionMap = new HashMap<>();
            for (Function function: functionList) {
                functionMap.put(function.getId(), function.getUrl());
            }
            for (Object obj : btnStrArr) {
                JSONObject btnStrObj = JSONObject.parseObject(obj.toString());
                Long funId = btnStrObj.getLong("funId");
                JSONObject btnStrWithUrlObj = new JSONObject();
                btnStrWithUrlObj.put("url", functionMap.get(funId));
                btnStrWithUrlObj.put("btnStr", btnStrObj.getString("btnStr"));
                btnStrWithUrlArr.add(btnStrWithUrlObj);
            }
        }
        return btnStrWithUrlArr;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(Byte status, String ids, HttpServletRequest request)throws Exception {
        logService.insertLog("鐢ㄦ埛",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ENABLED).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        List<Long> idList = StringUtil.strToLongList(ids);
        //杩囨护鎺夌鎴疯嚜韬紝绉熸埛涓嶈兘琚鐢?        User userInfo = userService.getCurrentUser();
        if(userInfo != null && userInfo.getTenantId() != null) {
            Long tenantId = userInfo.getTenantId();
            idList.remove(tenantId);
        }
        int result = 0;
        if(idList.size() > 0) {
            User user = new User();
            user.setStatus(status);
            UserExample example = new UserExample();
            example.createCriteria().andIdIn(idList);
            result = userMapper.updateByExampleSelective(user, example);
        }
        return result;
    }

    public User getUserByWeixinCode(String weixinCode) throws Exception {
        String weixinLogin = platformConfigService.getPlatformConfigByKey("weixinUrl").getPlatformValue() + BusinessConstants.WEIXIN_LOGIN;
        String weixinAppid = platformConfigService.getPlatformConfigByKey("weixinAppid").getPlatformValue();
        String weixinSecret = platformConfigService.getPlatformConfigByKey("weixinSecret").getPlatformValue();
        String url = weixinLogin + "?appid=" + weixinAppid + "&secret=" + weixinSecret + "&js_code=" + weixinCode
                + "&grant_type=authorization_code";
        JSONObject jsonObject = HttpClient.httpGet(url);
        if(jsonObject!=null) {
            String weixinOpenId = jsonObject.getString("openid");
            if(StringUtil.isNotEmpty(weixinOpenId)) {
                return userMapperEx.getUserByWeixinOpenId(weixinOpenId);
            }
        }
        return null;
    }

    public int weixinBind(String loginName, String password, String weixinCode) throws Exception {
        String weixinUrl = platformConfigService.getPlatformConfigByKey("weixinUrl").getPlatformValue();
        String weixinAppid = platformConfigService.getPlatformConfigByKey("weixinAppid").getPlatformValue();
        String weixinSecret = platformConfigService.getPlatformConfigByKey("weixinSecret").getPlatformValue();
        String url = weixinUrl + "?appid=" + weixinAppid + "&secret=" + weixinSecret + "&js_code=" + weixinCode
                + "&grant_type=authorization_code";
        JSONObject jsonObject = HttpClient.httpGet(url);
        if(jsonObject!=null) {
            String weixinOpenId = jsonObject.getString("openid");
            if(StringUtil.isNotEmpty(weixinOpenId)) {
                return userMapperEx.updateUserWithWeixinOpenId(loginName, password, weixinOpenId);
            }
        }
        return 0;
    }
}
