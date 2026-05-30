package com.jsh.erp.controller;


/**
 * 账户管理 Controller
 * 提供账户信息的 CRUD 接口（新增/编辑/查询/删除/唯一性校验）
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.Account;
import com.jsh.erp.datasource.vo.AccountVo4InOutList;
import com.jsh.erp.datasource.vo.AccountVo4List;
import com.jsh.erp.service.AccountService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.Constants;
import com.jsh.erp.utils.ErpInfo;
import com.jsh.erp.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author jishenghua 75271*8920
 */
@RestController
@RequestMapping(value = "/account")
@Api(tags = {"璐︽埛绠＄悊"})
public class AccountController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private AccountService accountService;

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Account account = accountService.getAccount(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(account != null) {
            objectMap.put("info", account);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String name = StringUtil.getInfo(search, "name");
        String serialNo = StringUtil.getInfo(search, "serialNo");
        String remark = StringUtil.getInfo(search, "remark");
        List<AccountVo4List> list = accountService.select(name, serialNo, remark);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = accountService.insertAccount(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = accountService.updateAccount(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = accountService.deleteAccount(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = accountService.batchDeleteAccount(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = accountService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鏌ユ壘缁撶畻璐︽埛淇℃伅-涓嬫媺妗?     * @param request
     * @return
     */
    @GetMapping(value = "/findBySelect")
    @ApiOperation(value = "鏌ユ壘缁撶畻璐︽埛淇℃伅-涓嬫媺妗?)
    public String findBySelect(HttpServletRequest request) throws Exception {
        String res = null;
        try {
            List<Account> dataList = accountService.findBySelect();
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (Account account : dataList) {
                    JSONObject item = new JSONObject();
                    item.put("Id", account.getId());
                    //缁撶畻璐︽埛鍚嶇О
                    item.put("AccountName", account.getName());
                    dataArray.add(item);
                }
            }
            res = dataArray.toJSONString();
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鑾峰彇鎵€鏈夌粨绠楄处鎴?     * @param request
     * @return
     */
    @GetMapping(value = "/getAccount")
    @ApiOperation(value = "鑾峰彇鎵€鏈夌粨绠楄处鎴?)
    public BaseResponseInfo getAccount(HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Account> accountList = accountService.getAccount();
            map.put("accountList", accountList);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 璐︽埛娴佹按淇℃伅
     * @param currentPage
     * @param pageSize
     * @param accountId
     * @param initialAmount
     * @param request
     * @return
     */
    @GetMapping(value = "/findAccountInOutList")
    @ApiOperation(value = "璐︽埛娴佹按淇℃伅")
    public BaseResponseInfo findAccountInOutList(@RequestParam("currentPage") Integer currentPage,
                                                 @RequestParam("pageSize") Integer pageSize,
                                                 @RequestParam("accountId") Long accountId,
                                                 @RequestParam("initialAmount") BigDecimal initialAmount,
                                                 @RequestParam(value = "number",required = false) String number,
                                                 @RequestParam(value = "beginTime",required = false) String beginTime,
                                                 @RequestParam(value = "endTime",required = false) String endTime,
                                                 HttpServletRequest request) throws Exception{
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Boolean forceFlag = systemConfigService.getForceApprovalFlag();
            List<AccountVo4InOutList> dataList = accountService.findAccountInOutList(accountId, StringUtil.toNull(number),
                    beginTime, endTime, forceFlag, (currentPage-1)*pageSize, pageSize);
            int total = accountService.findAccountInOutListCount(accountId, StringUtil.toNull(number),
                    beginTime, endTime, forceFlag);
            map.put("total", total);
            //瀛樻斁鏁版嵁json鏁扮粍
            JSONArray dataArray = new JSONArray();
            if (null != dataList) {
                for (AccountVo4InOutList aEx : dataList) {
                    String type = aEx.getType().replace("鍏跺畠", "");
                    aEx.setType(type);
                    String operTime = aEx.getOperTime();
                    BigDecimal balance = accountService.getAccountSum(accountId, null, operTime, forceFlag)
                            .add(accountService.getAccountSumByHead(accountId, null, operTime, forceFlag))
                            .add(accountService.getAccountSumByDetail(accountId, null, operTime, forceFlag))
                            .add(accountService.getManyAccountSum(accountId, null, operTime, forceFlag)).add(initialAmount);
                    aEx.setBalance(balance);
                    aEx.setAccountId(accountId);
                    dataArray.add(aEx);
                }
            }
            map.put("rows", dataArray);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏇存柊榛樿璐︽埛
     * @param object
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateIsDefault")
    @ApiOperation(value = "鏇存柊榛樿璐︽埛")
    public String updateIsDefault(@RequestBody JSONObject object,
                                       HttpServletRequest request) throws Exception{
        Long accountId = object.getLong("id");
        Map<String, Object> objectMap = new HashMap<>();
        int res = accountService.updateIsDefault(accountId);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 鑾峰彇甯︿綑棰濈殑鎶ヨ〃
     * @param request
     * @return
     */
    @GetMapping(value = "/listWithBalance")
    @ApiOperation(value = "鑾峰彇甯︿綑棰濈殑鎶ヨ〃")
    public TableDataInfo listWithBalance(@RequestParam("name") String name,
                                            @RequestParam("serialNo") String serialNo,
                                            HttpServletRequest request) throws Exception {
        List<AccountVo4List> list = accountService.listWithBalance(StringUtil.toNull(name), StringUtil.toNull(serialNo));
        return getDataTable(list);
    }

    /**
     * 缁撶畻璐︽埛鐨勭粺璁?     * @param request
     * @return
     */
    @GetMapping(value = "/getStatistics")
    @ApiOperation(value = "缁撶畻璐︽埛鐨勭粺璁?)
    public BaseResponseInfo getStatistics(@RequestParam("name") String name,
                                          @RequestParam("serialNo") String serialNo,
                                          HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            Map<String, Object> map = accountService.getStatistics(StringUtil.toNull(name), StringUtil.toNull(serialNo));
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鎵归噺璁剧疆鐘舵€?鍚敤鎴栬€呯鐢?     * @param jsonObject
     * @param request
     * @return
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "鎵归噺璁剧疆鐘舵€?)
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request)throws Exception {
        Boolean status = jsonObject.getBoolean("status");
        String ids = jsonObject.getString("ids");
        Map<String, Object> objectMap = new HashMap<>();
        int res = accountService.batchSetStatus(status, ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }
}
