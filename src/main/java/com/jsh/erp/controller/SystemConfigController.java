package com.jsh.erp.controller;


/**
 * 系统配置 Controller
 * 提供系统级配置参数的 CRUD 接口，包括文件上传大小限制等
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.SystemConfig;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * Description
 * @Author: jishenghua
 * @Date: 2021-3-13 0:01
 */
@RestController
@RequestMapping(value = "/systemConfig")
@Api(tags = {"绯荤粺鍙傛暟"})
public class SystemConfigController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(SystemConfigController.class);

    @Resource
    private SystemConfigService systemConfigService;

    @Value(value="${file.uploadType}")
    private Long fileUploadType;

    @Value(value="${file.path}")
    private String filePath;

    @Value(value="${spring.servlet.multipart.max-file-size}")
    private Long maxFileSize;

    @Value(value="${spring.servlet.multipart.max-request-size}")
    private Long maxRequestSize;

    @GetMapping(value = "/info")
    @ApiOperation(value = "鏍规嵁id鑾峰彇淇℃伅")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        SystemConfig systemConfig = systemConfigService.getSystemConfig(id);
        Map<String, Object> objectMap = new HashMap<>();
        if(systemConfig != null) {
            objectMap.put("info", systemConfig);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    @GetMapping(value = "/list")
    @ApiOperation(value = "鑾峰彇淇℃伅鍒楄〃")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request)throws Exception {
        String companyName = StringUtil.getInfo(search, "companyName");
        List<SystemConfig> list = systemConfigService.select(companyName);
        return getDataTable(list);
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "鏂板")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = systemConfigService.insertSystemConfig(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "淇敼")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = systemConfigService.updateSystemConfig(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "鍒犻櫎")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = systemConfigService.deleteSystemConfig(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "鎵归噺鍒犻櫎")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = systemConfigService.batchDeleteSystemConfig(ids, request);
        return returnStr(objectMap, delete);
    }

    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "妫€鏌ュ悕绉版槸鍚﹀瓨鍦?)
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value ="name", required = false) String name,
                                   HttpServletRequest request)throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = systemConfigService.checkIsNameExist(id, name);
        if(exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 鑾峰彇褰撳墠绉熸埛鐨勯厤缃俊鎭?     * @param request
     * @return
     */
    @GetMapping(value = "/getCurrentInfo")
    @ApiOperation(value = "鑾峰彇褰撳墠绉熸埛鐨勯厤缃俊鎭?)
    public BaseResponseInfo getCurrentInfo(HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try{
            List<SystemConfig> list = systemConfigService.getSystemConfig();
            res.code = 200;
            if(list.size()>0) {
                res.data = list.get(0);
            }
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鑾峰彇鏂囦欢澶у皬闄愬埗
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/fileSizeLimit")
    @ApiOperation(value = "鑾峰彇鏂囦欢澶у皬闄愬埗")
    public BaseResponseInfo fileSizeLimit(HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        try{
            Long limit = 0L;
            if(maxFileSize<maxRequestSize) {
                limit = maxFileSize;
            } else {
                limit = maxRequestSize;
            }
            res.code = 200;
            res.data = limit;
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "鑾峰彇鏁版嵁澶辫触";
        }
        return res;
    }

    /**
     * 鏂囦欢涓婁紶缁熶竴鏂规硶
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/upload")
    @ApiOperation(value = "鏂囦欢涓婁紶缁熶竴鏂规硶")
    public BaseResponseInfo upload(HttpServletRequest request, HttpServletResponse response) {
        BaseResponseInfo res = new BaseResponseInfo();
        try {
            String savePath = "";
            String bizPath = request.getParameter("biz");
            if ("bill".equals(bizPath) || "financial".equals(bizPath) || "material".equals(bizPath)) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                MultipartFile file = multipartRequest.getFile("file");// 鑾峰彇涓婁紶鏂囦欢瀵硅薄
                if(fileUploadType == 1) {
                    savePath = systemConfigService.uploadLocal(file, bizPath, request);
                } else if(fileUploadType == 2) {
                    savePath = systemConfigService.uploadAliOss(file, bizPath, request);
                }
                if(StringUtil.isNotEmpty(savePath)){
                    res.code = 200;
                    res.data = savePath;
                }else {
                    res.code = 500;
                    res.data = "涓婁紶澶辫触锛?;
                }
            } else {
                res.code = 505;
                res.data = "鏂囦欢鍒嗙被閿欒锛?;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            res.code = 500;
            res.data = "涓婁紶澶辫触锛?;
        }
        return res;
    }

    /**
     * 棰勮鍥剧墖&涓嬭浇鏂囦欢
     * 璇锋眰鍦板潃锛歨ttp://localhost:8080/common/static/{financial/afsdfasdfasdf_1547866868179.txt}
     *
     * @param request
     * @param response
     */
    @GetMapping(value = "/static/**")
    @ApiOperation(value = "棰勮鍥剧墖&涓嬭浇鏂囦欢")
    public void view(HttpServletRequest request, HttpServletResponse response) {
        // ISO-8859-1 ==> UTF-8 杩涜缂栫爜杞崲
        String imgPath = extractPathFromPattern(request);
        if(StringUtil.isEmpty(imgPath) || imgPath=="null"){
            return;
        }
        // 鍏朵綑澶勭悊鐣?        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            imgPath = imgPath.replace("..", "");
            if (imgPath.endsWith(",")) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            String fileUrl = "";
            if(fileUploadType == 1) {
                fileUrl = systemConfigService.getFileUrlLocal(imgPath);
                inputStream = new BufferedInputStream(new FileInputStream(fileUrl));
            } else if(fileUploadType == 2) {
                fileUrl = systemConfigService.getFileUrlAliOss(imgPath);
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                inputStream = conn.getInputStream();// 閫氳繃杈撳叆娴佽幏鍙栧浘鐗囨暟鎹?            }
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("棰勮鏂囦欢澶辫触" + e.getMessage());
            response.setStatus(404);
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            response.setStatus(404);
            logger.error(e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 棰勮缂╃暐鍥?涓嬭浇鏂囦欢
     * @param request
     * @param response
     */
    @GetMapping(value = "/static/mini/**")
    @ApiOperation(value = "棰勮缂╃暐鍥?涓嬭浇鏂囦欢")
    public void viewMini(HttpServletRequest request, HttpServletResponse response) {
        // ISO-8859-1 ==> UTF-8 杩涜缂栫爜杞崲
        String imgPath = extractPathFromPattern(request);
        if(StringUtil.isEmpty(imgPath) || imgPath=="null"){
            return;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            imgPath = imgPath.replace("..", "");
            if (imgPath.endsWith(",")) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            String fileUrl = "";
            if(fileUploadType == 1) {
                fileUrl = systemConfigService.getFileUrlLocal(imgPath);
                inputStream = new BufferedInputStream(new FileInputStream(fileUrl));
            } else if(fileUploadType == 2) {
                fileUrl = systemConfigService.getFileUrlAliOss(imgPath);
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                inputStream = conn.getInputStream();// 閫氳繃杈撳叆娴佽幏鍙栧浘鐗囨暟鎹?            }
            int index = fileUrl.lastIndexOf(".");
            String ext = fileUrl.substring(index + 1);
            BufferedImage image = systemConfigService.getImageMini(inputStream, 80);
            outputStream = response.getOutputStream();
            ImageIO.write(image, ext, outputStream);
            response.flushBuffer();
        } catch (Exception e) {
            response.setStatus(404);
            logger.error(e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Excel瀵煎嚭缁熶竴鎺ュ彛
     * @param response
     */
    @PostMapping(value = "/exportExcelByParam")
    @ApiOperation(value = "鐢熸垚excel琛ㄦ牸")
    public void exportExcelByParam(@RequestBody JSONObject jsonObject,
                                   HttpServletResponse response) {
        try {
            String title = jsonObject.getString("title");
            String head = jsonObject.getString("head");
            String tip = jsonObject.getString("tip");
            JSONArray arr = jsonObject.getJSONArray("list");
            systemConfigService.exportExcelByParam(title, head, tip, arr, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     *  鎶婃寚瀹歎RL鍚庣殑瀛楃涓插叏閮ㄦ埅鏂綋鎴愬弬鏁?     *  杩欎箞鍋氭槸涓轰簡闃叉URL涓寘鍚腑鏂囨垨鑰呯壒娈婂瓧绗︼紙/绛夛級鏃讹紝鍖归厤涓嶄簡鐨勯棶棰?     * @param request
     * @return
     */
    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}
