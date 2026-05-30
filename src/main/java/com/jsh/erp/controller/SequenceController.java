package com.jsh.erp.controller;


/**
 * 序列号生成 Controller
 * 提供单据编号序列的自动生成接口
 *
 * @author jishenghua
 */
import com.jsh.erp.service.SequenceService;
import com.jsh.erp.utils.BaseResponseInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ji-sheng-hua 752*718*920
 */
@RestController
@RequestMapping(value = "/sequence")
@Api(tags = {"鍗曟嵁缂栧彿"})
public class SequenceController {
    private Logger logger = LoggerFactory.getLogger(SequenceController.class);

    @Resource
    private SequenceService sequenceService;

    /**
     * 鍗曟嵁缂栧彿鐢熸垚鎺ュ彛
     * @param request
     * @return
     */
    @GetMapping(value = "/buildNumber")
    @ApiOperation(value = "鍗曟嵁缂栧彿鐢熸垚鎺ュ彛")
    public BaseResponseInfo buildNumber(HttpServletRequest request)throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String number = sequenceService.buildOnlyNumber();
            map.put("defaultNumber", number);
            res.code = 200;
            res.data = map;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

}
