package com.jsh.erp.service;


/**
 * 机构用户关系 Service
 * 提供机构与用户关联关系的业务逻辑
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.OrgaUserRel;
import com.jsh.erp.datasource.entities.OrgaUserRelExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.OrgaUserRelMapper;
import com.jsh.erp.datasource.mappers.OrgaUserRelMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description
 *
 * @Author: cjl
 * @Date: 2019/3/11 18:11
 */
@Service
public class OrgaUserRelService {
    private Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Resource
    private OrgaUserRelMapper orgaUserRelMapper;
    @Resource
    private OrgaUserRelMapperEx orgaUserRelMapperEx;
    @Resource
    private UserService userService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private LogService logService;

    public OrgaUserRel getOrgaUserRel(long id) throws Exception{
        return orgaUserRelMapper.selectByPrimaryKey(id);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertOrgaUserRel(JSONObject obj, HttpServletRequest request) throws Exception{
        OrgaUserRel orgaUserRel = JSONObject.parseObject(obj.toJSONString(), OrgaUserRel.class);
        int result=0;
        try{
            result=orgaUserRelMapper.insertSelective(orgaUserRel);
            logService.insertLog("鐢ㄦ埛涓庢満鏋勫叧绯?, BusinessConstants.LOG_OPERATION_TYPE_ADD, request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateOrgaUserRel(JSONObject obj, HttpServletRequest request) throws Exception{
        OrgaUserRel orgaUserRel = JSONObject.parseObject(obj.toJSONString(), OrgaUserRel.class);
        int result=0;
        try{
            result=orgaUserRelMapper.updateByPrimaryKeySelective(orgaUserRel);
            logService.insertLog("鐢ㄦ埛涓庢満鏋勫叧绯?,
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(orgaUserRel.getId()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteOrgaUserRel(Long id, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            result=orgaUserRelMapper.deleteByPrimaryKey(id);
            logService.insertLog("鐢ㄦ埛涓庢満鏋勫叧绯?,
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_DELETE).append(id).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteOrgaUserRel(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        OrgaUserRelExample example = new OrgaUserRelExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            result=orgaUserRelMapper.deleteByExample(example);
            logService.insertLog("鐢ㄦ埛涓庢満鏋勫叧绯?, "鎵归噺鍒犻櫎,id闆?" + ids, request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
    /**
     * create by: cjl
     * description:
     *  鏂板鏈烘瀯鐢ㄦ埛鍏宠仈鍏崇郴,鍙嶆樉id
     * create time: 2019/3/12 9:40
     * @Param: orgaUserRel
     * @return void
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public OrgaUserRel addOrgaUserRel(OrgaUserRel orgaUserRel) throws Exception{
        Date date = new Date();
        User userInfo=userService.getCurrentUser();
        //鍒涘缓鏃堕棿
        if(orgaUserRel.getCreateTime()==null){
            orgaUserRel.setCreateTime(date);
        }
        //鍒涘缓浜?        if(orgaUserRel.getCreator()==null){
            orgaUserRel.setCreator(userInfo==null?null:userInfo.getId());
        }
        //鏇存柊鏃堕棿
        if(orgaUserRel.getUpdateTime()==null){
            orgaUserRel.setUpdateTime(date);
        }
        //鏇存柊浜?        if(orgaUserRel.getUpdater()==null){
            orgaUserRel.setUpdater(userInfo==null?null:userInfo.getId());
        }
        orgaUserRel.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
        int result=0;
        try{
            result=orgaUserRelMapperEx.addOrgaUserRel(orgaUserRel);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        if(result>0){
            return orgaUserRel;
        }
        return null;
    }
    /**
     * create by: cjl
     * description:
     *  鏇存柊鏈烘瀯鐢ㄦ埛鍏宠仈鍏崇郴
     * create time: 2019/3/12 9:40
     * @Param: orgaUserRel
     * @return void
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public OrgaUserRel updateOrgaUserRel(OrgaUserRel orgaUserRel) throws Exception{
        User userInfo=userService.getCurrentUser();
        //鏇存柊鏃堕棿
        if(orgaUserRel.getUpdateTime()==null){
            orgaUserRel.setUpdateTime(new Date());
        }
        //鏇存柊浜?        if(orgaUserRel.getUpdater()==null){
            orgaUserRel.setUpdater(userInfo==null?null:userInfo.getId());
        }
        int result=0;
        try{
            result=orgaUserRelMapperEx.updateOrgaUserRel(orgaUserRel);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        if(result>0){
            return orgaUserRel;
        }
        return null;
    }

    /**
     * 鏍规嵁鐢ㄦ埛id鑾峰彇鐢ㄦ埛id鍒楄〃
     * @param userId
     * @return
     * @throws Exception
     */
    public String getUserIdListByUserId(Long userId) throws Exception{
        String users = "";
        OrgaUserRelExample example = new OrgaUserRelExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<OrgaUserRel> list = orgaUserRelMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            OrgaUserRel our = list.get(0);
            List<Long> userIdList = getUserIdListByOrgId(our.getOrgaId());
            for(Long u: userIdList){
                users = users + u + ",";
            }
            if(users.length()>0){
                users = users.substring(0,users.length()-1);
            }
        }
        return users;
    }

    /**
     * 鏍规嵁缁勭粐id鑾峰彇鎵€灞炵殑鐢ㄦ埛id鍒楄〃锛堝寘鍚粍缁囩殑閫掑綊锛?     * @param orgId
     * @return
     */
    public List<Long> getUserIdListByOrgId(Long orgId) {
        List<Long> orgIdList = organizationService.getOrgIdByParentId(orgId);
        List<Long> userIdList = new ArrayList<Long>();
        OrgaUserRelExample example = new OrgaUserRelExample();
        if(orgIdList!=null && orgIdList.size()>0) {
            example.createCriteria().andOrgaIdIn(orgIdList).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        } else {
            example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        }
        List<OrgaUserRel> list = orgaUserRelMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            for(OrgaUserRel our: list) {
                userIdList.add(our.getUserId());
            }
        }
        return  userIdList;
    }
}
