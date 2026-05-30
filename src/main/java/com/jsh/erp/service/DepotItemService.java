package com.jsh.erp.service;


/**
 * 单据明细 Service
 * 提供单据明细行的业务逻辑：新增/编辑/删除/查询/库存计算
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.datasource.vo.DepotItemStockWarningCount;
import com.jsh.erp.datasource.vo.DepotItemVo4Stock;
import com.jsh.erp.datasource.vo.DepotItemVoBatchNumberList;
import com.jsh.erp.datasource.vo.InOutPriceVo;
import com.jsh.erp.datasource.vo.AiPredictionSaveItem;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.StringUtil;
import com.jsh.erp.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class DepotItemService {
    private Logger logger = LoggerFactory.getLogger(DepotItemService.class);

    private final static String TYPE = "鍏ュ簱";
    private final static String SUM_TYPE = "number";
    private final static String IN = "in";
    private final static String OUT = "out";

    @Resource
    private DepotItemMapper depotItemMapper;
    @Resource
    private DepotItemMapperEx depotItemMapperEx;
    @Resource
    private MaterialService materialService;
    @Resource
    private MaterialExtendService materialExtendService;
    @Resource
    private SerialNumberMapperEx serialNumberMapperEx;
    @Resource
    private DepotHeadService depotHeadService;
    @Resource
    private DepotHeadMapper depotHeadMapper;
    @Resource
    private SerialNumberService serialNumberService;
    @Resource
    private UserService userService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private DepotService depotService;
    @Resource
    private UnitService unitService;
    @Resource
    private MaterialCurrentStockMapper materialCurrentStockMapper;
    @Resource
    private MaterialCurrentStockMapperEx materialCurrentStockMapperEx;
    @Resource
    private LogService logService;

    public DepotItem getDepotItem(long id)throws Exception {
        DepotItem result=null;
        try{
            result=depotItemMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<DepotItem> getDepotItem()throws Exception {
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotItem> list=null;
        try{
            list=depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotItem> select(String name, Integer type, String remark, int offset, int rows)throws Exception {
        List<DepotItem> list=null;
        try{
            list=depotItemMapperEx.selectByConditionDepotItem(name, type, remark, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long countDepotItem(String name, Integer type, String remark) throws Exception{
        Long result =null;
        try{
            result=depotItemMapperEx.countsByDepotItem(name, type, remark);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepotItem(JSONObject obj, HttpServletRequest request)throws Exception {
        DepotItem depotItem = JSONObject.parseObject(obj.toJSONString(), DepotItem.class);
        int result =0;
        try{
            result=depotItemMapper.insertSelective(depotItem);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepotItem(JSONObject obj, HttpServletRequest request)throws Exception {
        DepotItem depotItem = JSONObject.parseObject(obj.toJSONString(), DepotItem.class);
        int result =0;
        try{
            result=depotItemMapper.updateByPrimaryKeySelective(depotItem);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteDepotItem(Long id, HttpServletRequest request)throws Exception {
        int result =0;
        try{
            result=depotItemMapper.deleteByPrimaryKey(id);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotItem(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andIdIn(idList);
        int result =0;
        try{
            result=depotItemMapper.deleteByExample(example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        DepotItemExample example = new DepotItemExample();
        example.createCriteria().andIdNotEqualTo(id).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotItem> list =null;
        try{
            list=depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<DepotItemVo4DetailByTypeAndMId> findDetailByDepotIdsAndMaterialIdList(String depotIds, Boolean forceFlag, Boolean inOutManageFlag, String sku, String batchNumber,
                                                                                      String number, String beginTime, String endTime, Long mId, Integer offset, Integer rows)throws Exception {
        String[] depotIdArrOld = null;
        if(StringUtil.isNotEmpty(depotIds)) {
            depotIdArrOld = depotIds.split(",");
        }
        List<Long> depotList = depotService.parseDepotListByArr(depotIdArrOld);
        Long[] depotIdArray = StringUtil.listToLongArray(depotList);
        List<DepotItemVo4DetailByTypeAndMId> list =null;
        try{
            list = depotItemMapperEx.findDetailByDepotIdsAndMaterialIdList(depotIdArray, forceFlag, inOutManageFlag, sku, batchNumber, number, beginTime, endTime, mId, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long findDetailByDepotIdsAndMaterialIdCount(String depotIds, Boolean forceFlag, Boolean inOutManageFlag, String sku, String batchNumber,
                                                       String number, String beginTime, String endTime, Long mId)throws Exception {
        String[] depotIdArrOld = null;
        if(StringUtil.isNotEmpty(depotIds)) {
            depotIdArrOld = depotIds.split(",");
        }
        List<Long> depotList = depotService.parseDepotListByArr(depotIdArrOld);
        Long[] depotIdArray = StringUtil.listToLongArray(depotList);
        Long result =null;
        try{
            result = depotItemMapperEx.findDetailByDepotIdsAndMaterialIdCount(depotIdArray, forceFlag, inOutManageFlag, sku, batchNumber, number, beginTime, endTime, mId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepotItemWithObj(DepotItem depotItem)throws Exception {
        int result =0;
        try{
            result = depotItemMapper.insertSelective(depotItem);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepotItemWithObj(DepotItem depotItem)throws Exception {
        int result =0;
        try{
            result = depotItemMapper.updateByPrimaryKeySelective(depotItem);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<DepotItem> getListByHeaderId(Long headerId)throws Exception {
        List<DepotItem> list =null;
        try{
            DepotItemExample example = new DepotItemExample();
            example.createCriteria().andHeaderIdEqualTo(headerId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            list = depotItemMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 鏌ヨ褰撳墠鍗曟嵁涓寚瀹氬晢鍝佺殑鏄庣粏淇℃伅
     * @param headerId
     * @param meId
     * @return
     * @throws Exception
     */
    public DepotItem getItemByHeaderIdAndMaterial(Long headerId, Long meId)throws Exception {
        DepotItem depotItem = new DepotItem();
        try{
            DepotItemExample example = new DepotItemExample();
            example.createCriteria().andHeaderIdEqualTo(headerId).andMaterialExtendIdEqualTo(meId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            List<DepotItem> list = depotItemMapper.selectByExample(example);
            if(list!=null && list.size()>0) {
                depotItem = list.get(0);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return depotItem;
    }

    /**
     * 鏌ヨ琚叧鑱旇鍗曚腑鎸囧畾鍟嗗搧鐨勬槑缁嗕俊鎭?     * @param linkStr
     * @param meId
     * @return
     * @throws Exception
     */
    public DepotItem getPreItemByHeaderIdAndMaterial(String linkStr, Long meId, Long linkId)throws Exception {
        DepotItem depotItem = new DepotItem();
        try{
            DepotHead depotHead = depotHeadService.getDepotHead(linkStr);
            if(null!=depotHead && null!=depotHead.getId()) {
                DepotItemExample example = new DepotItemExample();
                example.createCriteria().andHeaderIdEqualTo(depotHead.getId()).andMaterialExtendIdEqualTo(meId).andIdEqualTo(linkId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                List<DepotItem> list = depotItemMapper.selectByExample(example);
                if(list!=null && list.size()>0) {
                    depotItem = list.get(0);
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return depotItem;
    }

    public List<DepotItemVo4WithInfoEx> getDetailList(Long headerId)throws Exception {
        List<DepotItemVo4WithInfoEx> list =null;
        try{
            list = depotItemMapperEx.getDetailList(headerId);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotItemVo4WithInfoEx> getInOutStock(String materialParam, List<Long> categoryIdList, String endTime, Integer offset, Integer rows)throws Exception {
        List<DepotItemVo4WithInfoEx> list =null;
        try{
            list = depotItemMapperEx.getInOutStock(materialParam, categoryIdList, endTime, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int getInOutStockCount(String materialParam, List<Long> categoryIdList, String endTime)throws Exception {
        int result=0;
        try{
            result = depotItemMapperEx.getInOutStockCount(materialParam, categoryIdList, endTime);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<DepotItemVo4WithInfoEx> getListWithBuyOrSale(String materialParam, String billType,
                                                             String beginTime, String endTime, String[] creatorArray, Long organId, String[] organArray, List<Long> categoryList, List<Long> depotList, Boolean forceFlag, Integer offset, Integer rows)throws Exception {
        List<DepotItemVo4WithInfoEx> list =null;
        try{
            list = depotItemMapperEx.getListWithBuyOrSale(materialParam, billType, beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int getListWithBuyOrSaleCount(String materialParam, String billType,
                                         String beginTime, String endTime, String[] creatorArray, Long organId, String[] organArray, List<Long> categoryList, List<Long> depotList, Boolean forceFlag)throws Exception {
        int result=0;
        try{
            result = depotItemMapperEx.getListWithBuyOrSaleCount(materialParam, billType, beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public BigDecimal buyOrSale(String type, String subType, Long meId, String beginTime, String endTime,
                                String[] creatorArray, Long organId, String [] organArray, List<Long> depotList, Boolean forceFlag, String sumType) throws Exception{
        BigDecimal result= BigDecimal.ZERO;
        try{
            if (SUM_TYPE.equals(sumType)) {
                result= depotItemMapperEx.buyOrSaleNumber(type, subType, meId, beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, sumType);
            } else {
                result= depotItemMapperEx.buyOrSalePrice(type, subType, meId, beginTime, endTime, creatorArray, organId, organArray, depotList, forceFlag, sumType);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public BigDecimal buyOrSalePriceTotal(String type, String subType, String materialParam, String beginTime, String endTime,
                                String[] creatorArray, Long organId, String [] organArray, List<Long> categoryList, List<Long> depotList, Boolean forceFlag) throws Exception{
        BigDecimal result= BigDecimal.ZERO;
        try{
            result= depotItemMapperEx.buyOrSalePriceTotal(type, subType, materialParam, beginTime, endTime, creatorArray, organId, organArray, categoryList, depotList, forceFlag);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;

    }

    /**
     * 缁熻閲囪喘銆侀攢鍞€侀浂鍞殑鎬婚噾棰濆垪琛?     * @param beginTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public List<InOutPriceVo> inOrOutPriceList(String beginTime, String endTime) throws Exception{
        List<InOutPriceVo> result = new ArrayList<>();
        try{
            String [] creatorArray = depotHeadService.getCreatorArray();
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            result = depotItemMapperEx.inOrOutPriceList(beginTime, endTime, creatorArray, forceFlag);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void saveDetials(String rows, Long headerId, String actionType, HttpServletRequest request) throws Exception{
        //鏌ヨ鍗曟嵁涓昏〃淇℃伅
        DepotHead depotHead =depotHeadMapper.selectByPrimaryKey(headerId);
        //鍒犻櫎搴忓垪鍙峰拰鍥炴敹搴忓垪鍙?        deleteOrCancelSerialNumber(actionType, depotHead, headerId);
        //鍒犻櫎鍗曟嵁鐨勬槑缁?        deleteDepotItemHeadId(headerId);
        JSONArray rowArr = JSONArray.parseArray(rows);
        if (null != rowArr && rowArr.size()>0) {
            //閽堝缁勮鍗曘€佹媶鍗稿崟鏍￠獙鏄惁瀛樺湪缁勫悎浠跺拰鏅€氬瓙浠?            checkAssembleWithMaterialType(rowArr, depotHead.getSubType());
            //鏍￠獙澶氳鏄庣粏褰撲腑鏄惁瀛樺湪閲嶅鐨勫簭鍒楀彿
            checkSerialNumberRepeat(rowArr);
            for (int i = 0; i < rowArr.size(); i++) {
                DepotItem depotItem = new DepotItem();
                JSONObject rowObj = JSONObject.parseObject(rowArr.getString(i));
                depotItem.setHeaderId(headerId);
                String barCode = rowObj.getString("barCode");
                MaterialExtend materialExtend = materialExtendService.getInfoByBarCode(barCode);
                if(materialExtend == null) {
                    throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_BARCODE_IS_NOT_EXIST_CODE,
                            String.format(ExceptionConstants.MATERIAL_BARCODE_IS_NOT_EXIST_MSG, barCode));
                }
                depotItem.setMaterialId(materialExtend.getMaterialId());
                depotItem.setMaterialExtendId(materialExtend.getId());
                depotItem.setMaterialUnit(rowObj.getString("unit"));
                Material material= materialService.getMaterial(depotItem.getMaterialId());
                if (BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber()) ||
                        BusinessConstants.ENABLE_BATCH_NUMBER_ENABLED.equals(material.getEnableBatchNumber())) {
                    //缁勮鎷嗗嵏鍗曚笉鑳介€夋嫨鎵瑰彿鎴栧簭鍒楀彿鍟嗗搧
                    if(BusinessConstants.SUB_TYPE_ASSEMBLE.equals(depotHead.getSubType()) ||
                            BusinessConstants.SUB_TYPE_DISASSEMBLE.equals(depotHead.getSubType())) {
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_ASSEMBLE_SELECT_ERROR_CODE,
                                String.format(ExceptionConstants.MATERIAL_ASSEMBLE_SELECT_ERROR_MSG, barCode));
                    }
                    //璋冩嫧鍗曚笉鑳介€夋嫨鎵瑰彿鎴栧簭鍒楀彿鍟嗗搧锛堣鍦烘櫙璧板嚭搴撳拰鍏ュ簱鍗曪級
                    if(BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_TRANSFER_SELECT_ERROR_CODE,
                                String.format(ExceptionConstants.MATERIAL_TRANSFER_SELECT_ERROR_MSG, barCode));
                    }
                    //鐩樼偣涓氬姟涓嶈兘閫夋嫨鎵瑰彿鎴栧簭鍒楀彿鍟嗗搧锛堣鍦烘櫙璧板嚭搴撳拰鍏ュ簱鍗曪級
                    if(BusinessConstants.SUB_TYPE_CHECK_ENTER.equals(depotHead.getSubType())
                       ||BusinessConstants.SUB_TYPE_REPLAY.equals(depotHead.getSubType())) {
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_STOCK_CHECK_ERROR_CODE,
                                String.format(ExceptionConstants.MATERIAL_STOCK_CHECK_ERROR_MSG, barCode));
                    }
                }
                if (StringUtil.isExist(rowObj.get("snList"))) {
                    depotItem.setSnList(rowObj.getString("snList"));
                    if(StringUtil.isExist(rowObj.get("depotId"))) {
                        String [] snArray = depotItem.getSnList().split(",");
                        int operNum = rowObj.getInteger("operNumber");
                        if(snArray.length == operNum) {
                            Long depotId = rowObj.getLong("depotId");
                            BigDecimal inPrice = BigDecimal.ZERO;
                            if (StringUtil.isExist(rowObj.get("unitPrice"))) {
                                inPrice = rowObj.getBigDecimal("unitPrice");
                            }
                            serialNumberService.addSerialNumberByBill(depotHead.getType(), depotHead.getSubType(),
                                    depotHead.getNumber(), materialExtend.getMaterialId(), depotId, inPrice, depotItem.getSnList());
                        } else {
                            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_SN_NUMBERE_FAILED_CODE,
                                    String.format(ExceptionConstants.DEPOT_HEAD_SN_NUMBERE_FAILED_MSG, barCode));
                        }
                    }
                } else {
                    //鍏ュ簱鎴栧嚭搴?                    if (BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType()) ||
                            BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())) {
                        //搴忓垪鍙蜂笉鑳戒负绌?                        if (BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())) {
                            //濡傛灉寮€鍚嚭鍏ュ簱绠＄悊锛屽苟涓旂被鍨嬬瓑浜庨噰璐€侀噰璐€€璐с€侀攢鍞€侀攢鍞€€璐э紝鍒欒烦杩?                            if(systemConfigService.getInOutManageFlag() &&
                                    (BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES_RETURN.equals(depotHead.getSubType()))) {
                                //璺宠繃
                            } else {
                                throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_SERIAL_NUMBERE_EMPTY_CODE,
                                        String.format(ExceptionConstants.MATERIAL_SERIAL_NUMBERE_EMPTY_MSG, barCode));
                            }
                        }
                    }
                }
                if (StringUtil.isExist(rowObj.get("batchNumber"))) {
                    depotItem.setBatchNumber(rowObj.getString("batchNumber"));
                } else {
                    //鍏ュ簱鎴栧嚭搴?                    if(BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType()) ||
                            BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())) {
                        //鎵瑰彿涓嶈兘涓虹┖
                        if (BusinessConstants.ENABLE_BATCH_NUMBER_ENABLED.equals(material.getEnableBatchNumber())) {
                            //濡傛灉寮€鍚嚭鍏ュ簱绠＄悊锛屽苟涓旂被鍨嬬瓑浜庨噰璐€侀噰璐€€璐с€侀攢鍞€侀攢鍞€€璐э紝鍒欒烦杩?                            if(systemConfigService.getInOutManageFlag() &&
                                    (BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES_RETURN.equals(depotHead.getSubType()))) {
                                //璺宠繃
                            } else {
                                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_BATCH_NUMBERE_EMPTY_CODE,
                                        String.format(ExceptionConstants.DEPOT_HEAD_BATCH_NUMBERE_EMPTY_MSG, barCode));
                            }
                        }
                    }
                }
                if (StringUtil.isExist(rowObj.get("expirationDate"))) {
                    depotItem.setExpirationDate(rowObj.getDate("expirationDate"));
                }
                if (StringUtil.isExist(rowObj.get("sku"))) {
                    depotItem.setSku(rowObj.getString("sku"));
                }
                if (StringUtil.isExist(rowObj.get("linkId"))) {
                    depotItem.setLinkId(rowObj.getLong("linkId"));
                }
                //浠ヤ笅杩涜鍗曚綅鎹㈢畻
                Unit unitInfo = materialService.findUnit(materialExtend.getMaterialId()); //鏌ヨ澶氬崟浣嶄俊鎭?                if (StringUtil.isExist(rowObj.get("operNumber"))) {
                    depotItem.setOperNumber(rowObj.getBigDecimal("operNumber"));
                    String unit = rowObj.get("unit").toString();
                    BigDecimal oNumber = rowObj.getBigDecimal("operNumber");
                    if (StringUtil.isNotEmpty(unitInfo.getName())) {
                        String basicUnit = unitInfo.getBasicUnit(); //鍩烘湰鍗曚綅
                        if (unit.equals(basicUnit)) { //濡傛灉绛変簬鍩烘湰鍗曚綅
                            depotItem.setBasicNumber(oNumber); //鏁伴噺涓€鑷?                        } else if (unit.equals(unitInfo.getOtherUnit())) { //濡傛灉绛変簬鍓崟浣?                            depotItem.setBasicNumber(oNumber.multiply(unitInfo.getRatio())); //鏁伴噺涔樹互姣斾緥
                        } else if (unit.equals(unitInfo.getOtherUnitTwo())) { //濡傛灉绛変簬鍓崟浣?
                            depotItem.setBasicNumber(oNumber.multiply(unitInfo.getRatioTwo())); //鏁伴噺涔樹互姣斾緥
                        } else if (unit.equals(unitInfo.getOtherUnitThree())) { //濡傛灉绛変簬鍓崟浣?
                            depotItem.setBasicNumber(oNumber.multiply(unitInfo.getRatioThree())); //鏁伴噺涔樹互姣斾緥
                        } else {
                            depotItem.setBasicNumber(oNumber); //鏁伴噺涓€鑷?                        }
                    } else {
                        depotItem.setBasicNumber(oNumber); //鍏朵粬鎯呭喌
                    }
                }
                //濡傛灉鏁伴噺+宸插畬鎴愭暟閲?鍘熻鍗曟暟閲忥紝缁欏嚭棰勮(鍒ゆ柇鍓嶆彁鏄瓨鍦ㄥ叧鑱旇鍗晐鍏宠仈璇疯喘鍗?
                String linkStr = StringUtil.isNotEmpty(depotHead.getLinkNumber())? depotHead.getLinkNumber(): depotHead.getLinkApply();
                if (StringUtil.isNotEmpty(linkStr) && StringUtil.isExist(rowObj.get("preNumber")) && StringUtil.isExist(rowObj.get("finishNumber"))) {
                    if("add".equals(actionType)) {
                        //鍦ㄦ柊澧炴ā寮忚繘琛岀姸鎬佽祴鍊?                        BigDecimal preNumber = rowObj.getBigDecimal("preNumber");
                        BigDecimal finishNumber = rowObj.getBigDecimal("finishNumber");
                        if(depotItem.getOperNumber().add(finishNumber).compareTo(preNumber)>0) {
                            if(!systemConfigService.getOverLinkBillFlag()) {
                                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_NUMBER_NEED_EDIT_FAILED_CODE,
                                        String.format(ExceptionConstants.DEPOT_HEAD_NUMBER_NEED_EDIT_FAILED_MSG, barCode));
                            }
                        }
                    } else if("update".equals(actionType)) {
                        //褰撳墠鍗曟嵁鐨勭被鍨?                        String currentSubType = depotHead.getSubType();
                        //鍦ㄦ洿鏂版ā寮忚繘琛岀姸鎬佽祴鍊?                        String unit = rowObj.get("unit").toString();
                        Long preHeaderId = depotHeadService.getDepotHead(linkStr).getId();
                        if(null!=preHeaderId) {
                            //鍓嶄竴涓崟鎹殑鏁伴噺
                            BigDecimal preNumber = getPreItemByHeaderIdAndMaterial(linkStr, depotItem.getMaterialExtendId(), depotItem.getLinkId()).getOperNumber();
                            //闄ゅ幓姝ゅ崟鎹箣澶栫殑宸插叆搴搢宸插嚭搴?                            BigDecimal realFinishNumber = getRealFinishNumber(currentSubType, depotItem.getMaterialExtendId(), depotItem.getLinkId(), preHeaderId, headerId, unitInfo, unit);
                            if(preNumber!=null) {
                                if (depotItem.getOperNumber().add(realFinishNumber).compareTo(preNumber) > 0) {
                                    if (!systemConfigService.getOverLinkBillFlag()) {
                                        throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_NUMBER_NEED_EDIT_FAILED_CODE,
                                                String.format(ExceptionConstants.DEPOT_HEAD_NUMBER_NEED_EDIT_FAILED_MSG, barCode));
                                    }
                                }
                            } else {
                                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_PRE_BILL_IS_CHANGE_CODE,
                                        ExceptionConstants.DEPOT_ITEM_PRE_BILL_IS_CHANGE_MSG);
                            }
                        }
                    }
                }
                if (StringUtil.isExist(rowObj.get("unitPrice"))) {
                    BigDecimal unitPrice = rowObj.getBigDecimal("unitPrice");
                    depotItem.setUnitPrice(unitPrice);
                    if(materialExtend.getLowDecimal()!=null) {
                        //闆跺敭鎴栭攢鍞崟浠蜂綆浜庢渶浣庡敭浠凤紝杩涜鎻愮ず
                        if("闆跺敭".equals(depotHead.getSubType()) || "閿€鍞?.equals(depotHead.getSubType())) {
                            if (unitPrice.compareTo(materialExtend.getLowDecimal()) < 0) {
                                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_UNIT_PRICE_LOW_CODE,
                                        String.format(ExceptionConstants.DEPOT_HEAD_UNIT_PRICE_LOW_MSG, barCode));
                            }
                        }
                    }
                }
                //濡傛灉鏄攢鍞嚭搴撱€侀攢鍞€€璐с€侀浂鍞嚭搴撱€侀浂鍞€€璐у垯缁欓噰璐崟浠峰瓧娈佃祴鍊硷紙濡傛灉鏄壒娆″晢鍝侊紝鍒欒鏍规嵁鎵瑰彿鍘绘壘涔嬪墠鐨勫叆搴撲环锛?                if(BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType()) ||
                    BusinessConstants.SUB_TYPE_SALES_RETURN.equals(depotHead.getSubType()) ||
                    BusinessConstants.SUB_TYPE_RETAIL.equals(depotHead.getSubType()) ||
                    BusinessConstants.SUB_TYPE_RETAIL_RETURN.equals(depotHead.getSubType())) {
                    boolean moveAvgPriceFlag = systemConfigService.getMoveAvgPriceFlag();
                    BigDecimal currentUnitPrice = materialCurrentStockMapperEx.getCurrentUnitPriceByMId(materialExtend.getMaterialId());
                    currentUnitPrice = unitService.parseUnitPriceByUnit(currentUnitPrice, unitInfo, depotItem.getMaterialUnit());
                    BigDecimal unitPrice = moveAvgPriceFlag? currentUnitPrice: materialExtend.getPurchaseDecimal();
                    depotItem.setPurchaseUnitPrice(unitPrice);
                    if(StringUtil.isNotEmpty(depotItem.getBatchNumber())) {
                        depotItem.setPurchaseUnitPrice(getDepotItemByBatchNumber(depotItem.getMaterialExtendId(),depotItem.getBatchNumber()).getUnitPrice());
                    }
                }
                if (StringUtil.isExist(rowObj.get("taxUnitPrice"))) {
                    depotItem.setTaxUnitPrice(rowObj.getBigDecimal("taxUnitPrice"));
                }
                if (StringUtil.isExist(rowObj.get("allPrice"))) {
                    depotItem.setAllPrice(rowObj.getBigDecimal("allPrice"));
                }
                if (StringUtil.isExist(rowObj.get("depotId"))) {
                    depotItem.setDepotId(rowObj.getLong("depotId"));
                } else {
                    if(!BusinessConstants.SUB_TYPE_PURCHASE_APPLY.equals(depotHead.getSubType())
                            && !BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(depotHead.getSubType())
                            && !BusinessConstants.SUB_TYPE_SALES_ORDER.equals(depotHead.getSubType())) {
                        throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_DEPOT_FAILED_CODE,
                                String.format(ExceptionConstants.DEPOT_HEAD_DEPOT_FAILED_MSG));
                    }
                }
                if(BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                    if (StringUtil.isExist(rowObj.get("anotherDepotId"))) {
                        if(rowObj.getLong("anotherDepotId").equals(rowObj.getLong("depotId"))) {
                            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_ANOTHER_DEPOT_EQUAL_FAILED_CODE,
                                    String.format(ExceptionConstants.DEPOT_HEAD_ANOTHER_DEPOT_EQUAL_FAILED_MSG));
                        } else {
                            depotItem.setAnotherDepotId(rowObj.getLong("anotherDepotId"));
                        }
                    } else {
                        throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_ANOTHER_DEPOT_FAILED_CODE,
                                String.format(ExceptionConstants.DEPOT_HEAD_ANOTHER_DEPOT_FAILED_MSG));
                    }
                }
                if (StringUtil.isExist(rowObj.get("taxRate"))) {
                    depotItem.setTaxRate(rowObj.getBigDecimal("taxRate"));
                }
                if (StringUtil.isExist(rowObj.get("taxMoney"))) {
                    depotItem.setTaxMoney(rowObj.getBigDecimal("taxMoney"));
                }
                if (StringUtil.isExist(rowObj.get("taxLastMoney"))) {
                    depotItem.setTaxLastMoney(rowObj.getBigDecimal("taxLastMoney"));
                }
                if (StringUtil.isExist(rowObj.get("mType"))) {
                    depotItem.setMaterialType(rowObj.getString("mType"));
                }
                if (StringUtil.isExist(rowObj.get("remark"))) {
                    depotItem.setRemark(rowObj.getString("remark"));
                }
                //鍑哄簱鏃跺垽鏂簱瀛樻槸鍚﹀厖瓒?                if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())){
                    String stockMsg = material.getName() + "-" + barCode;
                    BigDecimal stock = getCurrentStockByParam(depotItem.getDepotId(),depotItem.getMaterialId());
                    if(StringUtil.isNotEmpty(depotItem.getSku())) {
                        //瀵逛簬sku鍟嗗搧瑕佹崲涓柟寮忚绠楀簱瀛?                        stock = getSkuStockByParam(depotItem.getDepotId(),depotItem.getMaterialExtendId(),null,null);
                    }
                    if(StringUtil.isNotEmpty(depotItem.getBatchNumber())) {
                        //瀵逛簬鎵规鍟嗗搧瑕佹崲涓柟寮忚绠楀簱瀛?                        stock = getOneBatchNumberStock(depotItem.getDepotId(), barCode, depotItem.getBatchNumber());
                        stockMsg += "-鎵瑰彿" + depotItem.getBatchNumber();
                    }
                    BigDecimal thisRealNumber = depotItem.getBasicNumber()==null?BigDecimal.ZERO:depotItem.getBasicNumber();
                    if(StringUtil.isNotEmpty(depotItem.getBatchNumber())) {
                        //瀵逛簬鎵规鍟嗗搧锛岀洿鎺ヤ娇鐢ㄥ綋鍓嶅～鍐欑殑鏁伴噺
                        thisRealNumber = depotItem.getOperNumber()==null?BigDecimal.ZERO:depotItem.getOperNumber();
                    }
                    if(!systemConfigService.getMinusStockFlag() && stock.compareTo(thisRealNumber)<0){
                        throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_CODE,
                                String.format(ExceptionConstants.MATERIAL_STOCK_NOT_ENOUGH_MSG, stockMsg));
                    }
                    //鍑哄簱鏃跺鐞嗗簭鍒楀彿
                    if(!BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                        //鍒ゆ柇鍟嗗搧鏄惁寮€鍚簭鍒楀彿锛屽紑鍚殑鍞嚭搴忓垪鍙凤紝鏈紑鍚殑璺宠繃
                        if(BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED.equals(material.getEnableSerialNumber())) {
                            //濡傛灉寮€鍚嚭鍏ュ簱绠＄悊锛屽苟涓旂被鍨嬬瓑浜庨噰璐€侀噰璐€€璐с€侀攢鍞€侀攢鍞€€璐э紝鍒欒烦杩?                            if(systemConfigService.getInOutManageFlag() &&
                                    (BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())
                                            ||BusinessConstants.SUB_TYPE_SALES_RETURN.equals(depotHead.getSubType()))) {
                                //璺宠繃
                            } else {
                                //鍞嚭搴忓垪鍙凤紝鑾峰緱褰撳墠鎿嶄綔浜?                                User userInfo = userService.getCurrentUser();
                                serialNumberService.checkAndUpdateSerialNumber(depotItem, depotHead.getNumber(), userInfo, StringUtil.toNull(depotItem.getSnList()));
                            }
                        }
                    }
                }
                this.insertDepotItemWithObj(depotItem);
                //鏇存柊褰撳墠搴撳瓨
                updateCurrentStock(depotItem);
                //鏇存柊褰撳墠鎴愭湰浠?                updateCurrentUnitPrice(depotItem);
                //鏇存柊鍟嗗搧鐨勪环鏍?                updateMaterialExtendPrice(materialExtend.getId(), depotHead.getSubType(), depotHead.getBillType(), rowObj);
            }
            //濡傛灉鍏宠仈鍗曟嵁鍙烽潪绌哄垯鏇存柊璁㈠崟鐨勭姸鎬?鍗曟嵁绫诲瀷锛氶噰璐叆搴撳崟銆侀攢鍞嚭搴撳崟銆佺洏鐐瑰鐩樺崟銆佸叾瀹冨叆搴撳崟銆佸叾瀹冨嚭搴撳崟
            if(BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_REPLAY.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_OTHER.equals(depotHead.getSubType())) {
                if(StringUtil.isNotEmpty(depotHead.getLinkNumber())) {
                    //鍗曟嵁鐘舵€?鏄惁鍏ㄩ儴瀹屾垚 2-鍏ㄩ儴瀹屾垚 3-閮ㄥ垎瀹屾垚锛堥拡瀵硅鍗曠殑鍒嗘壒鍑哄叆搴擄級
                    String billStatus = getBillStatusByParam(depotHead, depotHead.getLinkNumber(), "normal");
                    changeBillStatus(depotHead.getLinkNumber(), billStatus);
                }
            }
            //褰撳墠鍗曟嵁绫诲瀷涓洪噰璐鍗曠殑閫昏緫
            if(BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(depotHead.getSubType())) {
                //濡傛灉鍏宠仈鍗曟嵁鍙烽潪绌哄垯鏇存柊璁㈠崟鐨勭姸鎬?姝ゅ閽堝閿€鍞鍗曡浆閲囪喘璁㈠崟鐨勫満鏅?                if(StringUtil.isNotEmpty(depotHead.getLinkNumber())) {
                    String billStatus = getBillStatusByParam(depotHead, depotHead.getLinkNumber(), "normal");
                    changeBillPurchaseStatus(depotHead.getLinkNumber(), billStatus);
                }
                //濡傛灉鍏宠仈鍗曟嵁鍙烽潪绌哄垯鏇存柊璁㈠崟鐨勭姸鎬?姝ゅ閽堝璇疯喘鍗曡浆閲囪喘璁㈠崟鐨勫満鏅?                if(StringUtil.isNotEmpty(depotHead.getLinkApply())) {
                    String billStatus = getBillStatusByParam(depotHead, depotHead.getLinkApply(), "apply");
                    changeBillStatus(depotHead.getLinkApply(), billStatus);
                }
            }
        } else {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_ROW_FAILED_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_ROW_FAILED_MSG));
        }
    }
    /**
     * 鍒ゆ柇鍗曟嵁鐨勭姸鎬?     * 閫氳繃鏁扮粍瀵规瘮锛氬師鍗曟嵁鐨勫晢鍝佸拰鍟嗗搧鏁伴噺锛堟眹鎬伙級 涓?鍒嗘壒鎿嶄綔鍚庡崟鎹殑鍟嗗搧鍜屽晢鍝佹暟閲忥紙姹囨€伙級
     * @param depotHead
     * @param linkStr
     * @return
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public String getBillStatusByParam(DepotHead depotHead, String linkStr, String linkType) {
        String res = BusinessConstants.BILLS_STATUS_SKIPED;
        //鑾峰彇鍘熷崟鎹殑鍟嗗搧鍜屽晢鍝佹暟閲忥紙姹囨€伙級
        List<DepotItemVo4MaterialAndSum> linkList = depotItemMapperEx.getLinkBillDetailMaterialSum(linkStr);
        //鑾峰彇鍒嗘壒鎿嶄綔鍚庡崟鎹殑鍟嗗搧鍜屽晢鍝佹暟閲忥紙姹囨€伙級
        List<DepotItemVo4MaterialAndSum> batchList = depotItemMapperEx.getBatchBillDetailMaterialSum(linkStr, linkType, depotHead.getType());
        //灏嗗垎鎵规搷浣滃悗鐨勫崟鎹殑鍟嗗搧鍜屽晢鍝佹暟鎹瀯閫犳垚Map
        Map<Long, BigDecimal> materialSumMap = new HashMap<>();
        for(DepotItemVo4MaterialAndSum materialAndSum : batchList) {
            materialSumMap.put(materialAndSum.getMaterialExtendId(), materialAndSum.getOperNumber());
        }
        for(DepotItemVo4MaterialAndSum materialAndSum : linkList) {
            //杩囨护鎺夊師鍗曢噷闈㈡湁鏁伴噺涓?鐨勫晢鍝?            if(materialAndSum.getOperNumber().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal materialSum = materialSumMap.get(materialAndSum.getMaterialExtendId());
                if (materialSum != null) {
                    if (materialSum.compareTo(materialAndSum.getOperNumber()) < 0) {
                        res = BusinessConstants.BILLS_STATUS_SKIPING;
                    }
                } else {
                    res = BusinessConstants.BILLS_STATUS_SKIPING;
                }
            }
        }
        return res;
    }

    /**
     * 鏇存柊鍗曟嵁鐘舵€?     * @param linkStr
     * @param billStatus
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void changeBillStatus(String linkStr, String billStatus) {
        DepotHead depotHeadOrders = new DepotHead();
        depotHeadOrders.setStatus(billStatus);
        DepotHeadExample example = new DepotHeadExample();
        List<String> linkNoList = StringUtil.strToStringList(linkStr);
        example.createCriteria().andNumberIn(linkNoList);
        try{
            depotHeadMapper.updateByExampleSelective(depotHeadOrders, example);
        }catch(Exception e){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],寮傚父[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE,ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
    }

    /**
     * 鏇存柊鍗曟嵁鐘舵€?姝ゅ閽堝閿€鍞鍗曡浆閲囪喘璁㈠崟鐨勫満鏅?     * @param linkStr
     * @param billStatus
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void changeBillPurchaseStatus(String linkStr, String billStatus) {
        DepotHead depotHeadOrders = new DepotHead();
        depotHeadOrders.setPurchaseStatus(billStatus);
        DepotHeadExample example = new DepotHeadExample();
        List<String> linkNoList = StringUtil.strToStringList(linkStr);
        example.createCriteria().andNumberIn(linkNoList);
        try{
            depotHeadMapper.updateByExampleSelective(depotHeadOrders, example);
        }catch(Exception e){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],寮傚父[{}]",
                    ExceptionConstants.DATA_WRITE_FAIL_CODE,ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
            throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                    ExceptionConstants.DATA_WRITE_FAIL_MSG);
        }
    }

    /**
     * 鏍规嵁鎵瑰彿鏌ヨ鍗曟嵁鏄庣粏淇℃伅
     * @param materialExtendId
     * @param batchNumber
     * @return
     */
    public DepotItem getDepotItemByBatchNumber(Long materialExtendId, String batchNumber) {
        List<DepotItem> depotItemList = depotItemMapperEx.getDepotItemByBatchNumber(materialExtendId, batchNumber);
        if(null != depotItemList && depotItemList.size() > 0){
            return depotItemList.get(0);
        } else {
            return new DepotItem();
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void deleteDepotItemHeadId(Long headerId)throws Exception {
        try{
            //1銆佹煡璇㈠垹闄ゅ墠鐨勫崟鎹槑缁?            List<DepotItem> depotItemList = getListByHeaderId(headerId);
            //2銆佸垹闄ゅ崟鎹槑缁?            DepotItemExample example = new DepotItemExample();
            example.createCriteria().andHeaderIdEqualTo(headerId);
            depotItemMapper.deleteByExample(example);
            //3銆佽绠楀垹闄や箣鍚庡崟鎹槑缁嗕腑鍟嗗搧鐨勫簱瀛?            for(DepotItem depotItem : depotItemList){
                updateCurrentStock(depotItem);
            }
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
    }

    /**
     * 鍒犻櫎搴忓垪鍙峰拰鍥炴敹搴忓垪鍙?     * @param actionType
     * @throws Exception
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void deleteOrCancelSerialNumber(String actionType, DepotHead depotHead, Long headerId) throws Exception {
        if(actionType.equals("update")) {
            User userInfo = userService.getCurrentUser();
            if(BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType())){
                //鍏ュ簱閫昏緫
                String number = depotHead.getNumber();
                SerialNumberExample example = new SerialNumberExample();
                example.createCriteria().andInBillNoEqualTo(number);
                serialNumberService.deleteByExample(example);
            } else if(BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())){
                //鍑哄簱閫昏緫
                DepotItemExample example = new DepotItemExample();
                example.createCriteria().andHeaderIdEqualTo(headerId).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                List<DepotItem> depotItemList = depotItemMapper.selectByExample(example);
                if(null != depotItemList && depotItemList.size() > 0){
                    for (DepotItem depotItem : depotItemList){
                        if(StringUtil.isNotEmpty(depotItem.getSnList())){
                            serialNumberService.cancelSerialNumber(depotItem.getMaterialId(), depotHead.getNumber(), (depotItem.getBasicNumber() == null ? 0 : depotItem.getBasicNumber()).intValue(), userInfo);
                        }
                    }
                }
            }
        }
    }

    /**
     * 閽堝缁勮鍗曘€佹媶鍗稿崟鏍￠獙鏄惁瀛樺湪缁勫悎浠跺拰鏅€氬瓙浠?     * @param rowArr
     * @param subType
     */
    public void checkAssembleWithMaterialType(JSONArray rowArr, String subType) {
        if(BusinessConstants.SUB_TYPE_ASSEMBLE.equals(subType) ||
                BusinessConstants.SUB_TYPE_DISASSEMBLE.equals(subType)) {
            if(rowArr.size() > 1) {
                JSONObject firstRowObj = JSONObject.parseObject(rowArr.getString(0));
                JSONObject secondRowObj = JSONObject.parseObject(rowArr.getString(1));
                String firstMaterialType = firstRowObj.getString("mType");
                String secondMaterialType = secondRowObj.getString("mType");
                if(!"缁勫悎浠?.equals(firstMaterialType) || !"鏅€氬瓙浠?.equals(secondMaterialType)) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_CHECK_ASSEMBLE_EMPTY_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_CHECK_ASSEMBLE_EMPTY_MSG));
                }
            } else {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_CHECK_ASSEMBLE_EMPTY_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_CHECK_ASSEMBLE_EMPTY_MSG));
            }
        }
    }

    /**
     * 鏍￠獙澶氳鏄庣粏褰撲腑鏄惁瀛樺湪閲嶅鐨勫簭鍒楀彿
     * @param rowArr
     */
    public void checkSerialNumberRepeat(JSONArray rowArr) {
        List<String> allSnArr = new ArrayList<>();
        for (int i = 0; i < rowArr.size(); i++) {
            JSONObject rowObj = JSONObject.parseObject(rowArr.getString(i));
            if(StringUtil.isNotEmpty(rowObj.getString("snList"))) {
                String snList = rowObj.getString("snList");
                snList = snList.replaceAll("锛?, ",");
                List<String> snArr = StringUtil.strToStringList(snList);
                if(snArr!=null && !snArr.isEmpty()) {
                    allSnArr.addAll(snArr);
                }
            }
        }
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (String str : allSnArr) {
            if (!seen.add(str)) {
                duplicates.add(str);
            }
        }
        if(!duplicates.isEmpty()) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_CHECK_SERIAL_NUMBER_REPEAT_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_CHECK_SERIAL_NUMBER_REPEAT_MSG, String.join(", ", duplicates)));
        }
    }

    /**
     * 鏇存柊鍟嗗搧鐨勪环鏍?     * @param meId
     * @param subType
     * @param rowObj
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateMaterialExtendPrice(Long meId, String subType, String billType, JSONObject rowObj) throws Exception {
        if(systemConfigService.getUpdateUnitPriceFlag()) {
            if (StringUtil.isExist(rowObj.get("unitPrice"))) {
                BigDecimal unitPrice = rowObj.getBigDecimal("unitPrice");
                MaterialExtend materialExtend = new MaterialExtend();
                materialExtend.setId(meId);
                if(BusinessConstants.SUB_TYPE_PURCHASE.equals(subType)) {
                    materialExtend.setPurchaseDecimal(unitPrice);
                }
                if(BusinessConstants.SUB_TYPE_SALES.equals(subType)) {
                    materialExtend.setWholesaleDecimal(unitPrice);
                }
                if(BusinessConstants.SUB_TYPE_RETAIL.equals(subType)) {
                    materialExtend.setCommodityDecimal(unitPrice);
                }
                //鍏跺畠鍏ュ簱-鐢熶骇鍏ュ簱鐨勬儏鍐垫洿鏂伴噰璐崟浠?                if(BusinessConstants.SUB_TYPE_OTHER.equals(subType)) {
                    if(BusinessConstants.BILL_TYPE_PRODUCE_IN.equals(billType)) {
                        materialExtend.setPurchaseDecimal(unitPrice);
                    }
                }
                materialExtendService.updateMaterialExtend(materialExtend);
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public List<DepotItemStockWarningCount> findStockWarningCount(Integer offset, Integer rows, String materialParam, Long depotId, List<Long> depotList, List<Long> categoryList) {
        List<DepotItemStockWarningCount> list = null;
        try{
            list =depotItemMapperEx.findStockWarningCount(offset, rows, materialParam, depotId, depotList, categoryList);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int findStockWarningCountTotal(String materialParam, List<Long> depotList, List<Long> categoryList) {
        int result = 0;
        try{
            result =depotItemMapperEx.findStockWarningCountTotal(materialParam, depotList, categoryList);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int saveAiPredictionBatch(List<AiPredictionSaveItem> payloadList) {
        if(payloadList == null || payloadList.isEmpty()) {
            throw new RuntimeException("AI棰勬祴鏁版嵁涓虹┖");
        }
        List<AiPredictionSaveItem> validList = new ArrayList<>();
        for (AiPredictionSaveItem item : payloadList) {
            if(item == null || item.getMaterialId() == null || item.getDepotId() == null) {
                continue;
            }
            validList.add(item);
        }
        if(validList.isEmpty()) {
            throw new RuntimeException("AI棰勬祴鏁版嵁缂哄皯materialId鎴杁epotId");
        }
        try {
            User userInfo = userService.getCurrentUser();
            Long tenantId = userInfo != null ? userInfo.getTenantId() : null;
            int affectedRows = depotItemMapperEx.batchUpsertAiPrediction(validList, tenantId);
            logger.info("saveAiPredictionBatch: payloadSize={}, validSize={}, tenantId={}, affectedRows={}",
                    payloadList.size(), validList.size(), tenantId, affectedRows);
            if(affectedRows <= 0) {
                throw new RuntimeException("AI棰勬祴鏁版嵁鏈啓鍏ユ暟鎹簱");
            }
            return affectedRows;
        } catch (Exception e) {
            JshException.writeFail(logger, e);
            return 0;
        }
    }

    /**
     * 搴撳瓨缁熻-sku
     * @param depotId
     * @param meId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getSkuStockByParam(Long depotId, Long meId, String beginTime, String endTime) throws Exception {
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
        List<Long> depotList = depotService.parseDepotList(depotId);
        //鐩樼偣澶嶇洏鍚庢暟閲忕殑鍙樺姩
        BigDecimal stockCheckSum = depotItemMapperEx.getSkuStockCheckSumByDepotList(depotList, meId, forceFlag, beginTime, endTime);
        DepotItemVo4Stock stockObj = depotItemMapperEx.getSkuStockByParamWithDepotList(depotList, meId, forceFlag, inOutManageFlag, beginTime, endTime);
        BigDecimal stockSum = BigDecimal.ZERO;
        if(stockObj!=null) {
            BigDecimal inTotal = stockObj.getInTotal();
            BigDecimal transfInTotal = stockObj.getTransfInTotal();
            BigDecimal assemInTotal = stockObj.getAssemInTotal();
            BigDecimal disAssemInTotal = stockObj.getDisAssemInTotal();
            BigDecimal outTotal = stockObj.getOutTotal();
            BigDecimal transfOutTotal = stockObj.getTransfOutTotal();
            BigDecimal assemOutTotal = stockObj.getAssemOutTotal();
            BigDecimal disAssemOutTotal = stockObj.getDisAssemOutTotal();
            stockSum = inTotal.add(transfInTotal).add(assemInTotal).add(disAssemInTotal)
                    .subtract(outTotal).subtract(transfOutTotal).subtract(assemOutTotal).subtract(disAssemOutTotal);
        }
        return stockCheckSum.add(stockSum);
    }

    /**
     * 搴撳瓨缁熻-鍗曚粨搴?     * @param depotId
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getStockByParam(Long depotId, Long mId, String beginTime, String endTime) throws Exception {
        List<Long> depotList = depotService.parseDepotList(depotId);
        return getStockByParamWithDepotList(depotList, mId, beginTime, endTime);
    }

    /**
     * 搴撳瓨缁熻-澶氫粨搴?     * @param depotList
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public BigDecimal getStockByParamWithDepotList(List<Long> depotList, Long mId, String beginTime, String endTime) throws Exception {
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
        //鍒濆搴撳瓨
        BigDecimal initStock = materialService.getInitStockByMidAndDepotList(depotList, mId);
        //鐩樼偣澶嶇洏鍚庢暟閲忕殑鍙樺姩
        BigDecimal stockCheckSum = depotItemMapperEx.getStockCheckSumByDepotList(depotList, mId, forceFlag, beginTime, endTime);
        DepotItemVo4Stock stockObj = depotItemMapperEx.getStockByParamWithDepotList(depotList, mId, forceFlag, inOutManageFlag, beginTime, endTime);
        BigDecimal stockSum = BigDecimal.ZERO;
        if(stockObj!=null) {
            BigDecimal inTotal = stockObj.getInTotal();
            BigDecimal transfInTotal = stockObj.getTransfInTotal();
            BigDecimal assemInTotal = stockObj.getAssemInTotal();
            BigDecimal disAssemInTotal = stockObj.getDisAssemInTotal();
            BigDecimal outTotal = stockObj.getOutTotal();
            BigDecimal transfOutTotal = stockObj.getTransfOutTotal();
            BigDecimal assemOutTotal = stockObj.getAssemOutTotal();
            BigDecimal disAssemOutTotal = stockObj.getDisAssemOutTotal();
            stockSum = inTotal.add(transfInTotal).add(assemInTotal).add(disAssemInTotal)
                    .subtract(outTotal).subtract(transfOutTotal).subtract(assemOutTotal).subtract(disAssemOutTotal);
        }
        return initStock.add(stockCheckSum).add(stockSum);
    }

    /**
     * 缁熻鏃堕棿娈靛唴鐨勫叆搴撳拰鍑哄簱鏁伴噺-澶氫粨搴?     * @param depotList
     * @param mId
     * @param beginTime
     * @param endTime
     * @return
     */
    public Map<String, BigDecimal> getIntervalMapByParamWithDepotList(List<Long> depotList, Long mId, String beginTime, String endTime) throws Exception {
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
        Map<String,BigDecimal> intervalMap = new HashMap<>();
        BigDecimal inSum = BigDecimal.ZERO;
        BigDecimal outSum = BigDecimal.ZERO;
        //鐩樼偣澶嶇洏鍚庢暟閲忕殑鍙樺姩
        BigDecimal stockCheckSum = depotItemMapperEx.getStockCheckSumByDepotList(depotList, mId, forceFlag, beginTime, endTime);
        DepotItemVo4Stock stockObj = depotItemMapperEx.getStockByParamWithDepotList(depotList, mId, forceFlag, inOutManageFlag, beginTime, endTime);
        if(stockObj!=null) {
            BigDecimal inTotal = stockObj.getInTotal();
            BigDecimal transfInTotal = stockObj.getTransfInTotal();
            BigDecimal assemInTotal = stockObj.getAssemInTotal();
            BigDecimal disAssemInTotal = stockObj.getDisAssemInTotal();
            inSum = inTotal.add(transfInTotal).add(assemInTotal).add(disAssemInTotal);
            BigDecimal outTotal = stockObj.getOutTotal();
            BigDecimal transfOutTotal = stockObj.getTransfOutTotal();
            BigDecimal assemOutTotal = stockObj.getAssemOutTotal();
            BigDecimal disAssemOutTotal = stockObj.getDisAssemOutTotal();
            outSum = outTotal.add(transfOutTotal).add(assemOutTotal).add(disAssemOutTotal);
        }
        if(stockCheckSum.compareTo(BigDecimal.ZERO)>0) {
            inSum = inSum.add(stockCheckSum);
        } else {
            //鐩樼偣澶嶇洏鏁伴噺涓鸿礋鏁颁唬琛ㄥ嚭搴?            outSum = outSum.subtract(stockCheckSum);
        }
        intervalMap.put("inSum", inSum);
        intervalMap.put("outSum", outSum);
        return intervalMap;
    }

    /**
     * 鏍规嵁鍗曟嵁鏄庣粏鏉ユ壒閲忔洿鏂板綋鍓嶅簱瀛?     * @param depotItem
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateCurrentStock(DepotItem depotItem) throws Exception {
        BigDecimal currentUnitPrice = materialCurrentStockMapperEx.getCurrentUnitPriceByMId(depotItem.getMaterialId());
        updateCurrentStockFun(depotItem.getMaterialId(), depotItem.getDepotId(), currentUnitPrice);
        if(depotItem.getAnotherDepotId()!=null){
            updateCurrentStockFun(depotItem.getMaterialId(), depotItem.getAnotherDepotId(), currentUnitPrice);
        }
    }

    /**
     * 鏍规嵁鍗曟嵁鏄庣粏鏉ユ壒閲忔洿鏂板綋鍓嶆垚鏈环
     * @param depotItem
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateCurrentUnitPrice(DepotItem depotItem) throws Exception {
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        //姝ゅ缁欏嚭鍏ュ簱绠＄悊鐨勪紶鍊奸粯璁や负false锛屼笉鐒朵細瀵艰嚧鏌ヨ涓嶅埌閿€鍞浉鍏崇殑鍗曟嵁
        Boolean inOutManageFlag = false;
        //鏌ヨ澶氬崟浣嶄俊鎭?        Unit unitInfo = materialService.findUnit(depotItem.getMaterialId());
        List<DepotItemVo4DetailByTypeAndMId> itemList = findDetailByDepotIdsAndMaterialIdList(null, forceFlag, inOutManageFlag, null,
                null, null, null, null, depotItem.getMaterialId(), null, null);
        Collections.reverse(itemList); //鍊掑簭涔嬪悗鍙樻垚鎸夋椂闂翠粠鍓嶅線鍚庢帓搴?        BigDecimal currentNumber = BigDecimal.ZERO;
        BigDecimal currentUnitPrice = BigDecimal.ZERO;
        BigDecimal currentAllPrice = BigDecimal.ZERO;
        for(DepotItemVo4DetailByTypeAndMId item: itemList) {
            BigDecimal basicNumber = item.getBnum()!=null?item.getBnum():BigDecimal.ZERO;
            //鏁伴噺*鍗曚环  鍙﹀璁＄畻鏂扮殑鎴愭湰浠?            BigDecimal allPrice = unitService.parseAllPriceByUnit(item.getAllPrice()!=null?item.getAllPrice():BigDecimal.ZERO, unitInfo, item.getMaterialUnit());
            if(basicNumber.compareTo(BigDecimal.ZERO)!=0 && allPrice.compareTo(BigDecimal.ZERO)!=0) {
                //鍏ュ簱
                if (BusinessConstants.DEPOTHEAD_TYPE_IN.equals(item.getType())) {
                    //闆跺敭閫€璐с€侀攢鍞€€璐?                    if (BusinessConstants.SUB_TYPE_RETAIL_RETURN.equals(item.getSubType()) || BusinessConstants.SUB_TYPE_SALES_RETURN.equals(item.getSubType())) {
                        //鏁伴噺*褰撳墠鐨勬垚鏈崟浠?                        currentNumber = currentNumber.add(basicNumber);
                        currentAllPrice = currentAllPrice.add(basicNumber.multiply(currentUnitPrice));
                    } else {
                        currentAllPrice = currentAllPrice.add(allPrice);
                        currentNumber = currentNumber.add(basicNumber);
                        //鍙湁褰撳墠搴撳瓨鎬婚噾棰濆拰褰撳墠搴撳瓨鏁伴噺閮藉ぇ浜?鎵嶈绠楃Щ鍔ㄥ钩鍧囦环
                        if (currentAllPrice.compareTo(BigDecimal.ZERO) > 0 && currentNumber.compareTo(BigDecimal.ZERO) > 0) {
                            currentUnitPrice = currentAllPrice.divide(currentNumber, 4, BigDecimal.ROUND_HALF_UP);
                        } else {
                            currentUnitPrice = item.getUnitPrice();
                        }
                    }
                }
                //鍑哄簱
                if (BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(item.getType())) {
                    //閲囪喘閫€璐?                    if (BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(item.getSubType())) {
                        currentAllPrice = currentAllPrice.add(allPrice);
                        currentNumber = currentNumber.add(basicNumber);
                        //鍙湁褰撳墠搴撳瓨鎬婚噾棰濆拰褰撳墠搴撳瓨鏁伴噺閮藉ぇ浜?鎵嶈绠楃Щ鍔ㄥ钩鍧囦环
                        if (currentAllPrice.compareTo(BigDecimal.ZERO) > 0 && currentNumber.compareTo(BigDecimal.ZERO) > 0) {
                            currentUnitPrice = currentAllPrice.divide(currentNumber, 4, BigDecimal.ROUND_HALF_UP);
                        } else {
                            currentUnitPrice = item.getUnitPrice();
                        }
                    } else {
                        //鏁伴噺*褰撳墠鐨勬垚鏈崟浠?                        currentNumber = currentNumber.add(basicNumber);
                        currentAllPrice = currentAllPrice.add(basicNumber.multiply(currentUnitPrice));
                    }
                }
                //鐗规畩鎯呭喌锛?-缁勮鍗?2-鎷嗗嵏鍗?3-鐩樼偣澶嶇洏
                if(BusinessConstants.SUB_TYPE_ASSEMBLE.equals(item.getSubType())||
                        BusinessConstants.SUB_TYPE_DISASSEMBLE.equals(item.getSubType())||
                        BusinessConstants.SUB_TYPE_REPLAY.equals(item.getSubType())) {
                    //鏁伴噺*褰撳墠鐨勬垚鏈崟浠?                    currentNumber = currentNumber.add(basicNumber);
                    currentAllPrice = currentAllPrice.add(basicNumber.multiply(currentUnitPrice));
                }
                //闃叉鍗曚环閲戦婧㈠嚭
                if(currentUnitPrice.compareTo(BigDecimal.valueOf(100000000))>0 || currentUnitPrice.compareTo(BigDecimal.valueOf(-100000000))<0) {
                    currentUnitPrice = BigDecimal.ZERO;
                }
            }
        }
        //鏇存柊瀹炴椂搴撳瓨涓殑褰撳墠鍗曚环
        materialCurrentStockMapperEx.updateUnitPriceByMId(currentUnitPrice, depotItem.getMaterialId());
    }

    /**
     * 鏍规嵁鍟嗗搧鍜屼粨搴撴潵鏇存柊褰撳墠搴撳瓨
     * @param mId
     * @param dId
     */
    public void updateCurrentStockFun(Long mId, Long dId, BigDecimal currentUnitPrice) throws Exception {
        if(mId!=null && dId!=null) {
            MaterialCurrentStockExample example = new MaterialCurrentStockExample();
            example.createCriteria().andMaterialIdEqualTo(mId).andDepotIdEqualTo(dId)
                    .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            List<MaterialCurrentStock> list = materialCurrentStockMapper.selectByExample(example);
            MaterialCurrentStock materialCurrentStock = new MaterialCurrentStock();
            materialCurrentStock.setMaterialId(mId);
            materialCurrentStock.setDepotId(dId);
            materialCurrentStock.setCurrentNumber(getStockByParam(dId,mId,null,null));
            materialCurrentStock.setCurrentUnitPrice(currentUnitPrice);
            if(list!=null && list.size()>0) {
                Long mcsId = list.get(0).getId();
                materialCurrentStock.setId(mcsId);
                materialCurrentStockMapper.updateByPrimaryKeySelective(materialCurrentStock);
            } else {
                materialCurrentStockMapper.insertSelective(materialCurrentStock);
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BigDecimal getFinishNumber(Long meId, Long id, Long headerId, Unit unitInfo, String materialUnit, String linkType) {
        Long linkId = id;
        String goToType = "";
        DepotHead depotHead =depotHeadMapper.selectByPrimaryKey(headerId);
        String linkStr = depotHead.getNumber(); //璁㈠崟鍙?        if("purchase".equals(linkType)) {
            //閽堝浠ラ攢瀹氳喘鐨勬儏鍐?            if(BusinessConstants.SUB_TYPE_SALES_ORDER.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_PURCHASE_ORDER;
            }
        } else if("other".equals(linkType)) {
            //閲囪喘鍏ュ簱銆侀噰璐€€璐с€侀攢鍞嚭搴撱€侀攢鍞€€璐ч兘杞叾瀹冨叆搴?            if(BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())
                    || BusinessConstants.SUB_TYPE_SALES_RETURN.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_OTHER;
            }
        } else if("basic".equals(linkType)) {
            //閲囪喘璁㈠崟杞噰璐叆搴?            if(BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_PURCHASE;
            }
            //閿€鍞鍗曡浆閿€鍞嚭搴?            if(BusinessConstants.SUB_TYPE_SALES_ORDER.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_SALES;
            }
            //閲囪喘鍏ュ簱杞噰璐€€璐?            if(BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_PURCHASE_RETURN;
            }
            //閿€鍞嚭搴撹浆閿€鍞€€璐?            if(BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType())) {
                goToType = BusinessConstants.SUB_TYPE_SALES_RETURN;
            }
        }
        String noType = "normal";
        if(BusinessConstants.SUB_TYPE_PURCHASE_APPLY.equals(depotHead.getSubType())) {
            noType = "apply";
        }
        BigDecimal count = depotItemMapperEx.getFinishNumber(meId, linkId, linkStr, noType, goToType);
        //鏍规嵁澶氬崟浣嶆儏鍐佃繘琛屾暟閲忕殑杞崲
        if(materialUnit.equals(unitInfo.getOtherUnit()) && unitInfo.getRatio()!=null && unitInfo.getRatio().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatio(),2,BigDecimal.ROUND_HALF_UP);
        }
        if(materialUnit.equals(unitInfo.getOtherUnitTwo()) && unitInfo.getRatioTwo()!=null && unitInfo.getRatioTwo().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatioTwo(),2,BigDecimal.ROUND_HALF_UP);
        }
        if(materialUnit.equals(unitInfo.getOtherUnitThree()) && unitInfo.getRatioThree()!=null && unitInfo.getRatioThree().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatioThree(),2,BigDecimal.ROUND_HALF_UP);
        }
        return count;
    }

    /**
     * 闄ゅ幓姝ゅ崟鎹箣澶栫殑宸插叆搴搢宸插嚭搴搢宸茶浆閲囪喘
     * @param currentSubType
     * @param meId
     * @param linkId
     * @param preHeaderId
     * @param currentHeaderId
     * @param unitInfo
     * @param materialUnit
     * @return
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BigDecimal getRealFinishNumber(String currentSubType, Long meId, Long linkId, Long preHeaderId, Long currentHeaderId, Unit unitInfo, String materialUnit) {
        String goToType = currentSubType;
        DepotHead depotHead =depotHeadMapper.selectByPrimaryKey(preHeaderId);
        String linkStr = depotHead.getNumber(); //璁㈠崟鍙?        String linkType = "normal";
        if(BusinessConstants.SUB_TYPE_PURCHASE_APPLY.equals(depotHead.getSubType())) {
            linkType = "apply";
        }
        BigDecimal count = depotItemMapperEx.getRealFinishNumber(meId, linkId, linkStr, linkType, currentHeaderId, goToType);
        //鏍规嵁澶氬崟浣嶆儏鍐佃繘琛屾暟閲忕殑杞崲
        if(materialUnit.equals(unitInfo.getOtherUnit()) && unitInfo.getRatio()!=null && unitInfo.getRatio().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatio(),2,BigDecimal.ROUND_HALF_UP);
        }
        if(materialUnit.equals(unitInfo.getOtherUnitTwo()) && unitInfo.getRatioTwo()!=null && unitInfo.getRatioTwo().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatioTwo(),2,BigDecimal.ROUND_HALF_UP);
        }
        if(materialUnit.equals(unitInfo.getOtherUnitThree()) && unitInfo.getRatioThree()!=null && unitInfo.getRatioThree().compareTo(BigDecimal.ZERO)!=0) {
            count = count.divide(unitInfo.getRatioThree(),2,BigDecimal.ROUND_HALF_UP);
        }
        return count;
    }

    public List<DepotItemVoBatchNumberList> getBatchNumberList(String number, String name, Long depotId, String barCode,
                                                               String batchNumber, Boolean forceFlag, Boolean inOutManageFlag) throws Exception {
        List<DepotItemVoBatchNumberList> reslist = new ArrayList<>();
        List<DepotItemVoBatchNumberList> list =  depotItemMapperEx.getBatchNumberList(StringUtil.toNull(number), name,
                depotId, barCode, batchNumber, forceFlag, inOutManageFlag);
        for(DepotItemVoBatchNumberList bn: list) {
            if(bn.getTotalNum()!=null && bn.getTotalNum().compareTo(BigDecimal.ZERO)>0) {
                bn.setExpirationDateStr(Tools.parseDateToStr(bn.getExpirationDate()));
                if(bn.getUnitId()!=null) {
                    Unit unit = unitService.getUnit(bn.getUnitId());
                    String commodityUnit = bn.getCommodityUnit();
                    bn.setTotalNum(unitService.parseStockByUnit(bn.getTotalNum(), unit, commodityUnit));
                }
                reslist.add(bn);
            }
        }
        return reslist;
    }

    /**
     * 鏌ヨ鏌愪釜鎵瑰彿鐨勫晢鍝佸簱瀛?     * @param depotId
     * @param barCode
     * @param batchNumber
     * @return
     * @throws Exception
     */
    public BigDecimal getOneBatchNumberStock(Long depotId, String barCode, String batchNumber) throws Exception {
        BigDecimal totalNum = BigDecimal.ZERO;
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        Boolean inOutManageFlag = systemConfigService.getInOutManageFlag();
        List<DepotItemVoBatchNumberList> list =  depotItemMapperEx.getBatchNumberList(null, null,
                depotId, barCode, batchNumber, forceFlag, inOutManageFlag);
        if(list!=null && list.size()>0) {
            DepotItemVoBatchNumberList bn = list.get(0);
            totalNum = bn.getTotalNum();
            if(bn.getTotalNum()!=null && bn.getTotalNum().compareTo(BigDecimal.ZERO)>0) {
                if(bn.getUnitId()!=null) {
                    Unit unit = unitService.getUnit(bn.getUnitId());
                    String commodityUnit = bn.getCommodityUnit();
                    totalNum = unitService.parseStockByUnit(bn.getTotalNum(), unit, commodityUnit);
                }
            }
        }
        return totalNum;
    }

    public Long getCountByMaterialAndDepot(Long mId, Long depotId) {
        return depotItemMapperEx.getCountByMaterialAndDepot(mId, depotId);
    }

    public JSONObject parseMapByExcelData(String barCodes, List<Map<String, String>> detailList, String prefixNo) throws Exception {
        JSONObject map = new JSONObject();
        JSONArray arr = new JSONArray();
        List<MaterialVo4Unit> list = depotItemMapperEx.getBillItemByParam(barCodes);
        Map<String, MaterialVo4Unit> materialMap = new HashMap<>();
        Map<String, Long> depotMap = new HashMap<>();
        for (MaterialVo4Unit material: list) {
            materialMap.put(material.getmBarCode(), material);
        }
        JSONArray depotArr = depotService.findDepotByCurrentUser();
        for (Object depotObj: depotArr) {
            if(depotObj!=null) {
                JSONObject depotObject = JSONObject.parseObject(depotObj.toString());
                depotMap.put(depotObject.getString("depotName"), depotObject.getLong("id"));
            }
        }
        for (Map<String, String> detailMap: detailList) {
            JSONObject item = new JSONObject();
            String barCode = detailMap.get("barCode");
            if(StringUtil.isNotEmpty(barCode)) {
                MaterialVo4Unit m = materialMap.get(barCode);
                if(m!=null) {
                    //鍒ゆ柇浠撳簱鏄惁瀛樺湪
                    String depotName = detailMap.get("depotName");
                    if(StringUtil.isNotEmpty(depotName)) {
                        if(depotMap.get(depotName)!=null) {
                            item.put("depotName", depotName);
                            item.put("depotId", depotMap.get(depotName));
                        } else {
                            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_DEPOTNAME_IS_NOT_EXIST_CODE,
                                    String.format(ExceptionConstants.DEPOT_ITEM_DEPOTNAME_IS_NOT_EXIST_MSG, depotName));
                        }
                    }
                    item.put("barCode", barCode);
                    item.put("name", m.getName());
                    item.put("standard", m.getStandard());
                    if(StringUtil.isNotEmpty(m.getModel())) {
                        item.put("model", m.getModel());
                    }
                    if(StringUtil.isNotEmpty(m.getColor())) {
                        item.put("color", m.getColor());
                    }
                    if(StringUtil.isNotEmpty(m.getSku())) {
                        item.put("sku", m.getSku());
                    }
                    BigDecimal stock = BigDecimal.ZERO;
                    if(StringUtil.isNotEmpty(m.getSku())){
                        stock = getSkuStockByParam(null, m.getMeId(),null,null);
                    } else {
                        stock = getCurrentStockByParam(null, m.getId());
                    }
                    item.put("stock", stock);
                    item.put("unit", m.getCommodityUnit());
                    BigDecimal operNumber = BigDecimal.ZERO;
                    BigDecimal unitPrice = BigDecimal.ZERO;
                    BigDecimal taxRate = BigDecimal.ZERO;
                    if(StringUtil.isNotEmpty(detailMap.get("num"))) {
                        operNumber = new BigDecimal(detailMap.get("num"));
                    }
                    if(StringUtil.isNotEmpty(detailMap.get("unitPrice"))) {
                        unitPrice = new BigDecimal(detailMap.get("unitPrice"));
                    } else {
                        if("CGDD".equals(prefixNo)) {
                            unitPrice = m.getPurchaseDecimal();
                        } else if("XSDD".equals(prefixNo)) {
                            unitPrice = m.getWholesaleDecimal();
                        }
                    }
                    if(StringUtil.isNotEmpty(detailMap.get("taxRate"))) {
                        taxRate = new BigDecimal(detailMap.get("taxRate"));
                    }
                    String remark = detailMap.get("remark");
                    item.put("operNumber", operNumber);
                    item.put("unitPrice", unitPrice);
                    BigDecimal allPrice = BigDecimal.ZERO;
                    if(unitPrice!=null && unitPrice.compareTo(BigDecimal.ZERO)!=0) {
                        allPrice = unitPrice.multiply(operNumber).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    BigDecimal taxMoney = BigDecimal.ZERO;
                    if(taxRate.compareTo(BigDecimal.ZERO) != 0) {
                        taxMoney = taxRate.multiply(allPrice).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                    }
                    BigDecimal taxLastMoney = allPrice.add(taxMoney);
                    item.put("allPrice", allPrice);
                    item.put("taxRate", taxRate);
                    item.put("taxMoney", taxMoney);
                    item.put("taxLastMoney", taxLastMoney);
                    item.put("remark", remark);
                    arr.add(item);
                } else {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_BARCODE_IS_NOT_EXIST_CODE,
                            String.format(ExceptionConstants.DEPOT_ITEM_BARCODE_IS_NOT_EXIST_MSG, barCode));
                }
            }
        }
        map.put("rows", arr);
        return map;
    }

    public BigDecimal getLastUnitPriceByParam(Long organId, Long meId, String prefixNo) {
        String type = "";
        String subType = "";
        if("XSDD".equals(prefixNo)) {
            type = "鍏跺畠";
            subType = "閿€鍞鍗?;
        } else if("XSCK".equals(prefixNo)) {
            type = "鍑哄簱";
            subType = "閿€鍞?;
        } else if("XSTH".equals(prefixNo)) {
            type = "鍏ュ簱";
            subType = "閿€鍞€€璐?;
        } else if("QTCK".equals(prefixNo)) {
            type = "鍑哄簱";
            subType = "鍏跺畠";
        }
        return depotItemMapperEx.getLastUnitPriceByParam(organId, meId, type, subType);
    }

    public BigDecimal getCurrentStockByParam(Long depotId, Long mId) {
        BigDecimal stock = depotItemMapperEx.getCurrentStockByParam(depotId, mId);
        return stock!=null? stock: BigDecimal.ZERO;
    }
}
