package com.jsh.erp.service;


/**
 * 单据主表 Service
 * 提供单据主表的核心业务逻辑：新增/编辑/删除/审核/反审核/查询/Excel导出
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.DepotHeadMapper;
import com.jsh.erp.datasource.mappers.DepotHeadMapperEx;
import com.jsh.erp.datasource.mappers.DepotItemMapperEx;
import com.jsh.erp.datasource.vo.*;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.ExcelUtils;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import com.jsh.erp.utils.Tools;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.jsh.erp.utils.Tools.getCenternTime;
import static com.jsh.erp.utils.Tools.getNow3;

@Service
public class DepotHeadService {
    private Logger logger = LoggerFactory.getLogger(DepotHeadService.class);

    @Resource
    private DepotHeadMapper depotHeadMapper;
    @Resource
    private DepotHeadMapperEx depotHeadMapperEx;
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private DepotService depotService;
    @Resource
    DepotItemService depotItemService;
    @Resource
    private SupplierService supplierService;
    @Resource
    private UserBusinessService userBusinessService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private SerialNumberService serialNumberService;
    @Resource
    private OrgaUserRelService orgaUserRelService;
    @Resource
    private PersonService personService;
    @Resource
    private AccountService accountService;
    @Resource
    private AccountHeadService accountHeadService;
    @Resource
    private AccountItemService accountItemService;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private RedisService redisService;
    @Resource
    DepotItemMapperEx depotItemMapperEx;
    @Resource
    private LogService logService;

    public DepotHead getDepotHead(long id)throws Exception {
        DepotHead result=null;
        try{
            result=depotHeadMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<DepotHead> getDepotHead()throws Exception {
        DepotHeadExample example = new DepotHeadExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotHead> list=null;
        try{
            list=depotHeadMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<DepotHeadVo4List> select(String type, String subType, String hasDebt, String status, String purchaseStatus, String number, String linkApply, String linkNumber,
           String beginTime, String endTime, String materialParam, Long organId, Long creator, Long depotId, Long accountId, String salesMan, String remark) throws Exception {
        List<DepotHeadVo4List> list = new ArrayList<>();
        try{
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Long userId = userService.getUserId(request);
            String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            String billCategory = getBillCategory(subType);
            String [] depotArray = getDepotArray(subType);
            String [] creatorArray = getCreatorArray();
            String [] statusArray = StringUtil.isNotEmpty(status) ? status.split(",") : null;
            String [] purchaseStatusArray = StringUtil.isNotEmpty(purchaseStatus) ? purchaseStatus.split(",") : null;
            String [] organArray = getOrganArray(subType, purchaseStatus);
            //浠ラ攢瀹氳喘锛屾煡鐪嬪叏閮ㄦ暟鎹?            creatorArray = StringUtil.isNotEmpty(purchaseStatus) ? null: creatorArray;
            Map<Long,String> personMap = personService.getPersonMap();
            Map<Long,String> accountMap = accountService.getAccountMap();
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            PageUtils.startPage();
            list = depotHeadMapperEx.selectByConditionDepotHead(type, subType, creatorArray, hasDebt,
                    statusArray, purchaseStatusArray, number, linkApply, linkNumber, beginTime, endTime,
                    materialParam, organId, organArray, creator, depotId, depotArray, accountId, salesMan, remark);
            if (null != list) {
                List<Long> idList = new ArrayList<>();
                List<String> numberList = new ArrayList<>();
                for (DepotHeadVo4List dh : list) {
                    idList.add(dh.getId());
                    numberList.add(dh.getNumber());
                }
                //閫氳繃鎵归噺鏌ヨ鍘绘瀯閫爉ap
                Map<String,BigDecimal> finishDepositMap = getFinishDepositMapByNumberList(numberList);
                Map<Long,Integer> financialBillNoMap = getFinancialBillNoMapByBillIdList(idList);
                Map<String,Integer> billSizeMap = getBillSizeMapByLinkNumberList(numberList);
                Map<Long,String> materialsListMap = findMaterialsListMapByHeaderIdList(idList);
                Map<Long,BigDecimal> materialCountListMap = getMaterialCountListMapByHeaderIdList(idList);
                for (DepotHeadVo4List dh : list) {
                    if(accountMap!=null && StringUtil.isNotEmpty(dh.getAccountIdList()) && StringUtil.isNotEmpty(dh.getAccountMoneyList())) {
                        String accountStr = accountService.getAccountStrByIdAndMoney(accountMap, dh.getAccountIdList(), dh.getAccountMoneyList());
                        dh.setAccountName(accountStr);
                    }
                    if(dh.getAccountIdList() != null) {
                        String accountidlistStr = dh.getAccountIdList().replace("[", "").replace("]", "").replaceAll("\"", "");
                        dh.setAccountIdList(accountidlistStr);
                    }
                    if(dh.getAccountMoneyList() != null) {
                        String accountmoneylistStr = dh.getAccountMoneyList().replace("[", "").replace("]", "").replaceAll("\"", "");
                        dh.setAccountMoneyList(accountmoneylistStr);
                    }
                    if(dh.getChangeAmount() != null) {
                        dh.setChangeAmount(roleService.parseBillPriceByLimit(dh.getChangeAmount().abs(), billCategory, priceLimit, request));
                    } else {
                        dh.setChangeAmount(BigDecimal.ZERO);
                    }
                    if(dh.getTotalPrice() != null) {
                        BigDecimal lastTotalPrice = BusinessConstants.SUB_TYPE_CHECK_ENTER.equals(dh.getSubType())||
                                BusinessConstants.SUB_TYPE_REPLAY.equals(dh.getSubType())?dh.getTotalPrice():dh.getTotalPrice().abs();
                        dh.setTotalPrice(roleService.parseBillPriceByLimit(lastTotalPrice, billCategory, priceLimit, request));
                    }
                    BigDecimal discountLastMoney = dh.getDiscountLastMoney()!=null?dh.getDiscountLastMoney():BigDecimal.ZERO;
                    dh.setDiscountLastMoney(roleService.parseBillPriceByLimit(discountLastMoney, billCategory, priceLimit, request));
                    BigDecimal backAmount = dh.getBackAmount()!=null?dh.getBackAmount():BigDecimal.ZERO;
                    dh.setBackAmount(roleService.parseBillPriceByLimit(backAmount, billCategory, priceLimit, request));
                    if(dh.getDeposit() == null) {
                        dh.setDeposit(BigDecimal.ZERO);
                    } else {
                        dh.setDeposit(roleService.parseBillPriceByLimit(dh.getDeposit(), billCategory, priceLimit, request));
                    }
                    //宸茬粡瀹屾垚鐨勬瑺娆?                    if(finishDepositMap!=null) {
                        BigDecimal finishDeposit = finishDepositMap.get(dh.getNumber()) != null ? finishDepositMap.get(dh.getNumber()) : BigDecimal.ZERO;
                        dh.setFinishDeposit(roleService.parseBillPriceByLimit(finishDeposit, billCategory, priceLimit, request));
                    }
                    //娆犳璁＄畻
                    BigDecimal otherMoney = dh.getOtherMoney()!=null?dh.getOtherMoney():BigDecimal.ZERO;
                    BigDecimal deposit = dh.getDeposit()!=null?dh.getDeposit():BigDecimal.ZERO;
                    BigDecimal changeAmount = dh.getChangeAmount()!=null?dh.getChangeAmount():BigDecimal.ZERO;
                    BigDecimal debt = discountLastMoney.add(otherMoney).subtract((deposit.add(changeAmount)));
                    dh.setDebt(roleService.parseBillPriceByLimit(debt, billCategory, priceLimit, request));
                    //鏄惁鏈変粯娆惧崟鎴栨敹娆惧崟
                    if(financialBillNoMap!=null) {
                        Integer financialBillNoSize = financialBillNoMap.get(dh.getId());
                        dh.setHasFinancialFlag(financialBillNoSize!=null && financialBillNoSize>0);
                    }
                    //鏄惁鏈夐€€娆惧崟
                    if(billSizeMap!=null) {
                        Integer billListSize = billSizeMap.get(dh.getNumber());
                        dh.setHasBackFlag(billListSize!=null && billListSize>0);
                    }
                    if(StringUtil.isNotEmpty(dh.getSalesMan())) {
                        dh.setSalesManStr(personService.getPersonByMapAndIds(personMap,dh.getSalesMan()));
                    }
                    if(dh.getOperTime() != null) {
                        dh.setOperTimeStr(getCenternTime(dh.getOperTime()));
                    }
                    //鍟嗗搧淇℃伅绠€杩?                    if(materialsListMap!=null) {
                        dh.setMaterialsList(materialsListMap.get(dh.getId()));
                    }
                    //鍟嗗搧鎬绘暟閲?                    if(materialCountListMap!=null) {
                        dh.setMaterialCount(materialCountListMap.get(dh.getId()));
                    }
                    //浠ラ攢瀹氳喘鐨勬儏鍐碉紙涓嶈兘鏄剧ず閿€鍞崟鎹殑閲戦鍜屽鎴峰悕绉帮級
                    if(StringUtil.isNotEmpty(purchaseStatus)) {
                        dh.setOrganName("****");
                        dh.setTotalPrice(null);
                        dh.setDiscountLastMoney(null);
                    }
                }
            }
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 鏍规嵁鍗曟嵁绫诲瀷鑾峰彇浠撳簱鏁扮粍
     * @param subType
     * @return
     * @throws Exception
     */
    public String[] getDepotArray(String subType) throws Exception {
        String [] depotArray = null;
        if(!BusinessConstants.SUB_TYPE_PURCHASE_APPLY.equals(subType)
                && !BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(subType)
                && !BusinessConstants.SUB_TYPE_SALES_ORDER.equals(subType)) {
            String depotIds = depotService.findDepotStrByCurrentUser();
            depotArray = StringUtil.isNotEmpty(depotIds) ? depotIds.split(",") : null;
        }
        return depotArray;
    }

    /**
     * 鏍规嵁瑙掕壊绫诲瀷鑾峰彇鎿嶄綔鍛樻暟缁?     * @return
     * @throws Exception
     */
    public String[] getCreatorArray() throws Exception {
        String creator = getCreatorByCurrentUser();
        String [] creatorArray=null;
        if(StringUtil.isNotEmpty(creator)){
            creatorArray = creator.split(",");
        }
        return creatorArray;
    }

    /**
     * 鏍规嵁瑙掕壊绫诲瀷鑾峰彇鎿嶄綔鍛樻暟缁?     * @param organizationId
     * @return
     * @throws Exception
     */
    public String[] getCreatorArrayByOrg(Long organizationId) throws Exception {
        List<Long> userIdList = orgaUserRelService.getUserIdListByOrgId(organizationId);
        if(userIdList.size()>0) {
            List<String> userIdStrList = userIdList.stream().map(Object::toString).collect(Collectors.toList());
            return StringUtil.listToStringArray(userIdStrList);
        } else {
            return "-1".split(",");
        }
    }

    /**
     * 鑾峰彇鏈烘瀯鏁扮粍
     * @return
     */
    public String[] getOrganArray(String subType, String purchaseStatus) throws Exception {
        String [] organArray = null;
        String type = "UserCustomer";
        Long userId = userService.getCurrentUser().getId();
        //鑾峰彇鏉冮檺淇℃伅
        String ubValue = userBusinessService.getUBValueByTypeAndKeyId(type, userId.toString());
        List<SupplierSimple> supplierList = supplierService.getAllCustomer();
        if(BusinessConstants.SUB_TYPE_SALES_ORDER.equals(subType) || BusinessConstants.SUB_TYPE_SALES.equals(subType)
                ||BusinessConstants.SUB_TYPE_SALES_RETURN.equals(subType) ) {
            //閲囪喘璁㈠崟閲岄潰閫夋嫨閿€鍞鍗曠殑鏃跺€欎笉瑕佽繃婊?            if(StringUtil.isEmpty(purchaseStatus)) {
                if (null != supplierList && supplierList.size() > 0) {
                    boolean customerFlag = systemConfigService.getCustomerFlag();
                    List<String> organList = new ArrayList<>();
                    for (SupplierSimple supplier : supplierList) {
                        boolean flag = ubValue.contains("[" + supplier.getId().toString() + "]");
                        if (!customerFlag || flag) {
                            organList.add(supplier.getId().toString());
                        }
                    }
                    if(organList.size() > 0) {
                        organArray = StringUtil.listToStringArray(organList);
                    }
                }
            }
        }
        return organArray;
    }

    /**
     * 鏍规嵁瑙掕壊绫诲瀷鑾峰彇鎿嶄綔鍛?     * @return
     * @throws Exception
     */
    public String getCreatorByCurrentUser() throws Exception {
        String creator = "";
        User user = userService.getCurrentUser();
        String roleType = userService.getRoleTypeByUserId(user.getId()).getType(); //瑙掕壊绫诲瀷
        if(BusinessConstants.ROLE_TYPE_PRIVATE.equals(roleType)) {
            creator = user.getId().toString();
        } else if(BusinessConstants.ROLE_TYPE_THIS_ORG.equals(roleType)) {
            creator = orgaUserRelService.getUserIdListByUserId(user.getId());
        }
        return creator;
    }

    public Map<String, BigDecimal> getFinishDepositMapByNumberList(List<String> numberList) {
        Map<String,BigDecimal> finishDepositMap = new HashMap<>();
        if(numberList.size()>0) {
            List<FinishDepositVo> list = depotHeadMapperEx.getFinishDepositByNumberList(numberList);
            if(list!=null && list.size()>0) {
                for (FinishDepositVo finishDepositVo : list) {
                    if(finishDepositVo!=null) {
                        finishDepositMap.put(finishDepositVo.getNumber(), finishDepositVo.getFinishDeposit());
                    }
                }
            }
        }
        return finishDepositMap;
    }

    public Map<String, Integer> getBillSizeMapByLinkNumberList(List<String> numberList) throws Exception {
        Map<String, Integer> billListMap = new HashMap<>();
        if(numberList.size()>0) {
            List<DepotHead> list = getBillListByLinkNumberList(numberList);
            if(list!=null && list.size()>0) {
                for (DepotHead depotHead : list) {
                    if(depotHead!=null) {
                        billListMap.put(depotHead.getLinkNumber(), list.size());
                    }
                }
            }
        }
        return billListMap;
    }

    public Map<Long,Integer> getFinancialBillNoMapByBillIdList(List<Long> idList) {
        Map<Long, Integer> billListMap = new HashMap<>();
        if(idList.size()>0) {
            List<AccountItem> list = accountHeadService.getFinancialBillNoByBillIdList(idList);
            if(list!=null && list.size()>0) {
                for (AccountItem accountItem : list) {
                    if(accountItem!=null) {
                        billListMap.put(accountItem.getBillId(), list.size());
                    }
                }
            }
        }
        return billListMap;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertDepotHead(JSONObject obj, HttpServletRequest request)throws Exception {
        DepotHead depotHead = JSONObject.parseObject(obj.toJSONString(), DepotHead.class);
        depotHead.setCreateTime(new Timestamp(System.currentTimeMillis()));
        depotHead.setStatus(BusinessConstants.BILLS_STATUS_UN_AUDIT);
        int result=0;
        try{
            result=depotHeadMapper.insert(depotHead);
            logService.insertLog("鍗曟嵁", BusinessConstants.LOG_OPERATION_TYPE_ADD, request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateDepotHead(JSONObject obj, HttpServletRequest request) throws Exception{
        DepotHead depotHead = JSONObject.parseObject(obj.toJSONString(), DepotHead.class);
        DepotHead dh=null;
        try{
            dh = depotHeadMapper.selectByPrimaryKey(depotHead.getId());
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        depotHead.setStatus(dh.getStatus());
        depotHead.setCreateTime(dh.getCreateTime());
        int result=0;
        try{
            result = depotHeadMapper.updateByPrimaryKey(depotHead);
            logService.insertLog("鍗曟嵁",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(depotHead.getId()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteDepotHead(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteBillByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotHead(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteBillByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteBillByIds(String ids)throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<DepotHead> dhList = getDepotHeadListByIds(ids);
        for(DepotHead depotHead: dhList){
            //鍙湁鏈鏍哥殑鍗曟嵁鎵嶈兘琚垹闄?            if(!"0".equals(depotHead.getStatus())) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_UN_AUDIT_DELETE_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_UN_AUDIT_DELETE_FAILED_MSG));
            }
        }
        for(DepotHead depotHead: dhList){
            sb.append("[").append(depotHead.getNumber()).append("]");
            User userInfo = userService.getCurrentUser();
            //鍒犻櫎鍏ュ簱鍗曟嵁锛屽厛鏍￠獙搴忓垪鍙锋槸鍚﹀嚭搴擄紝濡傛灉鏈嚭搴撳垯鍚屾椂鍒犻櫎搴忓垪鍙凤紝濡傛灉宸插嚭搴撳垯涓嶈兘鍒犻櫎鍗曟嵁
            if (BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType())) {
                List<DepotItem> depotItemList = depotItemMapperEx.findDepotItemListBydepotheadId(depotHead.getId(), BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED);
                if (depotItemList != null && depotItemList.size() > 0) {
                    //鍗曟嵁鏄庣粏閲岄潰瀛樺湪搴忓垪鍙峰晢鍝?                    int serialNumberSellCount = depotHeadMapperEx.getSerialNumberBySell(depotHead.getNumber());
                    if (serialNumberSellCount > 0) {
                        //宸插嚭搴撳垯涓嶈兘鍒犻櫎鍗曟嵁
                        throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_SERIAL_IS_SELL_CODE,
                                String.format(ExceptionConstants.DEPOT_HEAD_SERIAL_IS_SELL_MSG, depotHead.getNumber()));
                    } else {
                        //鍒犻櫎搴忓垪鍙?                        SerialNumberExample example = new SerialNumberExample();
                        example.createCriteria().andInBillNoEqualTo(depotHead.getNumber());
                        serialNumberService.deleteByExample(example);
                    }
                }
            }
            //鍒犻櫎鍑哄簱鏁版嵁鍥炴敹搴忓垪鍙?            if (BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())
                    && !BusinessConstants.SUB_TYPE_TRANSFER.equals(depotHead.getSubType())) {
                //鏌ヨ鍗曟嵁瀛愯〃鍒楄〃
                List<DepotItem> depotItemList = depotItemMapperEx.findDepotItemListBydepotheadId(depotHead.getId(), BusinessConstants.ENABLE_SERIAL_NUMBER_ENABLED);
                /**鍥炴敹搴忓垪鍙?/
                if (depotItemList != null && depotItemList.size() > 0) {
                    for (DepotItem depotItem : depotItemList) {
                        //BasicNumber=OperNumber*ratio
                        serialNumberService.cancelSerialNumber(depotItem.getMaterialId(), depotHead.getNumber(), (depotItem.getBasicNumber() == null ? 0 : depotItem.getBasicNumber()).intValue(), userInfo);
                    }
                }
            }
            List<DepotItem> list = depotItemService.getListByHeaderId(depotHead.getId());
            //鍒犻櫎鍗曟嵁瀛愯〃鏁版嵁
            depotItemMapperEx.batchDeleteDepotItemByDepotHeadIds(new Long[]{depotHead.getId()});
            //鍒犻櫎鍗曟嵁涓昏〃淇℃伅
            batchDeleteDepotHeadByIds(depotHead.getId().toString());
            //灏嗗叧鑱旂殑鍗曟嵁缃负瀹℃牳鐘舵€?閽堝閲囪喘鍏ュ簱銆侀攢鍞嚭搴撱€佺洏鐐瑰鐩樸€佸叾瀹冨叆搴撱€佸叾瀹冨嚭搴?            if(StringUtil.isNotEmpty(depotHead.getLinkNumber())){
                if((BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_PURCHASE.equals(depotHead.getSubType()))
                        || (BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_SALES.equals(depotHead.getSubType()))
                        || (BusinessConstants.DEPOTHEAD_TYPE_OTHER.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_REPLAY.equals(depotHead.getSubType()))
                        || (BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_OTHER.equals(depotHead.getSubType()))
                        || (BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_OTHER.equals(depotHead.getSubType()))) {
                    String status = BusinessConstants.BILLS_STATUS_AUDIT;
                    //鏌ヨ闄ゅ綋鍓嶅崟鎹箣澶栫殑鍏宠仈鍗曟嵁鍒楄〃
                    List<DepotHead> exceptCurrentList = getListByLinkNumberExceptCurrent(depotHead.getLinkNumber(), depotHead.getNumber(), depotHead.getType());
                    if(exceptCurrentList!=null && exceptCurrentList.size()>0) {
                        status = BusinessConstants.BILLS_STATUS_SKIPING;
                    }
                    DepotHead dh = new DepotHead();
                    dh.setStatus(status);
                    DepotHeadExample example = new DepotHeadExample();
                    example.createCriteria().andNumberEqualTo(depotHead.getLinkNumber());
                    depotHeadMapper.updateByExampleSelective(dh, example);
                }
            }
            //灏嗗叧鑱旂殑鍗曟嵁缃负瀹℃牳鐘舵€?閽堝璇疯喘鍗曡浆閲囪喘璁㈠崟鐨勬儏鍐?            if(StringUtil.isNotEmpty(depotHead.getLinkApply())){
                if(BusinessConstants.DEPOTHEAD_TYPE_OTHER.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(depotHead.getSubType())) {
                    String status = BusinessConstants.BILLS_STATUS_AUDIT;
                    //鏌ヨ闄ゅ綋鍓嶅崟鎹箣澶栫殑鍏宠仈鍗曟嵁鍒楄〃
                    List<DepotHead> exceptCurrentList = getListByLinkApplyExceptCurrent(depotHead.getLinkApply(), depotHead.getNumber(), depotHead.getType());
                    if(exceptCurrentList!=null && exceptCurrentList.size()>0) {
                        status = BusinessConstants.BILLS_STATUS_SKIPING;
                    }
                    DepotHead dh = new DepotHead();
                    dh.setStatus(status);
                    DepotHeadExample example = new DepotHeadExample();
                    example.createCriteria().andNumberEqualTo(depotHead.getLinkApply());
                    depotHeadMapper.updateByExampleSelective(dh, example);
                }
            }
            //灏嗗叧鑱旂殑閿€鍞鍗曞崟鎹疆涓烘湭閲囪喘鐘舵€?閽堝閿€鍞鍗曡浆閲囪喘璁㈠崟鐨勬儏鍐?            if(StringUtil.isNotEmpty(depotHead.getLinkNumber())){
                if(BusinessConstants.DEPOTHEAD_TYPE_OTHER.equals(depotHead.getType()) &&
                        BusinessConstants.SUB_TYPE_PURCHASE_ORDER.equals(depotHead.getSubType())) {
                    DepotHead dh = new DepotHead();
                    //鑾峰彇鍒嗘壒鎿嶄綔鍚庡崟鎹殑鍟嗗搧鍜屽晢鍝佹暟閲忥紙姹囨€伙級
                    List<DepotItemVo4MaterialAndSum> batchList = depotItemMapperEx.getBatchBillDetailMaterialSum(depotHead.getLinkNumber(), "normal", depotHead.getType());
                    if(batchList.size()>0) {
                        dh.setPurchaseStatus(BusinessConstants.PURCHASE_STATUS_SKIPING);
                    } else {
                        dh.setPurchaseStatus(BusinessConstants.PURCHASE_STATUS_UN_AUDIT);
                    }
                    DepotHeadExample example = new DepotHeadExample();
                    example.createCriteria().andNumberEqualTo(depotHead.getLinkNumber());
                    depotHeadMapper.updateByExampleSelective(dh, example);
                }
            }
            //瀵逛簬闆跺敭鍑哄簱鍗曟嵁锛屾洿鏂颁細鍛樼殑棰勬敹娆句俊鎭?            if (BusinessConstants.DEPOTHEAD_TYPE_OUT.equals(depotHead.getType())
                    && BusinessConstants.SUB_TYPE_RETAIL.equals(depotHead.getSubType())){
                if(BusinessConstants.PAY_TYPE_PREPAID.equals(depotHead.getPayType())) {
                    if (depotHead.getOrganId() != null) {
                        //鏇存柊浼氬憳棰勪粯娆?                        supplierService.updateAdvanceIn(depotHead.getOrganId());
                    }
                }
            }
            for (DepotItem depotItem : list) {
                //鏇存柊褰撳墠搴撳瓨
                depotItemService.updateCurrentStock(depotItem);
                //鏇存柊褰撳墠鎴愭湰浠?                depotItemService.updateCurrentUnitPrice(depotItem);
            }
        }
        //璺緞鍒楄〃
        List<String> pathList = new ArrayList<>();
        for(DepotHead depotHead: dhList){
            if(StringUtil.isNotEmpty(depotHead.getFileName())) {
                pathList.add(depotHead.getFileName());
            }
        }
        //閫昏緫鍒犻櫎鏂囦欢
        systemConfigService.deleteFileByPathList(pathList);
        logService.insertLog("鍗曟嵁", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        return 1;
    }

    /**
     * 鍒犻櫎鍗曟嵁涓昏〃淇℃伅
     * @param ids
     * @return
     * @throws Exception
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteDepotHeadByIds(String ids)throws Exception {
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        try{
            result = depotHeadMapperEx.batchDeleteDepotHeadByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<DepotHead> getDepotHeadListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<DepotHead> list = new ArrayList<>();
        try{
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andIdIn(idList);
            list = depotHeadMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 鏍￠獙鍗曟嵁缂栧彿鏄惁瀛樺湪
     * @param id
     * @param number
     * @return
     * @throws Exception
     */
    public int checkIsBillNumberExist(Long id, String number)throws Exception {
        DepotHeadExample example = new DepotHeadExample();
        example.createCriteria().andIdNotEqualTo(id).andNumberEqualTo(number).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotHead> list = null;
        try{
            list = depotHeadMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchForceClose(String ids, HttpServletRequest request) throws Exception {
        int result = 0;
        StringBuilder billNoStr = new StringBuilder();
        List<Long> idList = StringUtil.strToLongList(ids);
        for(Long id: idList) {
            DepotHead depotHead = getDepotHead(id);
            //鐘舵€侀噷闈笉鍖呭惈閮ㄥ垎涓嶈兘寮哄埗缁撳崟
            if(!"3".equals(depotHead.getStatus())) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_FORCE_CLOSE_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_FORCE_CLOSE_FAILED_MSG, depotHead.getNumber()));
            } else {
                billNoStr.append(depotHead.getNumber()).append(" ");
            }
        }
        if(idList.size()>0) {
            DepotHead depotHead = new DepotHead();
            //瀹屾垚鐘舵€?            depotHead.setStatus("2");
            //缁欏娉ㄥ悗闈㈣拷鍔狅細寮哄埗缁撳崟
            String remark = StringUtil.isNotEmpty(depotHead.getRemark())? depotHead.getRemark() + "[寮哄埗缁撳崟]": "[寮哄埗缁撳崟]";
            depotHead.setRemark(remark);
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andIdIn(idList);
            result = depotHeadMapper.updateByExampleSelective(depotHead, example);
            //璁板綍鏃ュ織
            String billNos = billNoStr.toString();
            if(StringUtil.isNotEmpty(billNos)) {
                logService.insertLog("鍗曟嵁", "寮哄埗缁撳崟锛? + billNos,
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchForceClosePurchase(String ids, HttpServletRequest request) throws Exception {
        int result = 0;
        StringBuilder billNoStr = new StringBuilder();
        List<Long> idList = StringUtil.strToLongList(ids);
        for(Long id: idList) {
            DepotHead depotHead = getDepotHead(id);
            //鐘舵€侀噷闈笉鍖呭惈閮ㄥ垎涓嶈兘寮哄埗缁撳崟
            if(!"3".equals(depotHead.getPurchaseStatus())) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_FORCE_CLOSE_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_FORCE_CLOSE_FAILED_MSG, depotHead.getNumber()));
            } else {
                billNoStr.append(depotHead.getNumber()).append(" ");
            }
        }
        if(idList.size()>0) {
            DepotHead depotHead = new DepotHead();
            //瀹屾垚鐘舵€?            depotHead.setPurchaseStatus("2");
            //缁欏娉ㄥ悗闈㈣拷鍔狅細寮哄埗缁撳崟-浠ラ攢瀹氳喘
            String remark = StringUtil.isNotEmpty(depotHead.getRemark())? depotHead.getRemark() + "[寮哄埗缁撳崟-浠ラ攢瀹氳喘]": "[寮哄埗缁撳崟-浠ラ攢瀹氳喘]";
            depotHead.setRemark(remark);
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andIdIn(idList);
            result = depotHeadMapper.updateByExampleSelective(depotHead, example);
            //璁板綍鏃ュ織
            String billNos = billNoStr.toString();
            if(StringUtil.isNotEmpty(billNos)) {
                logService.insertLog("鍗曟嵁", "寮哄埗缁撳崟-浠ラ攢瀹氳喘锛? + billNos,
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(String status, String depotHeadIDs)throws Exception {
        int result = 0;
        List<Long> dhIds = new ArrayList<>();
        List<String> noList = new ArrayList<>();
        List<Long> ids = StringUtil.strToLongList(depotHeadIDs);
        for(Long id: ids) {
            DepotHead depotHead = getDepotHead(id);
            if("0".equals(status)){
                //杩涜鍙嶅鏍告搷浣?                if("1".equals(depotHead.getStatus()) && "0".equals(depotHead.getPurchaseStatus())) {
                    dhIds.add(id);
                    noList.add(depotHead.getNumber());
                } else if("2".equals(depotHead.getPurchaseStatus())) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_PURCHASE_STATUS_TWO_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_PURCHASE_STATUS_TWO_MSG));
                } else if("3".equals(depotHead.getPurchaseStatus())) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_PURCHASE_STATUS_THREE_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_PURCHASE_STATUS_THREE_MSG));
                } else {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_AUDIT_TO_UN_AUDIT_FAILED_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_AUDIT_TO_UN_AUDIT_FAILED_MSG));
                }
            } else if("1".equals(status)){
                //杩涜瀹℃牳鎿嶄綔
                if("0".equals(depotHead.getStatus())) {
                    dhIds.add(id);
                    noList.add(depotHead.getNumber());
                } else {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_UN_AUDIT_TO_AUDIT_FAILED_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_UN_AUDIT_TO_AUDIT_FAILED_MSG));
                }
            }
        }
        if(!dhIds.isEmpty()) {
            DepotHead depotHead = new DepotHead();
            depotHead.setStatus(status);
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andIdIn(dhIds);
            result = depotHeadMapper.updateByExampleSelective(depotHead, example);
            //鏇存柊褰撳墠搴撳瓨
            if(systemConfigService.getForceApprovalFlag()) {
                for(Long dhId: dhIds) {
                    List<DepotItem> list = depotItemService.getListByHeaderId(dhId);
                    for (DepotItem depotItem : list) {
                        depotItemService.updateCurrentStock(depotItem);
                    }
                }
            }
            //璁板綍鏃ュ織
            if(!noList.isEmpty() && ("0".equals(status) || "1".equals(status))) {
                String statusStr = status.equals("1")?"[瀹℃牳]":"[鍙嶅鏍竇";
                logService.insertLog("鍗曟嵁",
                        new StringBuffer(statusStr).append(String.join(", ", noList)).toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        }
        return result;
    }

    public Map<Long,String> findMaterialsListMapByHeaderIdList(List<Long> idList)throws Exception {
        Map<Long,String> materialsListMap = new HashMap<>();
        if(idList.size()>0) {
            List<MaterialsListVo> list = depotHeadMapperEx.findMaterialsListMapByHeaderIdList(idList);
            for (MaterialsListVo materialsListVo : list) {
                String materialsList = materialsListVo.getMaterialsList();
                if(StringUtil.isNotEmpty(materialsList)) {
                    materialsList = materialsList.replace(",","锛?);
                }
                materialsListMap.put(materialsListVo.getHeaderId(), materialsList);
            }
        }
        return materialsListMap;
    }

    public Map<Long,BigDecimal> getMaterialCountListMapByHeaderIdList(List<Long> idList)throws Exception {
        Map<Long,BigDecimal> materialCountListMap = new HashMap<>();
        if(idList.size()>0) {
            List<MaterialCountVo> list = depotHeadMapperEx.getMaterialCountListByHeaderIdList(idList);
            for(MaterialCountVo materialCountVo : list){
                materialCountListMap.put(materialCountVo.getHeaderId(), materialCountVo.getMaterialCount());
            }
        }
        return materialCountListMap;
    }

    public List<DepotHeadVo4InDetail> findInOutDetail(String beginTime, String endTime, String type, String[] creatorArray,
                                                      String[] organArray, List<Long> categoryList, Boolean forceFlag, Boolean inOutManageFlag,
                                                      String materialParam, List<Long> depotList, Integer oId, String number,
                                                      Long creator, String remark, String column, String order, Integer offset, Integer rows) throws Exception{
        List<DepotHeadVo4InDetail> list = null;
        try{
            list =depotHeadMapperEx.findInOutDetail(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    materialParam, depotList, oId, number, creator, remark, column, order, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int findInOutDetailCount(String beginTime, String endTime, String type, String[] creatorArray,
                                    String[] organArray, List<Long> categoryList, Boolean forceFlag, Boolean inOutManageFlag, String materialParam, List<Long> depotList, Integer oId, String number,
                                    Long creator, String remark) throws Exception{
        int result = 0;
        try{
            result =depotHeadMapperEx.findInOutDetailCount(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    materialParam, depotList, oId, number, creator, remark);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public DepotHeadVo4InDetail findInOutDetailStatistic(String beginTime, String endTime, String type, String [] creatorArray,
                                                      String [] organArray, List<Long> categoryList, Boolean forceFlag, Boolean inOutManageFlag,
                                                      String materialParam, List<Long> depotList, Integer oId, String number,
                                                      Long creator, String remark) throws Exception{
        DepotHeadVo4InDetail item = new DepotHeadVo4InDetail();
        try{
            List<DepotHeadVo4InDetail> list =depotHeadMapperEx.findInOutDetailStatistic(beginTime, endTime, type, creatorArray, organArray, categoryList, forceFlag, inOutManageFlag,
                    materialParam, depotList, oId, number, creator, remark);
            if(list.size()>0) {
                item.setOperNumber(list.get(0).getOperNumber());
                item.setAllPrice(list.get(0).getAllPrice());
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return item;
    }

    public List<DepotHeadVo4InOutMCount> findInOutMaterialCount(String beginTime, String endTime, String type, List<Long> categoryList,
                                                                Boolean forceFlag, Boolean inOutManageFlag, String materialParam,
                                                                List<Long> depotList, Long organizationId, Integer oId, String column, String order,
                                                                Integer offset, Integer rows)throws Exception {
        List<DepotHeadVo4InOutMCount> list = null;
        try{
            String [] creatorArray = getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = getCreatorArrayByOrg(organizationId);
            }
            String subType = "鍑哄簱".equals(type)? "閿€鍞? : "";
            String [] organArray = getOrganArray(subType, "");
            list =depotHeadMapperEx.findInOutMaterialCount(beginTime, endTime, type, categoryList, forceFlag, inOutManageFlag, materialParam, depotList, oId,
                    creatorArray, organArray, column, order, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int findInOutMaterialCountTotal(String beginTime, String endTime, String type, List<Long> categoryList,
                                           Boolean forceFlag, Boolean inOutManageFlag, String materialParam,
                                           List<Long> depotList, Long organizationId, Integer oId)throws Exception {
        int result = 0;
        try{
            String [] creatorArray = getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = getCreatorArrayByOrg(organizationId);
            }
            String subType = "鍑哄簱".equals(type)? "閿€鍞? : "";
            String [] organArray = getOrganArray(subType, "");
            result =depotHeadMapperEx.findInOutMaterialCountTotal(beginTime, endTime, type, categoryList, forceFlag, inOutManageFlag, materialParam, depotList, oId,
                    creatorArray, organArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public DepotHeadVo4InOutMCount findInOutMaterialCountStatistic(String beginTime, String endTime, String type, List<Long> categoryList,
                                                                Boolean forceFlag, Boolean inOutManageFlag, String materialParam,
                                                                List<Long> depotList, Long organizationId, Integer oId) throws Exception {
        DepotHeadVo4InOutMCount item = new DepotHeadVo4InOutMCount();
        try{
            String [] creatorArray = getCreatorArray();
            if(creatorArray == null && organizationId != null) {
                creatorArray = getCreatorArrayByOrg(organizationId);
            }
            String subType = "鍑哄簱".equals(type)? "閿€鍞? : "";
            String [] organArray = getOrganArray(subType, "");
            List<DepotHeadVo4InOutMCount> list = depotHeadMapperEx.findInOutMaterialCountStatistic(beginTime, endTime, type, categoryList,
                    forceFlag, inOutManageFlag, materialParam, depotList, oId, creatorArray, organArray);
            if(list.size()>0) {
                item.setNumSum(list.get(0).getNumSum());
                item.setPriceSum(list.get(0).getPriceSum());
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return item;
    }

    public List<DepotHeadVo4InDetail> findAllocationDetail(String beginTime, String endTime, String subType, String number,
                            String [] creatorArray, List<Long> categoryList, Boolean forceFlag, String materialParam, List<Long> depotList, List<Long> depotFList,
                            String remark, String column, String order, Integer offset, Integer rows) throws Exception{
        List<DepotHeadVo4InDetail> list = null;
        try{
            list =depotHeadMapperEx.findAllocationDetail(beginTime, endTime, subType, number, creatorArray, categoryList, forceFlag,
                    materialParam, depotList, depotFList, remark, column, order, offset, rows);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int findAllocationDetailCount(String beginTime, String endTime, String subType, String number,
                            String [] creatorArray, List<Long> categoryList, Boolean forceFlag, String materialParam, List<Long> depotList,  List<Long> depotFList,
                            String remark) throws Exception{
        int result = 0;
        try{
            result =depotHeadMapperEx.findAllocationDetailCount(beginTime, endTime, subType, number, creatorArray, categoryList, forceFlag,
                    materialParam, depotList, depotFList, remark);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public DepotHeadVo4InDetail findAllocationStatistic(String beginTime, String endTime, String subType, String number,
                                                        String [] creatorArray, List<Long> categoryList, Boolean forceFlag, String materialParam, List<Long> depotList, List<Long> depotFList,
                                                        String remark) throws Exception{
        DepotHeadVo4InDetail item = new DepotHeadVo4InDetail();
        try{
            List<DepotHeadVo4InDetail> list =depotHeadMapperEx.findAllocationStatistic(beginTime, endTime, subType, number, creatorArray, categoryList, forceFlag,
                    materialParam, depotList, depotFList, remark);
            if(list.size()>0) {
                item.setOperNumber(list.get(0).getOperNumber());
                item.setAllPrice(list.get(0).getAllPrice());
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return item;
    }

    public List<DepotHeadVo4StatementAccount> getStatementAccount(String beginTime, String endTime, Integer organId, String [] organArray,
                                                                  Integer hasDebt, String supplierType, String type, String subType, String typeBack,
                                                                  String subTypeBack, String billType, Integer offset, Integer rows) {
        List<DepotHeadVo4StatementAccount> list = null;
        try{
            list = depotHeadMapperEx.getStatementAccount(beginTime, endTime, organId, organArray, hasDebt, supplierType, type, subType,typeBack, subTypeBack, billType, offset, rows);
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int getStatementAccountCount(String beginTime, String endTime, Integer organId, String [] organArray,
                                        Integer hasDebt, String supplierType, String type, String subType, String typeBack, String subTypeBack, String billType) {
        int result = 0;
        try{
            result = depotHeadMapperEx.getStatementAccountCount(beginTime, endTime, organId, organArray, hasDebt, supplierType, type, subType,typeBack, subTypeBack, billType);
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<DepotHeadVo4StatementAccount> getStatementAccountTotalPay(String beginTime, String endTime, Integer organId, String [] organArray,
                                                                          Integer hasDebt, String supplierType, String type, String subType,
                                                                          String typeBack, String subTypeBack, String billType) {
        List<DepotHeadVo4StatementAccount> list = null;
        try{
            list = depotHeadMapperEx.getStatementAccountTotalPay(beginTime, endTime, organId, organArray, hasDebt, supplierType, type, subType,typeBack, subTypeBack, billType);
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public int getNeedCount(String supplierType) throws Exception {
        String type = "";
        String subType = "";
        String typeBack = "";
        String subTypeBack = "";
        String billType = "";
        if (("渚涘簲鍟?).equals(supplierType)) {
            type = "鍏ュ簱";
            subType = "閲囪喘";
            typeBack = "鍑哄簱";
            subTypeBack = "閲囪喘閫€璐?;
            billType = "浠樻";
        } else if (("瀹㈡埛").equals(supplierType)) {
            type = "鍑哄簱";
            subType = "閿€鍞?;
            typeBack = "鍏ュ簱";
            subTypeBack = "閿€鍞€€璐?;
            billType = "鏀舵";
        }
        String beginTime = Tools.parseDayToTime(Tools.getYearBegin(), BusinessConstants.DAY_FIRST_TIME);
        String endTime = Tools.getCenternTime(new Date());
        String [] organArray = getOrganArray(subType, "");
        return getStatementAccountCount(beginTime, endTime, null, organArray,
                1, supplierType, type, subType,typeBack, subTypeBack, billType);
    }

    public List<DepotHeadVo4List> getDetailByNumber(String number, HttpServletRequest request)throws Exception {
        List<DepotHeadVo4List> resList = new ArrayList<>();
        try{
            Long userId = userService.getUserId(request);
            String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            Map<Long,String> personMap = personService.getPersonMap();
            Map<Long,String> accountMap = accountService.getAccountMap();
            List<DepotHeadVo4List> list = depotHeadMapperEx.getDetailByNumber(number);
            if (null != list) {
                List<Long> idList = new ArrayList<>();
                List<String> numberList = new ArrayList<>();
                for (DepotHeadVo4List dh : list) {
                    idList.add(dh.getId());
                    numberList.add(dh.getNumber());
                }
                //閫氳繃鎵归噺鏌ヨ鍘绘瀯閫爉ap
                Map<Long,Integer> financialBillNoMap = getFinancialBillNoMapByBillIdList(idList);
                Map<String,Integer> billSizeMap = getBillSizeMapByLinkNumberList(numberList);
                Map<Long,String> materialsListMap = findMaterialsListMapByHeaderIdList(idList);
                Map<Long,BigDecimal> materialCountListMap = getMaterialCountListMapByHeaderIdList(idList);
                DepotHeadVo4List dh = list.get(0);
                String billCategory = getBillCategory(dh.getSubType());
                if(accountMap!=null && StringUtil.isNotEmpty(dh.getAccountIdList()) && StringUtil.isNotEmpty(dh.getAccountMoneyList())) {
                    String accountStr = accountService.getAccountStrByIdAndMoney(accountMap, dh.getAccountIdList(), dh.getAccountMoneyList());
                    dh.setAccountName(accountStr);
                }
                if(dh.getAccountIdList() != null) {
                    String accountidlistStr = dh.getAccountIdList().replace("[", "").replace("]", "").replaceAll("\"", "");
                    dh.setAccountIdList(accountidlistStr);
                }
                if(dh.getAccountMoneyList() != null) {
                    String accountmoneylistStr = dh.getAccountMoneyList().replace("[", "").replace("]", "").replaceAll("\"", "");
                    dh.setAccountMoneyList(accountmoneylistStr);
                }
                if(dh.getChangeAmount() != null) {
                    dh.setChangeAmount(roleService.parseBillPriceByLimit(dh.getChangeAmount().abs(), billCategory, priceLimit, request));
                } else {
                    dh.setChangeAmount(BigDecimal.ZERO);
                }
                if(dh.getTotalPrice() != null) {
                    dh.setTotalPrice(roleService.parseBillPriceByLimit(dh.getTotalPrice().abs(), billCategory, priceLimit, request));
                }
                BigDecimal discountLastMoney = dh.getDiscountLastMoney()!=null?dh.getDiscountLastMoney():BigDecimal.ZERO;
                dh.setDiscountLastMoney(roleService.parseBillPriceByLimit(discountLastMoney, billCategory, priceLimit, request));
                BigDecimal backAmount = dh.getBackAmount()!=null?dh.getBackAmount():BigDecimal.ZERO;
                dh.setBackAmount(roleService.parseBillPriceByLimit(backAmount, billCategory, priceLimit, request));
                if(dh.getDeposit() == null) {
                    dh.setDeposit(BigDecimal.ZERO);
                } else {
                    dh.setDeposit(roleService.parseBillPriceByLimit(dh.getDeposit(), billCategory, priceLimit, request));
                }
                //娆犳璁＄畻
                BigDecimal otherMoney = dh.getOtherMoney()!=null?dh.getOtherMoney():BigDecimal.ZERO;
                BigDecimal deposit = dh.getDeposit()!=null?dh.getDeposit():BigDecimal.ZERO;
                BigDecimal changeAmount = dh.getChangeAmount()!=null?dh.getChangeAmount():BigDecimal.ZERO;
                BigDecimal debt = discountLastMoney.add(otherMoney).subtract((deposit.add(changeAmount)));
                dh.setDebt(roleService.parseBillPriceByLimit(debt, billCategory, priceLimit, request));
                //鏄惁鏈変粯娆惧崟鎴栨敹娆惧崟
                if(financialBillNoMap!=null) {
                    Integer financialBillNoSize = financialBillNoMap.get(dh.getId());
                    dh.setHasFinancialFlag(financialBillNoSize!=null && financialBillNoSize>0);
                }
                //鏄惁鏈夐€€娆惧崟
                if(billSizeMap!=null) {
                    Integer billListSize = billSizeMap.get(dh.getNumber());
                    dh.setHasBackFlag(billListSize!=null && billListSize>0);
                }
                if(StringUtil.isNotEmpty(dh.getSalesMan())) {
                    dh.setSalesManStr(personService.getPersonByMapAndIds(personMap,dh.getSalesMan()));
                }
                if(dh.getOperTime() != null) {
                    dh.setOperTimeStr(getCenternTime(dh.getOperTime()));
                }
                //鍟嗗搧淇℃伅绠€杩?                if(materialsListMap!=null) {
                    dh.setMaterialsList(materialsListMap.get(dh.getId()));
                }
                //鍟嗗搧鎬绘暟閲?                if(materialCountListMap!=null) {
                    dh.setMaterialCount(materialCountListMap.get(dh.getId()));
                }
                User creatorUser = userService.getUser(dh.getCreator());
                if(creatorUser!=null) {
                    dh.setCreatorName(creatorUser.getUsername());
                }
                resList.add(dh);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return resList;
    }

    /**
     * 鏌ヨ闄ゅ綋鍓嶅崟鎹箣澶栫殑鍏宠仈鍗曟嵁鍒楄〃
     * @param linkNumber
     * @param number
     * @return
     * @throws Exception
     */
    public List<DepotHead> getListByLinkNumberExceptCurrent(String linkNumber, String number, String type)throws Exception {
        DepotHeadExample example = new DepotHeadExample();
        example.createCriteria().andLinkNumberEqualTo(linkNumber).andNumberNotEqualTo(number).andTypeEqualTo(type)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        return depotHeadMapper.selectByExample(example);
    }

    /**
     * 鏌ヨ闄ゅ綋鍓嶅崟鎹箣澶栫殑鍏宠仈鍗曟嵁鍒楄〃
     * @param linkApply
     * @param number
     * @return
     * @throws Exception
     */
    public List<DepotHead> getListByLinkApplyExceptCurrent(String linkApply, String number, String type)throws Exception {
        DepotHeadExample example = new DepotHeadExample();
        example.createCriteria().andLinkApplyEqualTo(linkApply).andNumberNotEqualTo(number).andTypeEqualTo(type)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        return depotHeadMapper.selectByExample(example);
    }

    /**
     * 鏍规嵁鍘熷崟鍙锋煡璇㈠叧鑱旂殑鍗曟嵁鍒楄〃(鎵归噺)
     * @param linkNumberList
     * @return
     * @throws Exception
     */
    public List<DepotHead> getBillListByLinkNumberList(List<String> linkNumberList)throws Exception {
        if(linkNumberList!=null && linkNumberList.size()>0) {
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andLinkNumberIn(linkNumberList).andSubTypeLike("閫€璐?).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            return depotHeadMapper.selectByExample(example);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 鏍规嵁鍘熷崟鍙锋煡璇㈠叧鑱旂殑鍗曟嵁鍒楄〃
     * @param linkNumber
     * @return
     * @throws Exception
     */
    public List<DepotHead> getBillListByLinkNumber(String linkNumber)throws Exception {
        DepotHeadExample example = new DepotHeadExample();
        example.createCriteria().andLinkNumberEqualTo(linkNumber).andSubTypeLike("閫€璐?).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        return depotHeadMapper.selectByExample(example);
    }

    /**
     * 鏂板鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?     * @param beanJson
     * @param rows
     * @param request
     * @throws Exception
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void addDepotHeadAndDetail(String beanJson, String rows,
                                      HttpServletRequest request) throws Exception {
        /**澶勭悊鍗曟嵁涓昏〃鏁版嵁*/
        DepotHead depotHead = JSONObject.parseObject(beanJson, DepotHead.class);
        //鍒ゆ柇鐢ㄦ埛鏄惁宸茬粡鐧诲綍杩囷紝鐧诲綍杩囦笉鍐嶅鐞?        User userInfo=userService.getCurrentUser();
        //閫氳繃redis鍘绘牎楠岄噸澶?        String keyNo = userInfo.getLoginName() + "_" + depotHead.getNumber();
        String keyValue = redisService.getCacheObject(keyNo);
        if(StringUtil.isNotEmpty(keyValue)) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_SUBMIT_REPEAT_FAILED_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_SUBMIT_REPEAT_FAILED_MSG));
        } else {
            redisService.storageKeyWithTime(keyNo, depotHead.getNumber(), 2L);
        }
        //鏍￠獙鍗曞彿鏄惁閲嶅
        if(checkIsBillNumberExist(0L, depotHead.getNumber())>0) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_BILL_NUMBER_EXIST_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_BILL_NUMBER_EXIST_MSG));
        }
        //鏍￠獙鏄惁鍚屾椂褰曞叆鍏宠仈璇疯喘鍗曞彿鍜屽叧鑱旇鍗曞彿
        if(StringUtil.isNotEmpty(depotHead.getLinkNumber()) && StringUtil.isNotEmpty(depotHead.getLinkApply())) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_REPEAT_NO_FAILED_CODE,
                    String.format(ExceptionConstants.DEPOT_ITEM_EXIST_REPEAT_NO_FAILED_MSG));
        }
        String subType = depotHead.getSubType();
        //缁撶畻璐︽埛鏍￠獙
        if("閲囪喘".equals(subType) || "閲囪喘閫€璐?.equals(subType) || "閿€鍞?.equals(subType) || "閿€鍞€€璐?.equals(subType)) {
            if (StringUtil.isEmpty(depotHead.getAccountIdList()) && depotHead.getAccountId() == null) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_ACCOUNT_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_ACCOUNT_FAILED_MSG));
            }
        }
        depotHead.setCreator(userInfo==null?null:userInfo.getId());
        depotHead.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if(StringUtil.isEmpty(depotHead.getStatus())) {
            depotHead.setStatus(BusinessConstants.BILLS_STATUS_UN_AUDIT);
        }
        depotHead.setPurchaseStatus(BusinessConstants.BILLS_STATUS_UN_AUDIT);
        depotHead.setPayType(depotHead.getPayType()==null?"鐜颁粯":depotHead.getPayType());
        if(StringUtil.isNotEmpty(depotHead.getAccountIdList())){
            depotHead.setAccountIdList(depotHead.getAccountIdList().replace("[", "").replace("]", "").replaceAll("\"", ""));
        }
        if(StringUtil.isNotEmpty(depotHead.getAccountMoneyList())) {
            //鏍￠獙澶氳处鎴风殑缁撶畻閲戦
            String accountMoneyList = depotHead.getAccountMoneyList().replace("[", "").replace("]", "").replaceAll("\"", "");
            BigDecimal sum = StringUtil.getArrSum(accountMoneyList.split(","));
            BigDecimal manyAccountSum = sum.abs();
            if(manyAccountSum.compareTo(depotHead.getChangeAmount().abs())!=0) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_MANY_ACCOUNT_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_MANY_ACCOUNT_FAILED_MSG));
            }
            depotHead.setAccountMoneyList(accountMoneyList);
        }
        //鏍￠獙绱鎵ｉ櫎璁㈤噾鏄惁瓒呭嚭璁㈠崟涓殑閲戦
        if(depotHead.getDeposit()!=null && StringUtil.isNotEmpty(depotHead.getLinkNumber())) {
            BigDecimal finishDeposit = depotHeadMapperEx.getFinishDepositByNumberExceptCurrent(depotHead.getLinkNumber(), depotHead.getNumber());
            //璁㈠崟涓殑璁㈤噾閲戦
            BigDecimal changeAmount = getDepotHead(depotHead.getLinkNumber()).getChangeAmount();
            if(changeAmount!=null) {
                BigDecimal preDeposit = changeAmount.abs();
                if(depotHead.getDeposit().add(finishDeposit).compareTo(preDeposit)>0) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_DEPOSIT_OVER_PRE_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_DEPOSIT_OVER_PRE_MSG));
                }
            }
        }
        //鏍￠獙闄勪欢鐨勬暟閲?        if(StringUtil.isNotEmpty(depotHead.getFileName())) {
            String[] fileArr = depotHead.getFileName().split(",");
            if(fileArr.length>4) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_FILE_NUM_LIMIT_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_FILE_NUM_LIMIT_MSG, 4));
            }
        }
        depotHeadMapper.insertSelective(depotHead);
        /**鍏ュ簱鍜屽嚭搴撳鐞嗛浠樻淇℃伅*/
        if(BusinessConstants.PAY_TYPE_PREPAID.equals(depotHead.getPayType())){
            if(depotHead.getOrganId()!=null) {
                BigDecimal currentAdvanceIn = supplierService.getSupplier(depotHead.getOrganId()).getAdvanceIn();
                if(currentAdvanceIn.compareTo(depotHead.getTotalPrice())>=0) {
                    //鏇存柊浼氬憳鐨勯浠樻
                    supplierService.updateAdvanceIn(depotHead.getOrganId());
                } else {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_MEMBER_PAY_LACK_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_MEMBER_PAY_LACK_MSG));
                }
            }
        }
        //鏍规嵁鍗曟嵁缂栧彿鏌ヨ鍗曟嵁id
        DepotHeadExample dhExample = new DepotHeadExample();
        dhExample.createCriteria().andNumberEqualTo(depotHead.getNumber()).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<DepotHead> list = depotHeadMapper.selectByExample(dhExample);
        if(list!=null) {
            Long headId = list.get(0).getId();
            /**鍏ュ簱鍜屽嚭搴撳鐞嗗崟鎹瓙琛ㄤ俊鎭?/
            depotItemService.saveDetials(rows,headId, "add",request);
        }
        String statusStr = depotHead.getStatus().equals("1")?"[瀹℃牳]":"";
        logService.insertLog("鍗曟嵁",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(depotHead.getNumber()).append(statusStr).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
    }

    /**
     * 鏇存柊鍗曟嵁涓昏〃鍙婂崟鎹瓙琛ㄤ俊鎭?     * @param beanJson
     * @param rows
     * @param request
     * @throws Exception
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateDepotHeadAndDetail(String beanJson, String rows,HttpServletRequest request)throws Exception {
        /**鏇存柊鍗曟嵁涓昏〃淇℃伅*/
        DepotHead depotHead = JSONObject.parseObject(beanJson, DepotHead.class);
        //鏍￠獙鍗曞彿鏄惁閲嶅
        if(checkIsBillNumberExist(depotHead.getId(), depotHead.getNumber())>0) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_BILL_NUMBER_EXIST_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_BILL_NUMBER_EXIST_MSG));
        }
        //鏍￠獙鏄惁鍚屾椂褰曞叆鍏宠仈璇疯喘鍗曞彿鍜屽叧鑱旇鍗曞彿
        if(StringUtil.isNotEmpty(depotHead.getLinkNumber()) && StringUtil.isNotEmpty(depotHead.getLinkApply())) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_REPEAT_NO_FAILED_CODE,
                    String.format(ExceptionConstants.DEPOT_ITEM_EXIST_REPEAT_NO_FAILED_MSG));
        }
        //鏍￠獙鍗曟嵁鐘舵€侊紝濡傛灉涓嶆槸鏈鏍稿垯鎻愮ず
        if(!"0".equals(getDepotHead(depotHead.getId()).getStatus())) {
            throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_BILL_CANNOT_EDIT_CODE,
                    String.format(ExceptionConstants.DEPOT_HEAD_BILL_CANNOT_EDIT_MSG));
        }
        //鑾峰彇涔嬪墠鐨勪細鍛榠d
        Long preOrganId = getDepotHead(depotHead.getId()).getOrganId();
        String subType = depotHead.getSubType();
        //缁撶畻璐︽埛鏍￠獙
        if("閲囪喘".equals(subType) || "閲囪喘閫€璐?.equals(subType) || "閿€鍞?.equals(subType) || "閿€鍞€€璐?.equals(subType)) {
            if (StringUtil.isEmpty(depotHead.getAccountIdList()) && depotHead.getAccountId() == null) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_ACCOUNT_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_ACCOUNT_FAILED_MSG));
            }
        }
        if(StringUtil.isNotEmpty(depotHead.getAccountIdList())){
            depotHead.setAccountIdList(depotHead.getAccountIdList().replace("[", "").replace("]", "").replaceAll("\"", ""));
        }
        if(StringUtil.isNotEmpty(depotHead.getAccountMoneyList())) {
            //鏍￠獙澶氳处鎴风殑缁撶畻閲戦
            String accountMoneyList = depotHead.getAccountMoneyList().replace("[", "").replace("]", "").replaceAll("\"", "");
            BigDecimal sum = StringUtil.getArrSum(accountMoneyList.split(","));
            BigDecimal manyAccountSum = sum.abs();
            if(manyAccountSum.compareTo(depotHead.getChangeAmount().abs())!=0) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_MANY_ACCOUNT_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_MANY_ACCOUNT_FAILED_MSG));
            }
            depotHead.setAccountMoneyList(accountMoneyList);
        }
        //鏍￠獙绱鎵ｉ櫎璁㈤噾鏄惁瓒呭嚭璁㈠崟涓殑閲戦
        if(depotHead.getDeposit()!=null && StringUtil.isNotEmpty(depotHead.getLinkNumber())) {
            BigDecimal finishDeposit = depotHeadMapperEx.getFinishDepositByNumberExceptCurrent(depotHead.getLinkNumber(), depotHead.getNumber());
            //璁㈠崟涓殑璁㈤噾閲戦
            BigDecimal changeAmount = getDepotHead(depotHead.getLinkNumber()).getChangeAmount();
            if(changeAmount!=null) {
                BigDecimal preDeposit = changeAmount.abs();
                if(depotHead.getDeposit().add(finishDeposit).compareTo(preDeposit)>0) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_DEPOSIT_OVER_PRE_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_DEPOSIT_OVER_PRE_MSG));
                }
            }
        }
        //鏍￠獙闄勪欢鐨勬暟閲?        if(StringUtil.isNotEmpty(depotHead.getFileName())) {
            String[] fileArr = depotHead.getFileName().split(",");
            if(fileArr.length>4) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_FILE_NUM_LIMIT_CODE,
                        String.format(ExceptionConstants.DEPOT_HEAD_FILE_NUM_LIMIT_MSG, 4));
            }
        }
        depotHeadMapper.updateByPrimaryKeySelective(depotHead);
        //濡傛灉瀛樺湪澶氳处鎴风粨绠楅渶瑕佸皢鍘熻处鎴风殑id缃┖
        if(StringUtil.isNotEmpty(depotHead.getAccountIdList())) {
            depotHeadMapperEx.setAccountIdToNull(depotHead.getId());
        }
        /**鍏ュ簱鍜屽嚭搴撳鐞嗛浠樻淇℃伅*/
        if(BusinessConstants.PAY_TYPE_PREPAID.equals(depotHead.getPayType())){
            if(depotHead.getOrganId()!=null){
                BigDecimal currentAdvanceIn = supplierService.getSupplier(depotHead.getOrganId()).getAdvanceIn();
                if(currentAdvanceIn.compareTo(depotHead.getTotalPrice())>=0) {
                    //鏇存柊浼氬憳鐨勯浠樻
                    supplierService.updateAdvanceIn(depotHead.getOrganId());
                    if(null != preOrganId && !preOrganId.equals(depotHead.getOrganId())) {
                        //鏇存柊涔嬪墠浼氬憳鐨勯浠樻
                        supplierService.updateAdvanceIn(preOrganId);
                    }
                } else {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_HEAD_MEMBER_PAY_LACK_CODE,
                            String.format(ExceptionConstants.DEPOT_HEAD_MEMBER_PAY_LACK_MSG));
                }
            }
        }
        /**鍏ュ簱鍜屽嚭搴撳鐞嗗崟鎹瓙琛ㄤ俊鎭?/
        depotItemService.saveDetials(rows,depotHead.getId(), "update",request);
        String statusStr = depotHead.getStatus().equals("1")?"[瀹℃牳]":"";
        logService.insertLog("鍗曟嵁",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(depotHead.getNumber()).append(statusStr).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
    }

    public Map<String, Object> getBuyAndSaleStatistics(String today, String monthFirstDay, String yesterdayBegin, String yesterdayEnd,
                                                       String yearBegin, String yearEnd, HttpServletRequest request) throws Exception {
        Long userId = userService.getUserId(request);
        String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
        Boolean forceFlag = systemConfigService.getForceApprovalFlag();
        String[] creatorArray = getCreatorArray();
        List<InOutPriceVo> inOutPriceVoList = depotHeadMapperEx.getBuyAndSaleStatisticsList(yearBegin, yearEnd, creatorArray, forceFlag);

        String[] periods = {"today", "month", "yesterday", "year"};
        String[] types = {"Buy", "BuyBack", "Sale", "SaleBack", "RetailSale", "RetailSaleBack"};

        Map<String, BigDecimal> statistics = new HashMap<>();

        // 鍒濆鍖?statistics Map
        for (String period : periods) {
            for (String type : types) {
                statistics.put(period + type, BigDecimal.ZERO);
            }
        }

        Date todayDate = Tools.strToDate(today);
        Date monthFirstDate = Tools.strToDate(monthFirstDay);
        Date yesterdayStartDate = Tools.strToDate(yesterdayBegin);
        Date yesterdayEndDate = Tools.strToDate(yesterdayEnd);
        Date yearStartDate = Tools.strToDate(yearBegin);
        Date yearEndDate = Tools.strToDate(yearEnd);

        for (InOutPriceVo item : inOutPriceVoList) {
            Date operTime = item.getOperTime();
            BigDecimal discountLastMoney = item.getDiscountLastMoney();
            BigDecimal totalPriceAbs = item.getTotalPrice().abs();

            if (isWithinRange(operTime, todayDate, Tools.strToDate(getNow3()))) {
                updateStatistics(statistics, item, "today", discountLastMoney, totalPriceAbs);
            }

            if (isWithinRange(operTime, monthFirstDate, Tools.strToDate(getNow3()))) {
                updateStatistics(statistics, item, "month", discountLastMoney, totalPriceAbs);
            }

            if (isWithinRange(operTime, yesterdayStartDate, yesterdayEndDate)) {
                updateStatistics(statistics, item, "yesterday", discountLastMoney, totalPriceAbs);
            }

            if (isWithinRange(operTime, yearStartDate, yearEndDate)) {
                updateStatistics(statistics, item, "year", discountLastMoney, totalPriceAbs);
            }
        }

        Map<String, Object> result = new HashMap<>();
        for (String period : periods) {
            result.put(period + "Buy", roleService.parseHomePriceByLimit(statistics.get(period + "Buy").subtract(statistics.get(period + "BuyBack")), "buy", priceLimit, "***", request));
            result.put(period + "Sale", roleService.parseHomePriceByLimit(statistics.get(period + "Sale").subtract(statistics.get(period + "SaleBack")), "sale", priceLimit, "***", request));
            result.put(period + "RetailSale", roleService.parseHomePriceByLimit(statistics.get(period + "RetailSale").subtract(statistics.get(period + "RetailSaleBack")), "retail", priceLimit, "***", request));
        }

        return result;
    }

    private boolean isWithinRange(Date operTime, Date startDate, Date endDate) {
        return operTime.compareTo(startDate) >= 0 && operTime.compareTo(endDate) <= 0;
    }

    private void updateStatistics(Map<String, BigDecimal> statistics, InOutPriceVo item, String period, BigDecimal discountLastMoney, BigDecimal totalPriceAbs) {
        switch (item.getType()) {
            case "鍏ュ簱":
                switch (item.getSubType()) {
                    case "閲囪喘":
                        statistics.put(period + "Buy", statistics.get(period + "Buy").add(discountLastMoney));
                        break;
                    case "閿€鍞€€璐?:
                        statistics.put(period + "SaleBack", statistics.get(period + "SaleBack").add(discountLastMoney));
                        break;
                    case "闆跺敭閫€璐?:
                        statistics.put(period + "RetailSaleBack", statistics.get(period + "RetailSaleBack").add(totalPriceAbs));
                        break;
                }
                break;
            case "鍑哄簱":
                switch (item.getSubType()) {
                    case "閲囪喘閫€璐?:
                        statistics.put(period + "BuyBack", statistics.get(period + "BuyBack").add(discountLastMoney));
                        break;
                    case "閿€鍞?:
                        statistics.put(period + "Sale", statistics.get(period + "Sale").add(discountLastMoney));
                        break;
                    case "闆跺敭":
                        statistics.put(period + "RetailSale", statistics.get(period + "RetailSale").add(totalPriceAbs));
                        break;
                }
                break;
        }
    }


    public DepotHead getDepotHead(String number)throws Exception {
        DepotHead depotHead = new DepotHead();
        try{
            DepotHeadExample example = new DepotHeadExample();
            example.createCriteria().andNumberEqualTo(number).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            List<DepotHead> list = depotHeadMapper.selectByExample(example);
            if(null!=list && list.size()>0) {
                depotHead = list.get(0);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return depotHead;
    }

    public List<DepotHeadVo4List> debtList(Long organId, String materialParam, String number, String beginTime, String endTime,
                                           String status, Integer offset, Integer rows) {
        List<DepotHeadVo4List> resList = new ArrayList<>();
        try{
            String depotIds = depotService.findDepotStrByCurrentUser();
            String [] depotArray=depotIds.split(",");
            String [] creatorArray = getCreatorArray();
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            List<DepotHeadVo4List> list=depotHeadMapperEx.debtList(organId, creatorArray, status, number,
                    beginTime, endTime, materialParam, depotArray, offset, rows);
            if (null != list) {
                resList = parseDebtBillList(list);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return resList;
    }

    public int debtListCount(Long organId, String materialParam, String number, String beginTime, String endTime,
                             String status) {
        int total = 0;
        try {
            String depotIds = depotService.findDepotStrByCurrentUser();
            String[] depotArray = depotIds.split(",");
            String[] creatorArray = getCreatorArray();
            beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime, BusinessConstants.DAY_LAST_TIME);
            total = depotHeadMapperEx.debtListCount(organId, creatorArray, status, number,
                    beginTime, endTime, materialParam, depotArray);
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return total;
    }

    public void debtExport(Long organId, String materialParam, String number, String type, String subType,
                           String beginTime, String endTime, String status, String mpList,
                           HttpServletRequest request, HttpServletResponse response) {
        try {
            Long userId = userService.getUserId(request);
            String priceLimit = userService.getRoleTypeByUserId(userId).getPriceLimit();
            String billCategory = getBillCategory(subType);
            String depotIds = depotService.findDepotStrByCurrentUser();
            String[] depotArray = depotIds.split(",");
            String[] creatorArray = getCreatorArray();
            status = StringUtil.isNotEmpty(status) ? status : null;
            beginTime = Tools.parseDayToTime(beginTime, BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime, BusinessConstants.DAY_LAST_TIME);
            List<DepotHeadVo4List> dhList = new ArrayList<>();
            List<DepotHeadVo4List> list = depotHeadMapperEx.debtList(organId, creatorArray, status, number,
                    beginTime, endTime, materialParam, depotArray, null, null);
            if (null != list) {
                dhList = parseDebtBillList(list);
            }
            //鐢熸垚Excel鏂囦欢
            String fileName = "鍗曟嵁淇℃伅";
            File file = new File("/opt/"+ fileName);
            WritableWorkbook wtwb = Workbook.createWorkbook(file);
            String oneTip = "";
            String sheetOneStr = "";
            if("閲囪喘".equals(subType)) {
                oneTip = "渚涘簲鍟嗗璐﹀垪琛?;
                sheetOneStr = "渚涘簲鍟?鍗曟嵁缂栧彿,鍏宠仈鍗曟嵁,鍟嗗搧淇℃伅,鍗曟嵁鏃ユ湡,鎿嶄綔鍛?鍗曟嵁閲戦,鏈崟娆犳,宸蹭粯娆犳,寰呬粯娆犳,澶囨敞";
            } else if("鍑哄簱".equals(type) && "閿€鍞?.equals(subType)) {
                oneTip = "瀹㈡埛瀵硅处鍒楄〃";
                sheetOneStr = "瀹㈡埛,鍗曟嵁缂栧彿,鍏宠仈鍗曟嵁,鍟嗗搧淇℃伅,鍗曟嵁鏃ユ湡,鎿嶄綔鍛?鍗曟嵁閲戦,鏈崟娆犳,宸叉敹娆犳,寰呮敹娆犳,澶囨敞";
            }
            if(StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(endTime)) {
                oneTip = oneTip + "锛? + beginTime + "鑷? + endTime + "锛?;
            }
            List<String> sheetOneList = StringUtil.strToStringList(sheetOneStr);
            String[] sheetOneArr = StringUtil.listToStringArray(sheetOneList);
            List<Long> idList = new ArrayList<>();
            List<String[]> billList = new ArrayList<>();
            Map<Long, BillListCacheVo> billListCacheVoMap = new HashMap<>();
            for (DepotHeadVo4List dh : dhList) {
                idList.add(dh.getId());
                BillListCacheVo billListCacheVo = new BillListCacheVo();
                billListCacheVo.setNumber(dh.getNumber());
                billListCacheVo.setOrganName(dh.getOrganName());
                billListCacheVo.setOperTimeStr(getCenternTime(dh.getOperTime()));
                billListCacheVoMap.put(dh.getId(), billListCacheVo);
                String[] objs = new String[sheetOneArr.length];
                objs[0] = dh.getOrganName();
                objs[1] = dh.getNumber();
                objs[2] = dh.getLinkNumber();
                objs[3] = dh.getMaterialsList();
                objs[4] = dh.getOperTimeStr();
                objs[5] = dh.getUserName();
                BigDecimal discountLastMoney = dh.getDiscountLastMoney() == null ? BigDecimal.ZERO : dh.getDiscountLastMoney();
                BigDecimal otherMoney = dh.getOtherMoney() == null ? BigDecimal.ZERO : dh.getOtherMoney();
                BigDecimal deposit = dh.getDeposit() == null ? BigDecimal.ZERO : dh.getDeposit();
                objs[6] = parseDecimalToStr(discountLastMoney.add(otherMoney).subtract(deposit), 2);
                objs[7] = parseDecimalToStr(dh.getNeedDebt(), 2);
                objs[8] = parseDecimalToStr(dh.getFinishDebt(), 2);
                objs[9] = parseDecimalToStr(dh.getDebt(), 2);
                objs[10] = dh.getRemark();
                billList.add(objs);
            }
            ExcelUtils.exportObjectsManySheet(wtwb, oneTip, sheetOneArr, "鍗曟嵁鍒楄〃", 0, billList);
            //瀵煎嚭鏄庣粏鏁版嵁
            if(idList.size()>0) {
                List<DepotItemVo4WithInfoEx> dataList = depotItemMapperEx.getBillDetailListByIds(idList);
                String twoTip = "";
                String sheetTwoStr = "";
                if ("閲囪喘".equals(subType)) {
                    twoTip = "渚涘簲鍟嗗崟鎹槑缁?;
                    sheetTwoStr = "渚涘簲鍟?鍗曟嵁缂栧彿,鍗曟嵁鏃ユ湡,浠撳簱鍚嶇О,鏉＄爜,鍚嶇О,瑙勬牸,鍨嬪彿,棰滆壊,鍝佺墝,鍒堕€犲晢," + mpList + ",鍗曚綅,搴忓垪鍙?鎵瑰彿,鏈夋晥鏈?澶氬睘鎬?鏁伴噺,鍗曚环,閲戦,绋庣巼(%),绋庨,浠风◣鍚堣,閲嶉噺,澶囨敞";
                } else if ("閿€鍞?.equals(subType)) {
                    twoTip = "瀹㈡埛鍗曟嵁鏄庣粏";
                    sheetTwoStr = "瀹㈡埛,鍗曟嵁缂栧彿,鍗曟嵁鏃ユ湡,浠撳簱鍚嶇О,鏉＄爜,鍚嶇О,瑙勬牸,鍨嬪彿,棰滆壊,鍝佺墝,鍒堕€犲晢," + mpList + ",鍗曚綅,搴忓垪鍙?鎵瑰彿,鏈夋晥鏈?澶氬睘鎬?鏁伴噺,鍗曚环,閲戦,绋庣巼(%),绋庨,浠风◣鍚堣,閲嶉噺,澶囨敞";
                }
                if (StringUtil.isNotEmpty(beginTime) && StringUtil.isNotEmpty(endTime)) {
                    twoTip = twoTip + "锛? + beginTime + "鑷? + endTime + "锛?;
                }
                List<String> sheetTwoList = StringUtil.strToStringList(sheetTwoStr);
                String[] sheetTwoArr = StringUtil.listToStringArray(sheetTwoList);
                List<String[]> billDetail = new ArrayList<>();
                for (DepotItemVo4WithInfoEx diEx : dataList) {
                    String[] objs = new String[sheetTwoArr.length];
                    BillListCacheVo billListCacheVo = billListCacheVoMap.get(diEx.getHeaderId());
                    objs[0] = billListCacheVo != null ? billListCacheVo.getOrganName() : "";
                    objs[1] = billListCacheVo != null ? billListCacheVo.getNumber() : "";
                    objs[2] = billListCacheVo != null ? billListCacheVo.getOperTimeStr() : "";
                    objs[3] = diEx.getDepotId() == null ? "" : diEx.getDepotName();
                    objs[4] = diEx.getBarCode();
                    objs[5] = diEx.getMName();
                    objs[6] = diEx.getMStandard();
                    objs[7] = diEx.getMModel();
                    objs[8] = diEx.getMColor();
                    objs[9] = diEx.getBrand();
                    objs[10] = diEx.getMMfrs();
                    objs[11] = diEx.getMOtherField1();
                    objs[12] = diEx.getMOtherField2();
                    objs[13] = diEx.getMOtherField3();
                    objs[14] = diEx.getMaterialUnit();
                    objs[15] = diEx.getSnList();
                    objs[16] = diEx.getBatchNumber();
                    objs[17] = Tools.parseDateToStr(diEx.getExpirationDate());
                    objs[18] = diEx.getSku();
                    objs[19] = parseDecimalToStr(diEx.getOperNumber(), 2);
                    objs[20] = parseDecimalToStr(roleService.parseBillPriceByLimit(diEx.getUnitPrice(), billCategory, priceLimit, request), 2);
                    objs[21] = parseDecimalToStr(roleService.parseBillPriceByLimit(diEx.getAllPrice(), billCategory, priceLimit, request), 2);
                    objs[22] = parseDecimalToStr(roleService.parseBillPriceByLimit(diEx.getTaxRate(), billCategory, priceLimit, request), 2);
                    objs[23] = parseDecimalToStr(roleService.parseBillPriceByLimit(diEx.getTaxMoney(), billCategory, priceLimit, request), 2);
                    objs[24] = parseDecimalToStr(roleService.parseBillPriceByLimit(diEx.getTaxLastMoney(), billCategory, priceLimit, request), 2);
                    BigDecimal allWeight = diEx.getBasicNumber() == null || diEx.getWeight() == null ? BigDecimal.ZERO : diEx.getBasicNumber().multiply(diEx.getWeight());
                    objs[25] = parseDecimalToStr(allWeight, 2);
                    objs[26] = diEx.getRemark();
                    billDetail.add(objs);
                }
                ExcelUtils.exportObjectsManySheet(wtwb, twoTip, sheetTwoArr, "鍗曟嵁鏄庣粏", 1, billDetail);
            }
            wtwb.write();
            wtwb.close();
            ExcelUtils.downloadExcel(file, file.getName(), response);
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
    }

    public List<DepotHeadVo4List> parseDebtBillList(List<DepotHeadVo4List> list) throws Exception {
        List<Long> idList = new ArrayList<>();
        List<DepotHeadVo4List> dhList = new ArrayList<>();
        for (DepotHeadVo4List dh : list) {
            idList.add(dh.getId());
        }
        //閫氳繃鎵归噺鏌ヨ鍘绘瀯閫爉ap
        Map<Long,String> materialsListMap = findMaterialsListMapByHeaderIdList(idList);
        for (DepotHeadVo4List dh : list) {
            if(dh.getChangeAmount() != null) {
                dh.setChangeAmount(dh.getChangeAmount().abs());
            }
            if(dh.getTotalPrice() != null) {
                dh.setTotalPrice(dh.getTotalPrice().abs());
            }
            if(dh.getDeposit() == null) {
                dh.setDeposit(BigDecimal.ZERO);
            }
            if(dh.getOperTime() != null) {
                dh.setOperTimeStr(getCenternTime(dh.getOperTime()));
            }
            BigDecimal discountLastMoney = dh.getDiscountLastMoney()!=null?dh.getDiscountLastMoney():BigDecimal.ZERO;
            BigDecimal otherMoney = dh.getOtherMoney()!=null?dh.getOtherMoney():BigDecimal.ZERO;
            BigDecimal deposit = dh.getDeposit()!=null?dh.getDeposit():BigDecimal.ZERO;
            BigDecimal changeAmount = dh.getChangeAmount()!=null?dh.getChangeAmount().abs():BigDecimal.ZERO;
            //鏈崟娆犳(濡傛灉閫€璐у垯涓鸿礋鏁?
            dh.setNeedDebt(discountLastMoney.add(otherMoney).subtract(deposit.add(changeAmount)));
            if(BusinessConstants.SUB_TYPE_PURCHASE_RETURN.equals(dh.getSubType()) || BusinessConstants.SUB_TYPE_SALES_RETURN.equals(dh.getSubType())) {
                dh.setNeedDebt(BigDecimal.ZERO.subtract(dh.getNeedDebt()));
            }
            BigDecimal needDebt = dh.getNeedDebt()!=null?dh.getNeedDebt():BigDecimal.ZERO;
            BigDecimal finishDebt = accountItemService.getEachAmountByBillId(dh.getId());
            finishDebt = finishDebt!=null?finishDebt:BigDecimal.ZERO;
            //宸叉敹娆犳
            dh.setFinishDebt(finishDebt);
            //寰呮敹娆犳
            dh.setDebt(needDebt.subtract(finishDebt));
            //鍟嗗搧淇℃伅绠€杩?            if(materialsListMap!=null) {
                dh.setMaterialsList(materialsListMap.get(dh.getId()));
            }
            dhList.add(dh);
        }
        return dhList;
    }

    public String getBillCategory(String subType) {
        if(subType.equals("闆跺敭") || subType.equals("闆跺敭閫€璐?)) {
            return "retail";
        } else if(subType.equals("閿€鍞鍗?) || subType.equals("閿€鍞?) || subType.equals("閿€鍞€€璐?)) {
            return "sale";
        } else {
            return "buy";
        }
    }

    /**
     * 鏍煎紡鍖栭噾棰濇牱寮?     * @param decimal
     * @param num
     * @return
     */
    private String parseDecimalToStr(BigDecimal decimal, Integer num) {
        return decimal == null ? "" : decimal.setScale(num, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String parseStatusToStr(String status, String type) {
        if(StringUtil.isNotEmpty(status)) {
            if("purchase".equals(type)) {
                switch (status) {
                    case "2":
                        return "瀹屾垚閲囪喘";
                    case "3":
                        return "閮ㄥ垎閲囪喘";
                }
            } else if("sale".equals(type)) {
                switch (status) {
                    case "2":
                        return "瀹屾垚閿€鍞?;
                    case "3":
                        return "閮ㄥ垎閿€鍞?;
                }
            }
            switch (status) {
                case "0":
                    return "鏈鏍?;
                case "1":
                    return "宸插鏍?;
                case "9":
                    return "瀹℃牳涓?;
            }
        }
        return "";
    }

    public List<DepotHeadVo4List> waitBillList(String number, String materialParam, String type, String subType,
                                               String beginTime, String endTime, String status, int offset, int rows) {
        List<DepotHeadVo4List> resList = new ArrayList<>();
        try{
            String [] depotArray = getDepotArray("鍏跺畠");
            //缁欎粨绠″彲浠ョ湅鍏ㄩ儴鐨勫崟鎹紙姝ゆ椂鍙互閫氳繃鍒嗛厤浠撳簱鍘绘帶鍒舵潈闄愶級
            String [] creatorArray = null;
            String [] subTypeArray = StringUtil.isNotEmpty(subType) ? subType.split(",") : null;
            String [] statusArray = StringUtil.isNotEmpty(status) ? status.split(",") : null;
            Map<Long,String> accountMap = accountService.getAccountMap();
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            List<DepotHeadVo4List> list = depotHeadMapperEx.waitBillList(type, subTypeArray, creatorArray, statusArray, number, beginTime, endTime,
                    materialParam, depotArray, offset, rows);
            if (null != list) {
                List<Long> idList = new ArrayList<>();
                for (DepotHeadVo4List dh : list) {
                    idList.add(dh.getId());
                }
                //閫氳繃鎵归噺鏌ヨ鍘绘瀯閫爉ap
                Map<Long,String> materialsListMap = findMaterialsListMapByHeaderIdList(idList);
                Map<Long,BigDecimal> materialCountListMap = getMaterialCountListMapByHeaderIdList(idList);
                for (DepotHeadVo4List dh : list) {
                    if(accountMap!=null && StringUtil.isNotEmpty(dh.getAccountIdList()) && StringUtil.isNotEmpty(dh.getAccountMoneyList())) {
                        String accountStr = accountService.getAccountStrByIdAndMoney(accountMap, dh.getAccountIdList(), dh.getAccountMoneyList());
                        dh.setAccountName(accountStr);
                    }
                    if(dh.getOperTime() != null) {
                        dh.setOperTimeStr(getCenternTime(dh.getOperTime()));
                    }
                    //鍟嗗搧淇℃伅绠€杩?                    if(materialsListMap!=null) {
                        dh.setMaterialsList(materialsListMap.get(dh.getId()));
                    }
                    //鍟嗗搧鎬绘暟閲?                    if(materialCountListMap!=null) {
                        dh.setMaterialCount(materialCountListMap.get(dh.getId()));
                    }
                    resList.add(dh);
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return resList;
    }

    public Long waitBillCount(String number, String materialParam, String type, String subType,
                             String beginTime, String endTime, String status) {
        Long result=null;
        try{
            String [] depotArray = getDepotArray("鍏跺畠");
            //缁欎粨绠″彲浠ョ湅鍏ㄩ儴鐨勫崟鎹紙姝ゆ椂鍙互閫氳繃鍒嗛厤浠撳簱鍘绘帶鍒舵潈闄愶級
            String [] creatorArray = null;
            String [] subTypeArray = StringUtil.isNotEmpty(subType) ? subType.split(",") : null;
            String [] statusArray = StringUtil.isNotEmpty(status) ? status.split(",") : null;
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            result=depotHeadMapperEx.waitBillCount(type, subTypeArray, creatorArray, statusArray, number, beginTime, endTime,
                    materialParam, depotArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void batchAddDepotHeadAndDetail(String ids, HttpServletRequest request) throws Exception {
        List<DepotHead> dhList = getDepotHeadListByIds(ids);
        StringBuilder sb = new StringBuilder();
        User userInfo=userService.getCurrentUser();
        for(DepotHead depotHead : dhList) {
            String prefixNo = BusinessConstants.DEPOTHEAD_TYPE_IN.equals(depotHead.getType())?"QTRK":"QTCK";
            //鍏宠仈鍗曟嵁鍗曞彿
            String oldNumber = depotHead.getNumber();
            //鏍￠獙鍗曟嵁鏈€鏂扮姸鎬佷笉鑳借繘琛屾壒閲忔搷浣?            if("0".equals(depotHead.getStatus()) || "2".equals(depotHead.getStatus()) || "9".equals(depotHead.getStatus())) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_NEW_STATUS_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_ITEM_EXIST_NEW_STATUS_FAILED_MSG, oldNumber, depotHead.getType()));
            }
            //鏍￠獙鏄惁鏄儴鍒嗗叆搴撴垨鑰呴儴鍒嗗嚭搴?            if("3".equals(depotHead.getStatus())) {
                throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_PARTIALLY_STATUS_FAILED_CODE,
                        String.format(ExceptionConstants.DEPOT_ITEM_EXIST_PARTIALLY_STATUS_FAILED_MSG, oldNumber, depotHead.getType()));
            }
            depotHead.setLinkNumber(oldNumber);
            //缁欏崟鍙烽噸鏂拌祴鍊?            String number = prefixNo + sequenceService.buildOnlyNumber();
            depotHead.setNumber(number);
            depotHead.setDefaultNumber(number);
            depotHead.setOperTime(new Date());
            depotHead.setSubType(BusinessConstants.SUB_TYPE_OTHER);
            depotHead.setChangeAmount(BigDecimal.ZERO);
            depotHead.setTotalPrice(BigDecimal.ZERO);
            depotHead.setDiscountLastMoney(BigDecimal.ZERO);
            depotHead.setCreator(userInfo==null?null:userInfo.getId());
            depotHead.setOrganId(null);
            depotHead.setAccountId(null);
            depotHead.setAccountIdList(null);
            depotHead.setAccountMoneyList(null);
            depotHead.setFileName(null);
            depotHead.setSalesMan(null);
            depotHead.setStatus("0");
            depotHead.setTenantId(null);
            //鏌ヨ鏄庣粏
            List<DepotItemVo4WithInfoEx> itemList = depotItemService.getDetailList(depotHead.getId());
            depotHead.setId(null);
            JSONArray rowArr = new JSONArray();
            for(DepotItemVo4WithInfoEx item: itemList) {
                if("1".equals(item.getEnableSerialNumber())) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_SERIAL_NUMBER_FAILED_CODE,
                            String.format(ExceptionConstants.DEPOT_ITEM_EXIST_SERIAL_NUMBER_FAILED_MSG, oldNumber));
                }
                if("1".equals(item.getEnableBatchNumber())) {
                    throw new BusinessRunTimeException(ExceptionConstants.DEPOT_ITEM_EXIST_BATCH_NUMBER_FAILED_CODE,
                            String.format(ExceptionConstants.DEPOT_ITEM_EXIST_BATCH_NUMBER_FAILED_MSG, oldNumber));
                }
                item.setUnitPrice(BigDecimal.ZERO);
                item.setAllPrice(BigDecimal.ZERO);
                item.setLinkId(item.getId());
                item.setTenantId(null);
                String itemStr = JSONObject.toJSONString(item);
                JSONObject itemObj = JSONObject.parseObject(itemStr);
                itemObj.put("unit", itemObj.getString("materialUnit"));
                rowArr.add(itemObj.toJSONString());
            }
            String rows = rowArr.toJSONString();
            //鏂板鍏跺畠鍏ュ簱鍗曟垨鍏跺畠鍑哄簱鍗?            sb.append("[").append(depotHead.getNumber()).append("]");
            depotHeadMapper.insertSelective(depotHead);
            //鏍规嵁鍗曟嵁缂栧彿鏌ヨ鍗曟嵁id
            DepotHeadExample dhExample = new DepotHeadExample();
            dhExample.createCriteria().andNumberEqualTo(depotHead.getNumber()).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
            List<DepotHead> list = depotHeadMapper.selectByExample(dhExample);
            if(list!=null) {
                Long headId = list.get(0).getId();
                /**鍏ュ簱鍜屽嚭搴撳鐞嗗崟鎹瓙琛ㄤ俊鎭?/
                depotItemService.saveDetials(rows, headId, "add", request);
            }
        }
        logService.insertLog("鍗曟嵁",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_BATCH_ADD).append(sb).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
    }
}
