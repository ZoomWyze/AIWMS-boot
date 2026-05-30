package com.jsh.erp.service;


/**
 * 机构 Service
 * 提供组织机构的业务逻辑：新增/编辑/删除/查询/树形结构
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.Organization;
import com.jsh.erp.datasource.entities.OrganizationExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.OrganizationMapper;
import com.jsh.erp.datasource.mappers.OrganizationMapperEx;
import com.jsh.erp.datasource.vo.TreeNode;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: jsh
 */
@Service
public class OrganizationService {
    private Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private OrganizationMapperEx organizationMapperEx;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    public Organization getOrganization(long id) throws Exception {
        return organizationMapper.selectByPrimaryKey(id);
    }

    public List<Organization> getOrganizationListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<Organization> list = new ArrayList<>();
        try{
            OrganizationExample example = new OrganizationExample();
            example.createCriteria().andIdIn(idList);
            list = organizationMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertOrganization(JSONObject obj, HttpServletRequest request)throws Exception {
        Organization organization = JSONObject.parseObject(obj.toJSONString(), Organization.class);
        organization.setCreateTime(new Date());
        organization.setUpdateTime(new Date());
        int result=0;
        try{
            result=organizationMapper.insertSelective(organization);
            logService.insertLog("鏈烘瀯",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(organization.getOrgAbr()).toString(),request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateOrganization(JSONObject obj, HttpServletRequest request)throws Exception {
        Organization organization = JSONObject.parseObject(obj.toJSONString(), Organization.class);
        organization.setUpdateTime(new Date());
        int result=0;
        try{
            result=organizationMapperEx.editOrganization(organization);
            logService.insertLog("鏈烘瀯",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(organization.getOrgAbr()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteOrganization(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteOrganizationByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteOrganization(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteOrganizationByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteOrganizationByIds(String ids) throws Exception{
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<Organization> list = getOrganizationListByIds(ids);
        for(Organization organization: list){
            sb.append("[").append(organization.getOrgAbr()).append("]");
        }
        logService.insertLog("鏈烘瀯", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        List <Organization> organList = organizationMapperEx.getOrganizationByParentIds(idArray);
        if(organList!=null && organList.size()>0) {
            //濡傛灉瀛樺湪瀛愭満鏋勫垯涓嶈兘鍒犻櫎
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}]",
                    ExceptionConstants.ORGANIZATION_CHILD_NOT_ALLOWED_DELETE_CODE,ExceptionConstants.ORGANIZATION_CHILD_NOT_ALLOWED_DELETE_MSG);
            throw new BusinessRunTimeException(ExceptionConstants.ORGANIZATION_CHILD_NOT_ALLOWED_DELETE_CODE,
                    ExceptionConstants.ORGANIZATION_CHILD_NOT_ALLOWED_DELETE_MSG);
        } else {
            result=organizationMapperEx.batchDeleteOrganizationByIds(
                    new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdNotEqualTo(id).andOrgAbrEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Organization> list=null;
        try{
            list= organizationMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<TreeNode> getOrganizationTree(Long id)throws Exception {
        List<TreeNode> list=null;
        try{
            list=organizationMapperEx.getNodeTree(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Organization> findById(Long id) throws Exception{
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdEqualTo(id);
        List<Organization> list=null;
        try{
            list=organizationMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Organization> findByParentId(Long parentId)throws Exception {
        List<Organization> list=null;
        if(parentId!=null){
            OrganizationExample example = new OrganizationExample();
            example.createCriteria().andIdEqualTo(parentId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            try{
                list=organizationMapper.selectByExample(example);
            }catch(Exception e){
                JshException.readFail(logger, e);
            }
        }
        return list;
    }

    public List<Organization> findByOrgNo(String orgNo)throws Exception {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andOrgNoEqualTo(orgNo).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Organization> list=null;
        try{
            list=organizationMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }
    /**
     * create by: cjl
     * description:
     *  妫€鏌ユ満鏋勭紪鍙锋槸鍚﹀凡缁忓瓨鍦?     * create time: 2019/3/7 10:01
     * @Param: orgNo
     * @return void
     */
    public void checkOrgNoIsExists(String orgNo,Long id)throws Exception {
        List<Organization> orgList=findByOrgNo(orgNo);
        if(orgList!=null&&orgList.size()>0){
            if(orgList.size()>1){
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,orgNo[{}]",
                        ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG,orgNo);
                //鑾峰彇鐨勬暟鎹潯鏁板ぇ浜?锛屾満鏋勭紪鍙峰凡瀛樺湪
                throw new BusinessRunTimeException(ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,
                        ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG);
            }
            if(id!=null){
                if(!orgList.get(0).getId().equals(id)){
                    //鏁版嵁鏉℃暟绛変簬1锛屼絾鏄拰缂栬緫鐨勬暟鎹殑id涓嶇浉鍚?                    logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,orgNo[{}],id[{}]",
                            ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG,orgNo,id);
                    throw new BusinessRunTimeException(ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,
                            ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG);
                }
            }else{
                logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,orgNo[{}]",
                        ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG,orgNo);
                //鏁版嵁鏉℃暟绛変簬1锛屼絾姝ゆ椂鏄柊澧?                throw new BusinessRunTimeException(ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_CODE,
                        ExceptionConstants.ORGANIZATION_NO_ALREADY_EXISTS_MSG);
            }
        }

    }

    /**
     * 鏍规嵁鐖剁骇id閫掑綊鑾峰彇瀛愰泦缁勭粐id
     * @return
     */
    public List<Long> getOrgIdByParentId(Long orgId) {
        List<Long> idList = new ArrayList<>();
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andIdEqualTo(orgId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Organization> orgList = organizationMapper.selectByExample(example);
        if(orgList!=null && orgList.size()>0) {
            idList.add(orgId);
            getOrgIdByParentNo(idList, orgList.get(0).getId());
        }
        return idList;
    }

    /**
     * 鏍规嵁缁勭粐缂栧彿閫掑綊鑾峰彇涓嬬骇缂栧彿
     * @param id
     * @return
     */
    public void getOrgIdByParentNo(List<Long> idList,Long id) {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andParentIdEqualTo(id).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Organization> orgList = organizationMapper.selectByExample(example);
        if(orgList!=null && orgList.size()>0) {
            for(Organization o: orgList) {
                idList.add(o.getId());
                getOrgIdByParentNo(idList, o.getId());
            }
        }
    }
}
