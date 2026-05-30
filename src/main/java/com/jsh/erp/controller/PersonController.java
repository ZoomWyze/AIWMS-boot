package com.jsh.erp.controller;


/**
 * 经手人管理 Controller
 * 提供经手人（操作员/业务员）的 CRUD 接口
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.Person;
import com.jsh.erp.service.PersonService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * @author ji|sheng|hua 绠′紛浣砮rp
 */
@RestController
@RequestMapping(value = "/person")
@Api(tags = {"缁忔墜浜虹鐞?})
public class PersonController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Resource
    private PersonService personService;


    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        Person person = personService.getPerson(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(person != null) {
            objectMap.put("info", person);
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
        String type = StringUtil.getInfo(search, "type");
        List<Person> list = personService.select(name, type);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = personService.insertPerson(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = personService.updatePerson(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = personService.deletePerson(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = personService.batchDeletePerson(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = personService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鍏ㄩ儴鏁版嵁鍒楄〃
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getAllList")
    @ApiOperation(value = "鍏ㄩ儴鏁版嵁鍒楄〃")
    public BaseResponseInfo getAllList(HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Person> personList = personService.getPerson();
            map.put("personList", personList);
            res.code = 200;
            res.data = personList;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏍规嵁Id鑾峰彇缁忔墜浜轰俊鎭?
     * @param personIds
     * @param request
     * @return
     */
    @GetMapping(value = "/getPersonByIds")
    @ApiOperation(value = "鏍规嵁Id鑾峰彇缁忔墜浜轰俊鎭?)
    public BaseResponseInfo getPersonByIds(@RequestParam("personIds") String personIds,
                                           HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Map<Long,String> personMap = personService.getPersonMap();
            String names = personService.getPersonByMapAndIds(personMap, personIds);
            map.put("names", names);
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
     * 鏍规嵁绫诲瀷鑾峰彇缁忔墜浜轰俊鎭?
     * @param type
     * @param request
     * @return
     */
    @GetMapping(value = "/getPersonByType")
    @ApiOperation(value = "鏍规嵁绫诲瀷鑾峰彇缁忔墜浜轰俊鎭?)
    public BaseResponseInfo getPersonByType(@RequestParam("type") String type,
                                            HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Person> personList = personService.getPersonByType(type);
            map.put("personList", personList);
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
     * 鏍规嵁绫诲瀷鑾峰彇缁忔墜浜轰俊鎭?1-閿€鍞憳锛?-浠撶鍛橈紝3-璐㈠姟鍛?
     * @param typeNum
     * @param request
     * @return
     */
    @GetMapping(value = "/getPersonByNumType")
    @ApiOperation(value = "鏍规嵁绫诲瀷鑾峰彇缁忔墜浜轰俊鎭?-閿€鍞憳锛?-浠撶鍛橈紝3-璐㈠姟鍛?)
    public JSONArray getPersonByNumType(@RequestParam("type") String typeNum,
                                        HttpServletRequest request)throws Exception {
        JSONArray dataArray = new JSONArray();
        try {
            String type = "";
            if (typeNum.equals("1")) {
                type = "閿€鍞憳";
            } else if (typeNum.equals("2")) {
                type = "浠撶鍛?;
            } else if (typeNum.equals("3")) {
                type = "璐㈠姟鍛?;
            }
            List<Person> personList = personService.getPersonByType(type);
            if (null != personList) {
                for (Person person : personList) {
                    JSONObject item = new JSONObject();
                    item.put("value", person.getId().toString());
                    item.put("text", person.getName());
                    dataArray.add(item);
                }
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return dataArray;
    }

    /**
     * 鎵归噺璁剧疆鐘舵€?鍚敤鎴栬€呯鐢?
     * @param jsonObject
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
        int res = personService.batchSetStatus(status, ids);
        if(res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }
}
