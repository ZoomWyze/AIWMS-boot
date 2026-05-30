package com.jsh.erp.service;


/**
 * 序列号 Service
 * 提供商品序列号（SN）的业务逻辑：新增/查询/启用/禁用
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SerialNumberService {
    private Logger logger = LoggerFactory.getLogger(SerialNumberService.class);

    @Resource
    private SerialNumberMapper serialNumberMapper;
    @Resource
    private SerialNumberMapperEx serialNumberMapperEx;
    @Resource
    private MaterialMapperEx materialMapperEx;
    @Resource
    private MaterialService materialService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    public SerialNumber getSerialNumber(long id)throws Exception {
        SerialNumber result=null;
        try{
            result=serialNumberMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<SerialNumber> getSerialNumberListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<SerialNumber> list = new ArrayList<>();
        try{
            SerialNumberExample example = new SerialNumberExample();
            example.createCriteria().andIdIn(idList);
            list = serialNumberMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<SerialNumber> getSerialNumber()throws Exception {
        SerialNumberExample example = new SerialNumberExample();
        List<SerialNumber> list=null;
        try{
            list=serialNumberMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<SerialNumberEx> select(String serialNumber, String materialName, Integer offset, Integer rows)throws Exception {
        return null;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertSerialNumber(JSONObject obj, HttpServletRequest request)throws Exception {
        return 0;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateSerialNumber(JSONObject obj, HttpServletRequest request) throws Exception{
        return 0;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSerialNumber(Long id, HttpServletRequest request)throws Exception {
        return 0;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSerialNumber(String ids, HttpServletRequest request)throws Exception {
        return 0;
    }

    public int checkIsNameExist(Long id, String serialNumber)throws Exception {
        return 0;
    }

    /**
     *  鏍规嵁鍟嗗搧鍚嶇О鍒ゆ柇缁欏晢鍝佹坊鍔犲簭鍒楀彿鏄惁鍙
     *  1銆佹牴鎹晢鍝佸悕绉板繀椤绘煡璇㈠埌鍞竴鐨勫晢鍝?     *  2銆佽鍟嗗搧蹇呴』宸茬粡鍚敤搴忓垪鍙?     *  3銆佽鍟嗗搧宸茬粦瀹氬簭鍒楀彿鏁伴噺灏忎簬鍟嗗搧鐜版湁搴撳瓨
     *  鐢ㄥ晢鍝佺殑搴撳瓨鍘婚檺鍒跺簭鍒楀彿鐨勬坊鍔犳湁鐐逛笉鍚堜箮閬撶悊锛屽幓鎺夋闄愬埗
     * @return Long 婊¤冻浣跨敤鏉′欢鐨勫晢鍝佺殑id
     */
    public Long getSerialNumberMaterialIdByBarCode(String materialCode)throws Exception{
        if(StringUtil.isNotEmpty(materialCode)){
            //璁＄畻鍟嗗搧搴撳瓨鍜岀洰鍓嶅崰鐢ㄧ殑鍙敤搴忓垪鍙锋暟閲忓叧绯?            //搴撳瓨=鍏ュ簱-鍑哄簱
            //鍏ュ簱鏁伴噺
            Long materialId = 0L;
            List<MaterialVo4Unit> list = materialService.getMaterialByBarCode(materialCode);
            if(list!=null && list.size()>0) {
                materialId = list.get(0).getId();
            }
            return materialId;
        }
        return null;
    }

    /**
     * 鍑哄簱鏃跺垽鏂簭鍒楀彿搴撳瓨鏄惁瓒冲锛?     * 鍚屾椂灏嗗搴旂殑搴忓垪鍙风粦瀹氬崟鎹?     */
    public void checkAndUpdateSerialNumber(DepotItem depotItem, String outBillNo,User userInfo, String snList) throws Exception{
        if(depotItem!=null){
            sellSerialNumber(depotItem.getMaterialId(), outBillNo, snList,userInfo);
        }
    }

    /**
     * 鍑哄敭搴忓垪鍙?     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void sellSerialNumber(Long materialId, String outBillNo, String snList, User user) throws Exception{
        //灏嗕腑鏂囩殑閫楀彿鎵归噺鏇挎崲涓鸿嫳鏂囬€楀彿
        snList = snList.replaceAll("锛?,",");
        String [] snArray=snList.split(",");
        for (String sn : snArray) {
            int isNotSellCount = serialNumberMapperEx.getIsNotSellCountByParam(materialId, sn);
            if (isNotSellCount == 0) {
                //濡傛灉搴忓垪鍙蜂笉瀛樺湪鎴栬€呭凡鍞嚭鍒欒繘琛屾彁绀猴紝涓嶅啀杩涜鍚庣画鐨勫嚭鍞搷浣?                throw new BusinessRunTimeException(ExceptionConstants.SERIAL_NUMBERE_NOT_EXISTS_CODE,
                        String.format(ExceptionConstants.SERIAL_NUMBERE_NOT_EXISTS_MSG, sn));
            }
        }
        serialNumberMapperEx.sellSerialNumber(materialId, outBillNo, snArray, new Date(), user == null ? null : user.getId());
    }

    /**
     * 璧庡洖搴忓垪鍙?     * @Param: materialId
     * @Param: depotheadId
     * @Param: isSell 璧庡洖'0'
     * @Param: Count 鍗栧嚭鎴栬€呰祹鍥炵殑鏁伴噺
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int cancelSerialNumber(Long materialId, String outBillNo,int count,User user) throws Exception{
        int result=0;
        try{
            result = serialNumberMapperEx.cancelSerialNumber(materialId,outBillNo,count,new Date(),user==null?null:user.getId());
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    /**
     * 鎵归噺娣诲姞搴忓垪鍙凤紝鏈€澶?00涓?     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batAddSerialNumber(String materialCode, String serialNumberPrefix, Integer batAddTotal, String remark)throws Exception {
        int result=0;
        try {
            if (StringUtil.isNotEmpty(materialCode)) {
                //鏌ヨ鍟嗗搧id
                Long materialId = getSerialNumberMaterialIdByBarCode(materialCode);
                List<SerialNumberEx> list = null;
                //褰撳墠鐢ㄦ埛
                User userInfo = userService.getCurrentUser();
                Long userId = userInfo == null ? null : userInfo.getId();
                Date date = null;
                Long million = null;
                synchronized (this) {
                    date = new Date();
                    million = date.getTime();
                }
                int insertNum = 0;
                StringBuffer prefixBuf = new StringBuffer(serialNumberPrefix).append(million);
                list = new ArrayList<SerialNumberEx>();
                int forNum = BusinessConstants.BATCH_INSERT_MAX_NUMBER >= batAddTotal ? batAddTotal : BusinessConstants.BATCH_INSERT_MAX_NUMBER;
                for (int i = 0; i < forNum; i++) {
                    insertNum++;
                    SerialNumberEx each = new SerialNumberEx();
                    each.setMaterialId(materialId);
                    each.setCreator(userId);
                    each.setCreateTime(date);
                    each.setUpdater(userId);
                    each.setUpdateTime(date);
                    each.setRemark(remark);
                    each.setSerialNumber(new StringBuffer(prefixBuf.toString()).append(insertNum).toString());
                    list.add(each);
                }
                result = serialNumberMapperEx.batAddSerialNumber(list);
                logService.insertLog("搴忓垪鍙?,
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_BATCH_ADD).append(batAddTotal).append(BusinessConstants.LOG_DATA_UNIT).toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<SerialNumberEx> getEnableSerialNumberList(String number, String name, String [] nameArray, Long depotId, String barCode, Integer offset, Integer rows)throws Exception {
        List<SerialNumberEx> list =null;
        try{
            list = serialNumberMapperEx.getEnableSerialNumberList(StringUtil.toNull(number), StringUtil.toNull(name), nameArray, depotId, barCode, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Long getEnableSerialNumberCount(String number, String name, String [] nameArray, Long depotId, String barCode)throws Exception {
        Long count = 0L;
        try{
            count = serialNumberMapperEx.getEnableSerialNumberCount(StringUtil.toNull(number), StringUtil.toNull(name), nameArray, depotId, barCode);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return count;
    }

    public void addSerialNumberByBill(String type, String subType, String inBillNo, Long materialId, Long depotId, BigDecimal inPrice, String snList) throws Exception {
        //褰曞叆搴忓垪鍙风殑鏃跺€欎笉鑳藉拰搴撻噷闈㈢殑閲嶅-鍏ュ簱
        if ((BusinessConstants.SUB_TYPE_PURCHASE.equals(subType) ||
                BusinessConstants.SUB_TYPE_OTHER.equals(subType) ||
                BusinessConstants.SUB_TYPE_SALES_RETURN.equals(subType)||
                BusinessConstants.SUB_TYPE_RETAIL_RETURN.equals(subType)) &&
                BusinessConstants.DEPOTHEAD_TYPE_IN.equals(type)) {
            //灏嗕腑鏂囩殑閫楀彿鎵归噺鏇挎崲涓鸿嫳鏂囬€楀彿
            snList = snList.replaceAll("锛?, ",");
            List<String> snArr = StringUtil.strToStringList(snList);
            for (String sn : snArr) {
                List<SerialNumber> list = new ArrayList<>();
                SerialNumberExample example = new SerialNumberExample();
                example.createCriteria().andMaterialIdEqualTo(materialId).andSerialNumberEqualTo(sn.trim()).andIsSellEqualTo("0")
                        .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                list = serialNumberMapper.selectByExample(example);
                //鍒ゆ柇濡傛灉涓嶅瓨鍦ㄩ噸澶嶅簭鍒楀彿灏辨柊澧?                if (list == null || list.size() == 0) {
                    SerialNumber serialNumber = new SerialNumber();
                    serialNumber.setMaterialId(materialId);
                    serialNumber.setDepotId(depotId);
                    serialNumber.setSerialNumber(sn);
                    serialNumber.setInPrice(inPrice);
                    Date date = new Date();
                    serialNumber.setCreateTime(date);
                    serialNumber.setUpdateTime(date);
                    User userInfo = userService.getCurrentUser();
                    serialNumber.setCreator(userInfo == null ? null : userInfo.getId());
                    serialNumber.setUpdater(userInfo == null ? null : userInfo.getId());
                    serialNumber.setInBillNo(inBillNo);
                    serialNumberMapper.insertSelective(serialNumber);
                } else {
                    if(!inBillNo.equals(list.get(0).getInBillNo())) {
                        throw new BusinessRunTimeException(ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_CODE,
                                String.format(ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_MSG, sn));
                    }
                }
            }
        }
    }

    /**
     * 鐩存帴鍒犻櫎搴忓垪鍙?     * @param example
     */
    public void deleteByExample(SerialNumberExample example) {
        serialNumberMapper.deleteByExample(example);
    }
}
