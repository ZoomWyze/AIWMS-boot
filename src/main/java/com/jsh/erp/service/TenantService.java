package com.jsh.erp.service;


/**
 * 租户 Service
 * 提供多租户的业务逻辑：新增/编辑/删除/查询/唯一性校验
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.Tenant;
import com.jsh.erp.datasource.entities.TenantEx;
import com.jsh.erp.datasource.entities.TenantExample;
import com.jsh.erp.datasource.entities.UserEx;
import com.jsh.erp.datasource.mappers.TenantMapper;
import com.jsh.erp.datasource.mappers.TenantMapperEx;
import com.jsh.erp.datasource.mappers.UserBusinessMapperEx;
import com.jsh.erp.datasource.mappers.UserMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.PermissionUtil;
import com.jsh.erp.utils.StringUtil;
import com.jsh.erp.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class TenantService {
    private Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantMapperEx tenantMapperEx;

    @Resource
    private UserMapperEx userMapperEx;

    @Resource
    private UserBusinessMapperEx userBusinessMapperEx;

    @Resource
    private UserService userService;

    @Resource
    private LogService logService;

    @Value("${manage.roleId}")
    private Integer manageRoleId;

    public Tenant getTenant(long id)throws Exception {
        Tenant result=null;
        try{
            result=tenantMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Tenant> getTenant()throws Exception {
        TenantExample example = new TenantExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Tenant> list=null;
        try{
            list=tenantMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<TenantEx> select(String loginName, String type, String enabled, String remark)throws Exception {
        List<TenantEx> list = null;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                PageUtils.startPage();
                list = tenantMapperEx.selectByConditionTenant(loginName, type, enabled, remark);
                if (null != list) {
                    for (TenantEx tenantEx : list) {
                        tenantEx.setCreateTimeStr(Tools.getCenternTime(tenantEx.getCreateTime()));
                        tenantEx.setExpireTimeStr(Tools.getCenternTime(tenantEx.getExpireTime()));
                    }
                }
            }
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertTenant(JSONObject obj, HttpServletRequest request)throws Exception {
        UserEx ue = JSONObject.parseObject(obj.toJSONString(), UserEx.class);
        int result = 0;
        try{
            ue.setUsername(ue.getLoginName());
            userService.checkLoginName(ue); //妫€鏌ョ櫥褰曞悕
            userService.registerUser(ue,manageRoleId,request);
            result = 1;
        } catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateTenant(JSONObject obj, HttpServletRequest request)throws Exception {
        Tenant tenant = JSONObject.parseObject(obj.toJSONString(), Tenant.class);
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                //濡傛灉绉熸埛涓嬬殑鐢ㄦ埛闄愬埗鏁伴噺涓?锛屽垯灏嗚绉熸埛涔嬪鐨勭敤鎴峰叏閮ㄧ鐢?                if (1 == tenant.getUserNumLimit()) {
                    userMapperEx.disableUserByLimit(tenant.getTenantId());
                }
                result = tenantMapper.updateByPrimaryKeySelective(tenant);
                //鏇存柊绉熸埛瀵瑰簲鐨勮鑹?                if(obj.get("roleId")!=null) {
                    String ubValue = "[" + obj.getString("roleId") + "]";
                    userBusinessMapperEx.updateValueByTypeAndKeyId("UserRole", tenant.getTenantId().toString(), ubValue);
                }
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteTenant(Long id, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = tenantMapper.deleteByPrimaryKey(id);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteTenant(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        TenantExample example = new TenantExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                result = tenantMapper.deleteByExample(example);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        TenantExample example = new TenantExample();
        example.createCriteria().andIdNotEqualTo(id).andLoginNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Tenant> list=null;
        try{
            list= tenantMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public Tenant getTenantByTenantId(long tenantId) {
        Tenant tenant = new Tenant();
        TenantExample example = new TenantExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Tenant> list = tenantMapper.selectByExample(example);
        if(list.size()>0) {
            tenant = list.get(0);
        }
        return tenant;
    }

    public int batchSetStatus(Boolean status, String ids)throws Exception {
        int result=0;
        try{
            if (PermissionUtil.isDefaultManager(userService.getCurrentUser())) {
                String statusStr = "";
                if (status) {
                    statusStr = "鎵归噺鍚敤";
                } else {
                    statusStr = "鎵归噺绂佺敤";
                }
                logService.insertLog("鐢ㄦ埛",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(ids).append("-").append(statusStr).toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
                List<Long> idList = StringUtil.strToLongList(ids);
                Tenant tenant = new Tenant();
                tenant.setEnabled(status);
                TenantExample example = new TenantExample();
                example.createCriteria().andIdIn(idList);
                result = tenantMapper.updateByExampleSelective(tenant, example);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
}
