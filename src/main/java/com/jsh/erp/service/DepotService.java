package com.jsh.erp.service;


/**
 * 仓库 Service
 * 提供仓库信息的业务逻辑：新增/编辑/删除/查询/唯一性校验
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
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

@Service
public class DepotService {
    private Logger logger = LoggerFactory.getLogger(DepotService.class);

    @Resource
    private DepotMapper depotMapper;
    @Resource
    private DepotMapperEx depotMapperEx;
    @Resource
    private UserService userService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private UserBusinessService userBusinessService;
    @Resource
    private LogService logService;
    @Resource
    private DepotItemMapperEx depotItemMapperEx;
    @Resource
    private MaterialInitialStockMapperEx materialInitialStockMapperEx;
    @Resource
    private MaterialCurrentStockMapperEx materialCurrentStockMapperEx;

    public Depot getDepot(long id)throws Exception {
        Depot result=null;
        try{
            result=depotMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Depot> getDepotListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        DepotExample example = new DepotExample();
        example.createCriteria().andIdIn(idList);
        return depotMapper.selectByExample(example);
    }

    public List<Depot> getDepot()throws Exception {
        DepotExample example = new DepotExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Depot> list=null;
        try{
            list=depotMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Depot> getAllList()throws Exception {
        DepotExample example = new DepotExample();
        example.createCriteria().andEnabledEqualTo(true).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Depot> list=null;
        try{
            list=depotMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotEx> select(String name, Integer type, String remark)throws Exception {
        List<DepotEx> list=null;
        try{
            PageUtils.startPage();
            list=depotMapperEx.selectByConditionDepot(name, type, remark);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepot(JSONObject obj, HttpServletRequest request)throws Exception {
        Depot depot = JSONObject.parseObject(obj.toJSONString(), Depot.class);
        int result=0;
        try{
            depot.setType(0);
            List<Depot> depotList = getDepot();
            if(depotList.size() == 0) {
                depot.setIsDefault(true);
            } else {
                depot.setIsDefault(false);
            }
            depot.setEnabled(true);
            result=depotMapper.insertSelective(depot);
            //鏂板浠撳簱鏃剁粰褰撳墠鐢ㄦ埛鑷姩鎺堟潈
            Long userId = userService.getUserId(request);
            Long depotId = getIdByName(depot.getName());
            String ubKey = "[" + depotId + "]";
            List<UserBusiness> ubList = userBusinessService.getBasicData(userId.toString(), "UserDepot");
            if(ubList ==null || ubList.size() == 0) {
                JSONObject ubObj = new JSONObject();
                ubObj.put("type", "UserDepot");
                ubObj.put("keyId", userId);
                ubObj.put("value", ubKey);
                userBusinessService.insertUserBusiness(ubObj, request);
            } else {
                UserBusiness ubInfo = ubList.get(0);
                JSONObject ubObj = new JSONObject();
                ubObj.put("id", ubInfo.getId());
                ubObj.put("type", ubInfo.getType());
                ubObj.put("keyId", ubInfo.getKeyId());
                ubObj.put("value", ubInfo.getValue() + ubKey);
                userBusinessService.updateUserBusiness(ubObj, request);
            }
            logService.insertLog("浠撳簱",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(depot.getName()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepot(JSONObject obj, HttpServletRequest request) throws Exception{
        Depot depot = JSONObject.parseObject(obj.toJSONString(), Depot.class);
        int result=0;
        try{
            result= depotMapper.updateByPrimaryKeySelective(depot);
            logService.insertLog("浠撳簱",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(depot.getName()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteDepot(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteDepotByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepot(String ids, HttpServletRequest request) throws Exception{
        return batchDeleteDepotByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotByIds(String ids)throws Exception {
        int result=0;
        String [] idArray=ids.split(",");
        //鏍￠獙鍗曟嵁瀛愯〃	jsh_depot_item
        List<DepotItem> depotItemList = depotItemMapperEx.getDepotItemListListByDepotIds(idArray);
        if(depotItemList!=null&&depotItemList.size()>0){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,DepotIds[{}]",
                    ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,ExceptionConstants.DELETE_FORCE_CONFIRM_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,
                    ExceptionConstants.DELETE_FORCE_CONFIRM_MSG);
        }
        try{
            //璁板綍鏃ュ織
            StringBuffer sb = new StringBuffer();
            sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
            List<Depot> list = getDepotListByIds(ids);
            for(Depot depot: list){
                sb.append("[").append(depot.getName()).append("]");
            }
            User userInfo=userService.getCurrentUser();
            //鏍￠獙閫氳繃鎵ц鍒犻櫎鎿嶄綔
            //鍒犻櫎浠撳簱鍏宠仈鐨勫晢鍝佺殑鍒濆搴撳瓨
            materialInitialStockMapperEx.batchDeleteByDepots(idArray);
            //鍒犻櫎浠撳簱鍏宠仈鐨勫晢鍝佺殑褰撳墠搴撳瓨
            materialCurrentStockMapperEx.batchDeleteByDepots(idArray);
            //鍒犻櫎浠撳簱
            result = depotMapperEx.batchDeleteDepotByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
            //璁板綍鏃ュ織
            logService.insertLog("浠撳簱", sb.toString(),
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        DepotExample example = new DepotExample();
        example.createCriteria().andIdNotEqualTo(id).andNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Depot> list=null;
        try{
            list= depotMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<Depot> findUserDepot()throws Exception{
        DepotExample example = new DepotExample();
        example.createCriteria().andTypeEqualTo(0).andEnabledEqualTo(true)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Depot> list=null;
        try{
            list= depotMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateIsDefault(Long depotId) throws Exception{
        int result=0;
        try{
            //鍏ㄩ儴鍙栨秷榛樿
            Depot allDepot = new Depot();
            allDepot.setIsDefault(false);
            DepotExample allExample = new DepotExample();
            allExample.createCriteria();
            depotMapper.updateByExampleSelective(allDepot, allExample);
            //缁欐寚瀹氫粨搴撹涓洪粯璁?            Depot depot = new Depot();
            depot.setIsDefault(true);
            DepotExample example = new DepotExample();
            example.createCriteria().andIdEqualTo(depotId);
            depotMapper.updateByExampleSelective(depot, example);
            logService.insertLog("浠撳簱",BusinessConstants.LOG_OPERATION_TYPE_EDIT+depotId,
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            result = 1;
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    /**
     * 鏍规嵁鍚嶇О鑾峰彇id
     * @param name
     */
    public Long getIdByName(String name){
        Long id = 0L;
        DepotExample example = new DepotExample();
        example.createCriteria().andNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Depot> list = depotMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            id = list.get(0).getId();
        }
        return id;
    }

    /**
     * 鏍规嵁鍗曚釜浠撳簱鏌ヨ
     * @param depotId
     * @return
     * @throws Exception
     */
    public List<Long> parseDepotList(Long depotId) throws Exception {
        List<Long> depotList = new ArrayList<>();
        if(depotId !=null) {
            depotList.add(depotId);
        } else {
            //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
            JSONArray depotArr = findDepotByCurrentUser();
            for(Object obj: depotArr) {
                JSONObject object = JSONObject.parseObject(obj.toString());
                depotList.add(object.getLong("id"));
            }
        }
        return depotList;
    }

    /**
     * 鏍规嵁澶氫釜浠撳簱鏌ヨ
     * @param depotIdArr
     * @return
     * @throws Exception
     */
    public List<Long> parseDepotListByArr(String[] depotIdArr) throws Exception {
        List<Long> depotList = new ArrayList<>();
        if(depotIdArr !=null) {
            for (int i = 0; i < depotIdArr.length; i++) {
                depotList.add(Long.parseLong(depotIdArr[i]));
            }
        } else {
            //鏈€夋嫨浠撳簱鏃堕粯璁や负褰撳墠鐢ㄦ埛鏈夋潈闄愮殑浠撳簱
            JSONArray depotArr = findDepotByCurrentUser();
            for(Object obj: depotArr) {
                JSONObject object = JSONObject.parseObject(obj.toString());
                depotList.add(object.getLong("id"));
            }
        }
        return depotList;
    }

    public JSONArray findDepotByCurrentUser() throws Exception {
        JSONArray arr = new JSONArray();
        String type = "UserDepot";
        Long userId = userService.getCurrentUser().getId();
        List<Depot> dataList = findUserDepot();
        //寮€濮嬫嫾鎺son鏁版嵁
        if (null != dataList) {
            boolean depotFlag = systemConfigService.getDepotFlag();
            if(depotFlag) {
                List<UserBusiness> list = userBusinessService.getBasicData(userId.toString(), type);
                if(list!=null && list.size()>0) {
                    String depotStr = list.get(0).getValue();
                    if(StringUtil.isNotEmpty(depotStr)){
                        depotStr = depotStr.replaceAll("\\[", "").replaceAll("]", ",");
                        String[] depotArr = depotStr.split(",");
                        for (Depot depot : dataList) {
                            for(String depotId: depotArr) {
                                if(depot.getId() == Long.parseLong(depotId)){
                                    JSONObject item = new JSONObject();
                                    item.put("id", depot.getId());
                                    item.put("depotName", depot.getName());
                                    item.put("isDefault", depot.getIsDefault());
                                    arr.add(item);
                                }
                            }
                        }
                    }
                }
            } else {
                for (Depot depot : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("id", depot.getId());
                    item.put("depotName", depot.getName());
                    item.put("isDefault", depot.getIsDefault());
                    arr.add(item);
                }
            }
        }
        return arr;
    }

    /**
     * 褰撳墠鐢ㄦ埛鏈夋潈闄愪娇鐢ㄧ殑浠撳簱鍒楄〃鐨刬d锛岀敤閫楀彿闅斿紑
     * @return
     * @throws Exception
     */
    public String findDepotStrByCurrentUser() throws Exception {
        JSONArray arr =  findDepotByCurrentUser();
        StringBuffer sb = new StringBuffer();
        for(Object object: arr) {
            JSONObject obj = (JSONObject)object;
            sb.append(obj.getLong("id")).append(",");
        }
        String depotStr = sb.toString();
        if(StringUtil.isNotEmpty(depotStr)){
            depotStr = depotStr.substring(0, depotStr.length()-1);
        }
        return depotStr;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(Boolean status, String ids)throws Exception {
        logService.insertLog("浠撳簱",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ENABLED).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        List<Long> depotIds = StringUtil.strToLongList(ids);
        Depot depot = new Depot();
        depot.setEnabled(status);
        DepotExample example = new DepotExample();
        example.createCriteria().andIdIn(depotIds);
        int result=0;
        try{
            result = depotMapper.updateByExampleSelective(depot, example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }
}
