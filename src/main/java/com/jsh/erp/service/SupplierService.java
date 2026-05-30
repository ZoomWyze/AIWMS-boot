package com.jsh.erp.service;


/**
 * 供应商/客户/会员 Service
 * 提供供应商、客户、会员的业务逻辑：新增/编辑/删除/查询/按类型分类
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.datasource.vo.DepotHeadVo4StatementAccount;
import com.jsh.erp.datasource.vo.SupplierSimple;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.*;
import jxl.Sheet;
import jxl.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;


@Service
public class SupplierService {
    private Logger logger = LoggerFactory.getLogger(SupplierService.class);

    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private SupplierMapperEx supplierMapperEx;
    @Resource
    private LogService logService;
    @Resource
    private UserService userService;
    @Resource
    private AccountHeadMapperEx accountHeadMapperEx;
    @Resource
    private DepotHeadMapperEx depotHeadMapperEx;
    @Resource
    private AccountItemMapperEx accountItemMapperEx;
    @Resource
    private DepotHeadService depotHeadService;
    @Resource
    private UserBusinessService userBusinessService;
    @Resource
    private UserBusinessMapper userBusinessMapper;

    public Supplier getSupplier(long id)throws Exception {
        Supplier result=null;
        try{
            result=supplierMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Supplier> getSupplierListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<Supplier> list = new ArrayList<>();
        try{
            SupplierExample example = new SupplierExample();
            example.createCriteria().andIdIn(idList);
            list = supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Supplier> getSupplier()throws Exception {
        SupplierExample example = new SupplierExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Supplier> list=null;
        try{
            list=supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Supplier> select(String supplier, String type, String contacts, String phonenum, String telephone) throws Exception{
        List<Supplier> list = new ArrayList<>();
        try{
            String [] creatorArray = depotHeadService.getCreatorArray();
            PageUtils.startPage();
            list = supplierMapperEx.selectByConditionSupplier(supplier, type, contacts, phonenum, telephone, creatorArray);
            for(Supplier s : list) {
                Integer supplierId = s.getId().intValue();
                String beginTime = Tools.getYearBegin();
                String endTime = Tools.getCenternTime(new Date());
                BigDecimal sum = BigDecimal.ZERO;
                String supplierType = type;
                String inOutType = "";
                String subType = "";
                String typeBack = "";
                String subTypeBack = "";
                String billType = "";
                if (("渚涘簲鍟?).equals(supplierType)) {
                    inOutType = "鍏ュ簱";
                    subType = "閲囪喘";
                    typeBack = "鍑哄簱";
                    subTypeBack = "閲囪喘閫€璐?;
                    billType = "浠樻";
                } else if (("瀹㈡埛").equals(supplierType)) {
                    inOutType = "鍑哄簱";
                    subType = "閿€鍞?;
                    typeBack = "鍏ュ簱";
                    subTypeBack = "閿€鍞€€璐?;
                    billType = "鏀舵";
                }
                List<DepotHeadVo4StatementAccount> saList = depotHeadService.getStatementAccount(beginTime, endTime, supplierId, null,
                        1, supplierType, inOutType, subType, typeBack, subTypeBack, billType, null, null);
                if(saList.size()>0) {
                    DepotHeadVo4StatementAccount item = saList.get(0);
                    //鏈熷垵 = 璧峰鏈熷垵閲戦+涓婃湡娆犳閲戦-涓婃湡閫€璐х殑娆犳閲戦-涓婃湡鏀朵粯娆?                    BigDecimal preNeed = item.getBeginNeed().add(item.getPreDebtMoney()).subtract(item.getPreReturnDebtMoney()).subtract(item.getPreBackMoney());
                    item.setPreNeed(preNeed);
                    //瀹為檯娆犳 = 鏈湡娆犳-鏈湡閫€璐х殑娆犳閲戦
                    BigDecimal realDebtMoney = item.getDebtMoney().subtract(item.getReturnDebtMoney());
                    item.setDebtMoney(realDebtMoney);
                    //鏈熸湯 = 鏈熷垵+瀹為檯娆犳-鏈湡鏀舵
                    BigDecimal allNeedGet = preNeed.add(realDebtMoney).subtract(item.getBackMoney());
                    sum = sum.add(allNeedGet);
                }
                if(("瀹㈡埛").equals(s.getType())) {
                    s.setAllNeedGet(sum);
                } else if(("渚涘簲鍟?).equals(s.getType())) {
                    s.setAllNeedPay(sum);
                }
            }
        } catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertSupplier(JSONObject obj, HttpServletRequest request)throws Exception {
        Supplier supplier = JSONObject.parseObject(obj.toJSONString(), Supplier.class);
        int result=0;
        try{
            supplier.setEnabled(true);
            User userInfo=userService.getCurrentUser();
            supplier.setCreator(userInfo==null?null:userInfo.getId());
            result=supplierMapper.insertSelective(supplier);
            //鏂板瀹㈡埛鏃剁粰褰撳墠鐢ㄦ埛鍜岀鎴疯嚜鍔ㄦ巿鏉?            setUserCustomerPermission(request, supplier);
            logService.insertLog("鍟嗗",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(supplier.getSupplier()).toString(),request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateSupplier(JSONObject obj, HttpServletRequest request)throws Exception {
        Supplier supplier = JSONObject.parseObject(obj.toJSONString(), Supplier.class);
        if(supplier.getBeginNeedPay() == null) {
            supplier.setBeginNeedPay(BigDecimal.ZERO);
        }
        if(supplier.getBeginNeedGet() == null) {
            supplier.setBeginNeedGet(BigDecimal.ZERO);
        }
        int result=0;
        try{
            result=supplierMapper.updateByPrimaryKeySelective(supplier);
            logService.insertLog("鍟嗗",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(supplier.getSupplier()).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSupplier(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteSupplierByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSupplier(String ids, HttpServletRequest request) throws Exception{
        return batchDeleteSupplierByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSupplierByIds(String ids)throws Exception {
        int result=0;
        String [] idArray=ids.split(",");
        //鏍￠獙璐㈠姟涓昏〃	jsh_accounthead
        List<AccountHead> accountHeadList=null;
        try{
            accountHeadList = accountHeadMapperEx.getAccountHeadListByOrganIds(idArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        if(accountHeadList!=null&&accountHeadList.size()>0){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,OrganIds[{}]",
                    ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,ExceptionConstants.DELETE_FORCE_CONFIRM_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,
                    ExceptionConstants.DELETE_FORCE_CONFIRM_MSG);
        }
        //鏍￠獙鍗曟嵁涓昏〃	jsh_depot_head
        List<DepotHead> depotHeadList=null;
        try{
            depotHeadList = depotHeadMapperEx.getDepotHeadListByOrganIds(idArray);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        if(depotHeadList!=null&&depotHeadList.size()>0){
            logger.error("寮傚父鐮乕{}],寮傚父鎻愮ず[{}],鍙傛暟,OrganIds[{}]",
                    ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,ExceptionConstants.DELETE_FORCE_CONFIRM_MSG,ids);
            throw new BusinessRunTimeException(ExceptionConstants.DELETE_FORCE_CONFIRM_CODE,
                    ExceptionConstants.DELETE_FORCE_CONFIRM_MSG);
        }
        //璁板綍鏃ュ織
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<Supplier> list = getSupplierListByIds(ids);
        for(Supplier supplier: list){
            sb.append("[").append(supplier.getSupplier()).append("]");
        }
        logService.insertLog("鍟嗗", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo=userService.getCurrentUser();
        //鏍￠獙閫氳繃鎵ц鍒犻櫎鎿嶄綔
        try{
            result = supplierMapperEx.batchDeleteSupplierByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        SupplierExample example = new SupplierExample();
        example.createCriteria().andIdNotEqualTo(id).andSupplierEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Supplier> list=null;
        try{
            list= supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public int checkIsNameAndTypeExist(Long id, String name, String type)throws Exception {
        name = name == null? "": name;
        SupplierExample example = new SupplierExample();
        example.createCriteria().andIdNotEqualTo(id).andSupplierEqualTo(name).andTypeEqualTo(type)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<Supplier> list=null;
        try{
            list= supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    /**
     * 鏇存柊浼氬憳鐨勯浠樻
     * @param supplierId
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void updateAdvanceIn(Long supplierId) {
        try{
            //鏌ヨ浼氬憳鍦ㄦ敹棰勪粯娆惧崟鎹殑鎬婚噾棰?            BigDecimal financialAllPrice = accountHeadMapperEx.getFinancialAllPriceByOrganId(supplierId);
            //鏌ヨ浼氬憳鍦ㄩ浂鍞嚭搴撳崟鎹殑鎬婚噾棰?            BigDecimal billAllPrice = depotHeadMapperEx.getBillAllPriceByOrganId(supplierId);
            Supplier supplier = new Supplier();
            supplier.setId(supplierId);
            supplier.setAdvanceIn(financialAllPrice.subtract(billAllPrice));
            supplierMapper.updateByPrimaryKeySelective(supplier);
        } catch (Exception e){
            JshException.writeFail(logger, e);
        }
    }

    public List<Supplier> findBySelectCus(String key, Long organId, Integer limit)throws Exception {
        List<Supplier> list=null;
        try{
            list = supplierMapperEx.findByTypeAndKey("瀹㈡埛", key, limit);
            if(organId!=null) {
                list = addOrganToList(list, organId);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Supplier> findBySelectSup(String key, Long organId, Integer limit)throws Exception {
        List<Supplier> list=null;
        try{
            list = supplierMapperEx.findByTypeAndKey("渚涘簲鍟?, key, limit);
            if(organId!=null) {
                list = addOrganToList(list, organId);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Supplier> findBySelectRetail(String key, Long organId, Integer limit)throws Exception {
        List<Supplier> list=null;
        try{
            list = supplierMapperEx.findByTypeAndKey("浼氬憳", key, limit);
            if(organId!=null) {
                list = addOrganToList(list, organId);
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 缁欏垪琛ㄨ拷鍔犱緵搴斿晢淇℃伅
     * @param list
     * @param organId
     * @return
     */
    public List<Supplier> addOrganToList(List<Supplier> list, Long organId) {
        boolean isExist = false;
        for(Supplier supplier: list) {
            if(supplier.getId().equals(organId)) {
                isExist = true;
            }
        }
        if(!isExist) {
            //鍒楄〃閲岄潰涓嶅瓨鍦ㄥ垯杩藉姞
            Supplier info = supplierMapperEx.getInfoById(organId);
            if(info!=null) {
                list.add(info);
            }
        }
        return list;
    }

    public List<Supplier> findById(Long supplierId)throws Exception {
        SupplierExample example = new SupplierExample();
        example.createCriteria().andIdEqualTo(supplierId)
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Supplier> list=null;
        try{
            list = supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(Boolean status, String ids)throws Exception {
        logService.insertLog("鍟嗗",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ENABLED).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        List<Long> supplierIds = StringUtil.strToLongList(ids);
        Supplier supplier = new Supplier();
        supplier.setEnabled(status);
        SupplierExample example = new SupplierExample();
        example.createCriteria().andIdIn(supplierIds);
        int result=0;
        try{
            result = supplierMapper.updateByExampleSelective(supplier, example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<Supplier> findUserCustomer()throws Exception{
        SupplierExample example = new SupplierExample();
        example.createCriteria().andTypeEqualTo("瀹㈡埛")
                .andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        example.setOrderByClause("sort asc, id desc");
        List<Supplier> list=null;
        try{
            list = supplierMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<Supplier> findByAll(String supplier, String type, String phonenum, String telephone) throws Exception{
        List<Supplier> list=null;
        try{
            list = supplierMapperEx.findByAll(supplier, type, phonenum, telephone);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public Map<String, Object> getBeginNeedByOrganId(Long organId) throws Exception {
        Supplier supplier = getSupplier(organId);
        Map<String, Object> map = new HashMap<>();
        BigDecimal needDebt = BigDecimal.ZERO;
        if("渚涘簲鍟?.equals(supplier.getType())) {
            needDebt = supplier.getBeginNeedPay();
        } else if("瀹㈡埛".equals(supplier.getType())) {
            needDebt = supplier.getBeginNeedGet();
        }
        BigDecimal finishDebt = accountItemMapperEx.getFinishDebtByOrganId(organId).abs();
        BigDecimal eachAmount = BigDecimal.ZERO;
        if(needDebt != null) {
            eachAmount = needDebt.subtract(finishDebt);
        }
        //搴旀敹娆犳
        map.put("needDebt", needDebt);
        //宸叉敹娆犳
        map.put("finishDebt", finishDebt);
        //鏈鏀舵
        map.put("eachAmount", eachAmount);
        return map;
    }

    /**
     * 鏍￠獙鏂囦欢鏍煎紡
     * @param file
     */
    public void checkFileExt(MultipartFile file) {
        //鏂囦欢鎵╁睍鍚嶅彧鑳戒负xls
        String fileName = file.getOriginalFilename();
        if(StringUtil.isNotEmpty(fileName)) {
            String fileExt = fileName.substring(fileName.indexOf(".")+1);
            if(!"xls".equals(fileExt)) {
                throw new BusinessRunTimeException(ExceptionConstants.FILE_EXTENSION_ERROR_CODE,
                        ExceptionConstants.FILE_EXTENSION_ERROR_MSG);
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void importVendor(MultipartFile file, HttpServletRequest request) throws Exception{
        String type = "渚涘簲鍟?;
        User userInfo = userService.getCurrentUser();
        Workbook workbook = Workbook.getWorkbook(file.getInputStream());
        Sheet src = workbook.getSheet(0);
        //'鍚嶇О', '鑱旂郴浜?, '鎵嬫満鍙风爜', '鑱旂郴鐢佃瘽', '鐢靛瓙閭', '浼犵湡', '鏈熷垵搴斾粯', '绾崇◣浜鸿瘑鍒彿', '绋庣巼(%)', '寮€鎴疯', '璐﹀彿', '鍦板潃', '澶囨敞', '鎺掑簭', '鐘舵€?
        List<Supplier> sList = new ArrayList<>();
        for (int i = 2; i < src.getRows(); i++) {
            String supplierName = ExcelUtils.getContent(src, i, 0);
            String enabled = ExcelUtils.getContent(src, i, 14);
            if(StringUtil.isNotEmpty(supplierName) && StringUtil.isNotEmpty(enabled)) {
                Supplier s = new Supplier();
                s.setType(type);
                s.setSupplier(supplierName);
                s.setContacts(ExcelUtils.getContent(src, i, 1));
                s.setTelephone(ExcelUtils.getContent(src, i, 2));
                s.setPhoneNum(ExcelUtils.getContent(src, i, 3));
                s.setEmail(ExcelUtils.getContent(src, i, 4));
                s.setFax(ExcelUtils.getContent(src, i, 5));
                s.setBeginNeedPay(parseBigDecimalEx(ExcelUtils.getContent(src, i, 6)));
                s.setTaxNum(ExcelUtils.getContent(src, i, 7));
                s.setTaxRate(parseBigDecimalEx(ExcelUtils.getContent(src, i, 8)));
                s.setBankName(ExcelUtils.getContent(src, i, 9));
                s.setAccountNumber(ExcelUtils.getContent(src, i, 10));
                s.setAddress(ExcelUtils.getContent(src, i, 11));
                s.setDescription(ExcelUtils.getContent(src, i, 12));
                s.setSort(ExcelUtils.getContent(src, i, 13));
                s.setCreator(userInfo==null?null:userInfo.getId());
                s.setEnabled("1".equals(enabled));
                sList.add(s);
            }
        }
        importExcel(sList, type, request);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void importCustomer(MultipartFile file, HttpServletRequest request) throws Exception{
        String type = "瀹㈡埛";
        User userInfo = userService.getCurrentUser();
        Workbook workbook = Workbook.getWorkbook(file.getInputStream());
        Sheet src = workbook.getSheet(0);
        //'鍚嶇О', '鑱旂郴浜?, '鎵嬫満鍙风爜', '鑱旂郴鐢佃瘽', '鐢靛瓙閭', '浼犵湡', '鏈熷垵搴旀敹', '绾崇◣浜鸿瘑鍒彿', '绋庣巼(%)', '寮€鎴疯', '璐﹀彿', '鍦板潃', '澶囨敞', '鎺掑簭', '鐘舵€?
        List<Supplier> sList = new ArrayList<>();
        for (int i = 2; i < src.getRows(); i++) {
            String supplierName = ExcelUtils.getContent(src, i, 0);
            String enabled = ExcelUtils.getContent(src, i, 14);
            if(StringUtil.isNotEmpty(supplierName) && StringUtil.isNotEmpty(enabled)) {
                Supplier s = new Supplier();
                s.setType(type);
                s.setSupplier(supplierName);
                s.setContacts(ExcelUtils.getContent(src, i, 1));
                s.setTelephone(ExcelUtils.getContent(src, i, 2));
                s.setPhoneNum(ExcelUtils.getContent(src, i, 3));
                s.setEmail(ExcelUtils.getContent(src, i, 4));
                s.setFax(ExcelUtils.getContent(src, i, 5));
                s.setBeginNeedGet(parseBigDecimalEx(ExcelUtils.getContent(src, i, 6)));
                s.setTaxNum(ExcelUtils.getContent(src, i, 7));
                s.setTaxRate(parseBigDecimalEx(ExcelUtils.getContent(src, i, 8)));
                s.setBankName(ExcelUtils.getContent(src, i, 9));
                s.setAccountNumber(ExcelUtils.getContent(src, i, 10));
                s.setAddress(ExcelUtils.getContent(src, i, 11));
                s.setDescription(ExcelUtils.getContent(src, i, 12));
                s.setSort(ExcelUtils.getContent(src, i, 13));
                s.setCreator(userInfo==null?null:userInfo.getId());
                s.setEnabled("1".equals(enabled));
                sList.add(s);
            }
        }
        importExcel(sList, type, request);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void importMember(MultipartFile file, HttpServletRequest request) throws Exception{
        String type = "浼氬憳";
        User userInfo = userService.getCurrentUser();
        Workbook workbook = Workbook.getWorkbook(file.getInputStream());
        Sheet src = workbook.getSheet(0);
        //'鍚嶇О', '鑱旂郴浜?, '鎵嬫満鍙风爜', '鑱旂郴鐢佃瘽', '鐢靛瓙閭', '澶囨敞', '鎺掑簭', '鐘舵€?
        List<Supplier> sList = new ArrayList<>();
        for (int i = 2; i < src.getRows(); i++) {
            String supplierName = ExcelUtils.getContent(src, i, 0);
            String enabled = ExcelUtils.getContent(src, i, 7);
            if(StringUtil.isNotEmpty(supplierName) && StringUtil.isNotEmpty(enabled)) {
                Supplier s = new Supplier();
                s.setType(type);
                s.setSupplier(supplierName);
                s.setContacts(ExcelUtils.getContent(src, i, 1));
                s.setTelephone(ExcelUtils.getContent(src, i, 2));
                s.setPhoneNum(ExcelUtils.getContent(src, i, 3));
                s.setEmail(ExcelUtils.getContent(src, i, 4));
                s.setDescription(ExcelUtils.getContent(src, i, 5));
                s.setSort(ExcelUtils.getContent(src, i, 6));
                s.setCreator(userInfo==null?null:userInfo.getId());
                s.setEnabled("1".equals(enabled));
                sList.add(s);
            }
        }
        importExcel(sList, type, request);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public BaseResponseInfo importExcel(List<Supplier> mList, String type, HttpServletRequest request) throws Exception {
        logService.insertLog(type,
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_IMPORT).append(mList.size()).append(BusinessConstants.LOG_DATA_UNIT).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        BaseResponseInfo info = new BaseResponseInfo();
        Map<String, Object> data = new HashMap<>();
        try {
            for(Supplier supplier: mList) {
                SupplierExample example = new SupplierExample();
                example.createCriteria().andSupplierEqualTo(supplier.getSupplier()).andTypeEqualTo(type).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
                List<Supplier> list= supplierMapper.selectByExample(example);
                if(list.size() <= 0) {
                    supplierMapper.insertSelective(supplier);
                    //鏂板瀹㈡埛鏃剁粰褰撳墠鐢ㄦ埛鍜岀鎴疯嚜鍔ㄦ巿鏉?                    setUserCustomerPermission(request, supplier);
                } else {
                    Long id = list.get(0).getId();
                    supplier.setId(id);
                    supplierMapper.updateByPrimaryKeySelective(supplier);
                }
            }
            info.code = 200;
            data.put("message", "鎴愬姛");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            info.code = 500;
            data.put("message", e.getMessage());
        }
        info.data = data;
        return info;
    }

    public BigDecimal parseBigDecimalEx(String str)throws Exception{
        if(!StringUtil.isEmpty(str)) {
            return new BigDecimal(str);
        } else {
            return null;
        }
    }

    public File exportExcel(List<Supplier> dataList, String type) throws Exception {
        if("渚涘簲鍟?.equals(type)) {
            return exportExcelVendorOrCustomer(dataList, type);
        } else if("瀹㈡埛".equals(type)) {
            return exportExcelVendorOrCustomer(dataList, type);
        } else {
            //浼氬憳
            String[] names = {"浼氬憳鍗″彿*", "鑱旂郴浜?, "鎵嬫満鍙风爜", "鑱旂郴鐢佃瘽", "鐢靛瓙閭", "澶囨敞", "鎺掑簭", "鐘舵€?"};
            String title = "淇℃伅鍐呭";
            List<Object[]> objects = new ArrayList<>();
            if (null != dataList) {
                for (Supplier s : dataList) {
                    Object[] objs = new Object[names.length];
                    objs[0] = s.getSupplier();
                    objs[1] = s.getContacts();
                    objs[2] = s.getTelephone();
                    objs[3] = s.getPhoneNum();
                    objs[4] = s.getEmail();
                    objs[5] = s.getDescription();
                    objs[6] = s.getSort();
                    objs[7] = s.getEnabled() ? "1" : "0";
                    objects.add(objs);
                }
            }
            return ExcelUtils.exportObjectsOneSheet(title, "*瀵煎叆鏃舵湰琛屽唴瀹硅鍕垮垹闄わ紝鍒囪锛?, names, title, objects);
        }
    }

    private File exportExcelVendorOrCustomer(List<Supplier> dataList, String type) throws Exception {
        String beginNeedStr = "";
        if("渚涘簲鍟?.equals(type)) {
            beginNeedStr = "鏈熷垵搴斾粯";
        } else if("瀹㈡埛".equals(type)) {
            beginNeedStr = "鏈熷垵搴旀敹";
        }
        String[] names = {"鍚嶇О*", "鑱旂郴浜?, "鎵嬫満鍙风爜", "鑱旂郴鐢佃瘽", "鐢靛瓙閭", "浼犵湡", beginNeedStr,
                "绾崇◣浜鸿瘑鍒彿", "绋庣巼(%)", "寮€鎴疯", "璐﹀彿", "鍦板潃", "澶囨敞", "鎺掑簭", "鐘舵€?"};
        String title = "淇℃伅鍐呭";
        List<Object[]> objects = new ArrayList<>();
        if (null != dataList) {
            for (Supplier s : dataList) {
                Object[] objs = new Object[names.length];
                objs[0] = s.getSupplier();
                objs[1] = s.getContacts();
                objs[2] = s.getTelephone();
                objs[3] = s.getPhoneNum();
                objs[4] = s.getEmail();
                objs[5] = s.getFax();
                if(("瀹㈡埛").equals(s.getType())) {
                    objs[6] = s.getBeginNeedGet() == null? "" : s.getBeginNeedGet().setScale(2,BigDecimal.ROUND_HALF_UP);
                } else if(("渚涘簲鍟?).equals(s.getType())) {
                    objs[6] = s.getBeginNeedPay() == null? "" : s.getBeginNeedPay().setScale(2,BigDecimal.ROUND_HALF_UP);
                }
                objs[7] = s.getTaxNum();
                objs[8] = s.getTaxRate() == null? "" : s.getTaxRate().setScale(2,BigDecimal.ROUND_HALF_UP);
                objs[9] = s.getBankName();
                objs[10] = s.getAccountNumber();
                objs[11] = s.getAddress();
                objs[12] = s.getDescription();
                objs[13] = s.getSort();
                objs[14] = s.getEnabled() ? "1" : "0";
                objects.add(objs);
            }
        }
        return ExcelUtils.exportObjectsOneSheet(title, "*瀵煎叆鏃舵湰琛屽唴瀹硅鍕垮垹闄わ紝鍒囪锛?, names, title, objects);
    }

    /**
     * 鏂板瀹㈡埛鏃剁粰褰撳墠鐢ㄦ埛鍜岀鎴疯嚜鍔ㄦ巿鏉?     * @param request
     * @param supplier
     * @throws Exception
     */
    private void setUserCustomerPermission(HttpServletRequest request, Supplier supplier) throws Exception {
        if("瀹㈡埛".equals(supplier.getType())) {
            User user = userService.getCurrentUser();
            Supplier sInfo = supplierMapperEx.getSupplierByNameAndType(supplier.getSupplier(), supplier.getType());
            String ubKey = "[" + sInfo.getId() + "]";
            //鎺堟潈褰撳墠鐢ㄦ埛
            setPermissionByParam(user.getId(), ubKey);
            if(!user.getId().equals(user.getTenantId())) {
                //鎺堟潈褰撳墠绉熸埛
                setPermissionByParam(user.getTenantId(), ubKey);
            }
        }
    }

    /**
     * 鏉冮檺鎺堟潈鎿嶄綔
     * @param userId
     * @param ubKey
     * @throws Exception
     */
    private void setPermissionByParam(Long userId, String ubKey) throws Exception {
        List<UserBusiness> ubList = userBusinessService.getBasicData(userId.toString(), "UserCustomer");
        if(ubList ==null || ubList.size() == 0) {
            JSONObject ubObj = new JSONObject();
            ubObj.put("type", "UserCustomer");
            ubObj.put("keyId", userId);
            ubObj.put("value", ubKey);
            UserBusiness userBusiness = JSONObject.parseObject(ubObj.toJSONString(), UserBusiness.class);
            userBusinessMapper.insertSelective(userBusiness);
        } else {
            UserBusiness ubInfo = ubList.get(0);
            JSONObject ubObj = new JSONObject();
            ubObj.put("id", ubInfo.getId());
            ubObj.put("type", ubInfo.getType());
            ubObj.put("keyId", ubInfo.getKeyId());
            ubObj.put("value", ubInfo.getValue() + ubKey);
            UserBusiness userBusiness = JSONObject.parseObject(ubObj.toJSONString(), UserBusiness.class);
            userBusinessMapper.updateByPrimaryKeySelective(userBusiness);
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetAdvanceIn(String ids) throws Exception {
        int res = 0;
        List<Long> idList = StringUtil.strToLongList(ids);
        for(Long sId: idList) {
            updateAdvanceIn(sId);
            res = 1;
        }
        return res;
    }

    public List<SupplierSimple> getAllCustomer() {
        return supplierMapperEx.getAllCustomer();
    }

    public Supplier getInfoByName(String name, String type) {
        return supplierMapperEx.getInfoByName(name, type);
    }
}
