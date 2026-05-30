package com.jsh.erp.service;


/**
 * 商品扩展信息 Service
 * 提供商品扩展属性的业务逻辑：查询/保存多单位和价格信息
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.MaterialExtend;
import com.jsh.erp.datasource.entities.MaterialExtendExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.MaterialExtendMapper;
import com.jsh.erp.datasource.mappers.MaterialExtendMapperEx;
import com.jsh.erp.datasource.vo.MaterialExtendVo4List;
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
import java.util.*;


@Service
public class MaterialExtendService {
    private Logger logger = LoggerFactory.getLogger(MaterialExtendService.class);

    @Resource
    private MaterialExtendMapper materialExtendMapper;
    @Resource
    private MaterialExtendMapperEx materialExtendMapperEx;
    @Resource
    private UserService userService;
    @Resource
    private RedisService redisService;
    
    public MaterialExtend getMaterialExtend(long id)throws Exception {
        MaterialExtend result=null;
        try{
            result=materialExtendMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }
    public List<MaterialExtendVo4List> getDetailList(Long materialId) {
        List<MaterialExtendVo4List> list=null;
        try{
            list = materialExtendMapperEx.getDetailList(materialId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<MaterialExtend> getListByMIds(List<Long> idList) {
        List<MaterialExtend> meList = null;
        try{
            Long [] idArray= StringUtil.listToLongArray(idList);
            if(idArray!=null && idArray.length>0) {
                meList = materialExtendMapperEx.getListByMId(idArray);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return meList;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public String saveDetials(JSONObject obj, String sortList, Long materialId, String type) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        JSONArray meArr = obj.getJSONArray("meList");
        JSONArray insertedJson = new JSONArray();
        JSONArray updatedJson = new JSONArray();
        JSONArray deletedJson = obj.getJSONArray("meDeleteIdList");
        JSONArray sortJson = JSONArray.parseArray(sortList);
        if (null != meArr) {
            if("insert".equals(type)){
                for (int i = 0; i < meArr.size(); i++) {
                    JSONObject tempJson = meArr.getJSONObject(i);
                    insertedJson.add(tempJson);
                }
            } else if("update".equals(type)){
                for (int i = 0; i < meArr.size(); i++) {
                    JSONObject tempJson = meArr.getJSONObject(i);
                    String tempId = tempJson.getString("id");
                    if(tempId.length()>19){
                        insertedJson.add(tempJson);
                    } else {
                        updatedJson.add(tempJson);
                    }
                }
                //閽堝澶氬睘鎬у晢鍝佽鑰冭檻鍒版湁鏉＄爜琚垹鐨勬儏鍐碉紝闇€瑕佸拰鍘熸潵鐨勬潯鐮佹槑缁嗚繘琛屽姣?                if(StringUtil.isNotEmpty(obj.getString("manySku"))) {
                    //1.鍏堟煡璇㈠師鏉ョ殑鏉＄爜鍒楄〃
                    List<MaterialExtendVo4List> meList = materialExtendMapperEx.getDetailList(materialId);
                    //2.鏋勯€犳柊鐨勬潯鐮佸垪琛╩ap
                    Map<String, String> barCodeMap = new HashMap<>();
                    for (int i = 0; i < meArr.size(); i++) {
                        JSONObject tempJson = meArr.getJSONObject(i);
                        barCodeMap.put(tempJson.getString("barCode"),tempJson.getString("barCode"));
                    }
                    //3.濡傛灉鑰佺殑鏉＄爜鍦ㄦ柊鐨勯噷闈笉瀛樺湪锛屽垯涓㈠叆鍒犻櫎闃熷垪
                    for(MaterialExtendVo4List me: meList) {
                        if(barCodeMap.get(me.getBarCode()) == null) {
                            deletedJson.add(me.getId());
                        }
                    }
                }
            }
        }
        if (null != deletedJson) {
            StringBuffer bf=new StringBuffer();
            for (int i = 0; i < deletedJson.size(); i++) {
                bf.append(deletedJson.getString(i));
                if(i<(deletedJson.size()-1)){
                    bf.append(",");
                }
            }
            if(StringUtil.isNotEmpty(bf.toString())) {
                this.batchDeleteMaterialExtendByIds(bf.toString(), request);
            }
        }
        if (null != insertedJson) {
            for (int i = 0; i < insertedJson.size(); i++) {
                MaterialExtend materialExtend = new MaterialExtend();
                JSONObject tempInsertedJson = JSONObject.parseObject(insertedJson.getString(i));
                materialExtend.setMaterialId(materialId);
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("barCode"))) {
                    int exist = checkIsBarCodeExist(0L, tempInsertedJson.getString("barCode"));
                    if(exist>0) {
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_BARCODE_EXISTS_CODE,
                                String.format(ExceptionConstants.MATERIAL_BARCODE_EXISTS_MSG,tempInsertedJson.getString("barCode")));
                    } else {
                        materialExtend.setBarCode(tempInsertedJson.getString("barCode"));
                    }
                }
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("commodityUnit"))) {
                    materialExtend.setCommodityUnit(tempInsertedJson.getString("commodityUnit"));
                }
                if (tempInsertedJson.get("sku")!=null) {
                    materialExtend.setSku(tempInsertedJson.getString("sku"));
                }
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("purchaseDecimal"))) {
                    materialExtend.setPurchaseDecimal(tempInsertedJson.getBigDecimal("purchaseDecimal"));
                }
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("commodityDecimal"))) {
                    materialExtend.setCommodityDecimal(tempInsertedJson.getBigDecimal("commodityDecimal"));
                }
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("wholesaleDecimal"))) {
                    materialExtend.setWholesaleDecimal(tempInsertedJson.getBigDecimal("wholesaleDecimal"));
                }
                if (StringUtils.isNotEmpty(tempInsertedJson.getString("lowDecimal"))) {
                    materialExtend.setLowDecimal(tempInsertedJson.getBigDecimal("lowDecimal"));
                }
                this.insertMaterialExtend(materialExtend);
            }
        }
        if (null != updatedJson) {
            for (int i = 0; i < updatedJson.size(); i++) {
                JSONObject tempUpdatedJson = JSONObject.parseObject(updatedJson.getString(i));
                MaterialExtend materialExtend = new MaterialExtend();
                materialExtend.setId(tempUpdatedJson.getLong("id"));
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("barCode"))) {
                    int exist = checkIsBarCodeExist(tempUpdatedJson.getLong("id"), tempUpdatedJson.getString("barCode"));
                    if(exist>0) {
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_BARCODE_EXISTS_CODE,
                                String.format(ExceptionConstants.MATERIAL_BARCODE_EXISTS_MSG,tempUpdatedJson.getString("barCode")));
                    } else {
                        materialExtend.setBarCode(tempUpdatedJson.getString("barCode"));
                    }
                }
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("commodityUnit"))) {
                    materialExtend.setCommodityUnit(tempUpdatedJson.getString("commodityUnit"));
                }
                if (tempUpdatedJson.get("sku")!=null) {
                    materialExtend.setSku(tempUpdatedJson.getString("sku"));
                }
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("purchaseDecimal"))) {
                    materialExtend.setPurchaseDecimal(tempUpdatedJson.getBigDecimal("purchaseDecimal"));
                }
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("commodityDecimal"))) {
                    materialExtend.setCommodityDecimal(tempUpdatedJson.getBigDecimal("commodityDecimal"));
                }
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("wholesaleDecimal"))) {
                    materialExtend.setWholesaleDecimal(tempUpdatedJson.getBigDecimal("wholesaleDecimal"));
                }
                if (StringUtils.isNotEmpty(tempUpdatedJson.getString("lowDecimal"))) {
                    materialExtend.setLowDecimal(tempUpdatedJson.getBigDecimal("lowDecimal"));
                }
                this.updateMaterialExtend(materialExtend);
                //濡傛灉閲戦涓虹┖锛屾澶勫崟鐙疆绌?                materialExtendMapperEx.specialUpdatePrice(materialExtend);
            }
        }
        //澶勭悊鏉＄爜鐨勬帓搴忥紝鍩烘湰鍗曚綅鎺掔涓€涓?        if (null != sortJson && sortJson.size()>0) {
            //姝ゅ涓烘洿鏂扮殑閫昏緫
            for (int i = 0; i < sortJson.size(); i++) {
                JSONObject tempSortJson = JSONObject.parseObject(sortJson.getString(i));
                MaterialExtend materialExtend = new MaterialExtend();
                if(StringUtil.isExist(tempSortJson.get("id"))) {
                    materialExtend.setId(tempSortJson.getLong("id"));
                }
                if(StringUtil.isExist(tempSortJson.get("defaultFlag"))) {
                    materialExtend.setDefaultFlag(tempSortJson.getString("defaultFlag"));
                }
                this.updateMaterialExtend(materialExtend);
            }
        } else {
            //鏂板鐨勬椂鍊欏皢绗竴鏉¤褰曡缃负榛樿鍩烘湰鍗曚綅
            MaterialExtendExample example = new MaterialExtendExample();
            example.createCriteria().andMaterialIdEqualTo(materialId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            List<MaterialExtend> meList = materialExtendMapper.selectByExample(example);
            if(meList!=null) {
                for(int i=0; i<meList.size(); i++) {
                    MaterialExtend materialExtend = new MaterialExtend();
                    materialExtend.setId(meList.get(i).getId());
                    if(i==0) {
                        materialExtend.setDefaultFlag("1"); //榛樿
                    } else {
                        materialExtend.setDefaultFlag("0"); //闈為粯璁?                    }
                    this.updateMaterialExtend(materialExtend);
                }
            }
        }
        return null;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertMaterialExtend(MaterialExtend materialExtend)throws Exception {
        User user = userService.getCurrentUser();
        materialExtend.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
        materialExtend.setCreateTime(new Date());
        materialExtend.setUpdateTime(new Date().getTime());
        materialExtend.setCreateSerial(user.getLoginName());
        materialExtend.setUpdateSerial(user.getLoginName());
        int result =0;
        try{
            result= materialExtendMapper.insertSelective(materialExtend);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateMaterialExtend(MaterialExtend materialExtend) throws Exception{
        User user = userService.getCurrentUser();
        materialExtend.setUpdateTime(System.currentTimeMillis());
        materialExtend.setUpdateSerial(user.getLoginName());
        int res =0;
        try{
            res= materialExtendMapper.updateByPrimaryKeySelective(materialExtend);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return res;
    }

    public int checkIsBarCodeExist(Long id, String barCode)throws Exception {
        MaterialExtendExample example = new MaterialExtendExample();
        MaterialExtendExample.Criteria criteria = example.createCriteria();
        criteria.andBarCodeEqualTo(barCode);
        if (id > 0) {
            criteria.andIdNotEqualTo(id).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        } else {
            criteria.andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        }
        List<MaterialExtend> list =null;
        try{
            list = materialExtendMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteMaterialExtend(Long id, HttpServletRequest request)throws Exception {
        int result =0;
        MaterialExtend materialExtend = new MaterialExtend();
        materialExtend.setId(id);
        materialExtend.setDeleteFlag(BusinessConstants.DELETE_FLAG_DELETED);
        Long userId = Long.parseLong(redisService.getObjectFromSessionByKey(request,"userId").toString());
        User user = userService.getUser(userId);
        materialExtend.setUpdateTime(new Date().getTime());
        materialExtend.setUpdateSerial(user.getLoginName());
        try{
            result= materialExtendMapper.updateByPrimaryKeySelective(materialExtend);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteMaterialExtendByIds(String ids, HttpServletRequest request) throws Exception{
        String [] idArray=ids.split(",");
        int result = 0;
        try{
            result = materialExtendMapperEx.batchDeleteMaterialExtendByIds(idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int insertMaterialExtend(JSONObject obj, HttpServletRequest request) throws Exception{
        MaterialExtend materialExtend = JSONObject.parseObject(obj.toJSONString(), MaterialExtend.class);
        int result=0;
        try{
            result = materialExtendMapper.insertSelective(materialExtend);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int updateMaterialExtend(JSONObject obj, HttpServletRequest request)throws Exception {
        MaterialExtend materialExtend = JSONObject.parseObject(obj.toJSONString(), MaterialExtend.class);
        int result=0;
        try{
            result = materialExtendMapper.updateByPrimaryKeySelective(materialExtend);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<MaterialExtend> getMaterialExtendByTenantAndTime(Long tenantId, Long lastTime, Long syncNum)throws Exception {
        List<MaterialExtend> list=new ArrayList<MaterialExtend>();
        try{
            //鍏堣幏鍙栨渶澶х殑鏃堕棿鎴筹紝鍐嶆煡涓や釜鏃堕棿鎴充箣闂寸殑鏁版嵁锛岃繖鏍峰悓姝ヨ兘澶熼槻姝涪澶辨暟鎹紙搴斾负鏃堕棿鎴虫湁閲嶅锛?            Long maxTime = materialExtendMapperEx.getMaxTimeByTenantAndTime(tenantId, lastTime, syncNum);
            if(tenantId!=null && lastTime!=null && maxTime!=null) {
                MaterialExtendExample example = new MaterialExtendExample();
                example.createCriteria().andTenantIdEqualTo(tenantId)
                        .andUpdateTimeGreaterThan(lastTime)
                        .andUpdateTimeLessThanOrEqualTo(maxTime);
                list=materialExtendMapper.selectByExample(example);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long selectIdByMaterialIdAndDefaultFlag(Long materialId, String defaultFlag) {
        Long id = 0L;
        MaterialExtendExample example = new MaterialExtendExample();
        example.createCriteria().andMaterialIdEqualTo(materialId).andDefaultFlagEqualTo(defaultFlag)
                                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialExtend> list = materialExtendMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            id = list.get(0).getId();
        }
        return id;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long selectIdByMaterialIdAndBarCode(Long materialId, String barCode) {
        Long id = 0L;
        MaterialExtendExample example = new MaterialExtendExample();
        example.createCriteria().andMaterialIdEqualTo(materialId).andBarCodeEqualTo(barCode)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialExtend> list = materialExtendMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            id = list.get(0).getId();
        }
        return id;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public List<MaterialExtend> getListByMaterialIdAndDefaultFlagAndBarCode(Long materialId, String defaultFlag, String barCode) {
        MaterialExtendExample example = new MaterialExtendExample();
        example.createCriteria().andMaterialIdEqualTo(materialId).andDefaultFlagEqualTo(defaultFlag).andBarCodeNotEqualTo(barCode)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        return materialExtendMapper.selectByExample(example);
    }

    public MaterialExtend getInfoByBarCode(String barCode)throws Exception {
        MaterialExtendExample example = new MaterialExtendExample();
        example.createCriteria().andBarCodeEqualTo(barCode)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialExtend> list = materialExtendMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 鍟嗗搧鐨勫壇鏉＄爜鍜屾暟鎹簱閲岄潰鐨勫晢鍝佹潯鐮佸瓨鍦ㄩ噸澶嶏紙闄よ嚜韬晢鍝佷箣澶栵級
     * @param manyBarCode
     * @param barCode
     * @return
     */
    public int getCountByManyBarCodeWithoutUs(String manyBarCode, String barCode) {
        MaterialExtendExample example = new MaterialExtendExample();
        example.createCriteria().andBarCodeEqualTo(manyBarCode).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialExtend> list = materialExtendMapper.selectByExample(example);
        if(list!=null && list.size()>0) {
            for(MaterialExtend me: list) {
                List<MaterialExtend> basicMeList = materialExtendMapperEx.getBasicInfoByMid(me.getMaterialId());
                for(MaterialExtend basicMe: basicMeList) {
                    if(basicMe!=null && !barCode.equals(basicMe.getBarCode())) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }
}
