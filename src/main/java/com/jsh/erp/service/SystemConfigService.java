package com.jsh.erp.service;


/**
 * 系统配置 Service
 * 提供系统级配置参数的业务逻辑：查询/更新/文件大小限制
 *
 * @author jishenghua
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.PutObjectRequest;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.SystemConfig;
import com.jsh.erp.datasource.entities.SystemConfigExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.SystemConfigMapper;
import com.jsh.erp.datasource.mappers.SystemConfigMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SystemConfigService {
    private Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private SystemConfigMapperEx systemConfigMapperEx;
    @Resource
    private PlatformConfigService platformConfigService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    @Value(value="${file.uploadType}")
    private Long fileUploadType;

    @Value(value="${file.path}")
    private String filePath;

    private static String DELETED = "deleted";

    public SystemConfig getSystemConfig(long id)throws Exception {
        SystemConfig result=null;
        try{
            result=systemConfigMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<SystemConfig> getSystemConfig()throws Exception {
        SystemConfigExample example = new SystemConfigExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<SystemConfig> list=null;
        try{
            list=systemConfigMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }
    public List<SystemConfig> select(String companyName)throws Exception {
        List<SystemConfig> list=null;
        try{
            PageUtils.startPage();
            list=systemConfigMapperEx.selectByConditionSystemConfig(companyName);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertSystemConfig(JSONObject obj, HttpServletRequest request) throws Exception{
        SystemConfig systemConfig = JSONObject.parseObject(obj.toJSONString(), SystemConfig.class);
        int result=0;
        try{
            result=systemConfigMapper.insertSelective(systemConfig);
            String logInfo = StringUtil.isNotEmpty(systemConfig.getCompanyName())?systemConfig.getCompanyName():"閰嶇疆淇℃伅";
            logService.insertLogWithUserId(userService.getCurrentUser().getId(), userService.getCurrentUser().getTenantId(), "绯荤粺閰嶇疆",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(logInfo).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateSystemConfig(JSONObject obj, HttpServletRequest request) throws Exception{
        SystemConfig systemConfig = JSONObject.parseObject(obj.toJSONString(), SystemConfig.class);
        int result=0;
        try{
            result = systemConfigMapper.updateByPrimaryKeySelective(systemConfig);
            String logInfo = StringUtil.isNotEmpty(systemConfig.getCompanyName())?systemConfig.getCompanyName():"閰嶇疆淇℃伅";
            logService.insertLogWithUserId(userService.getCurrentUser().getId(), userService.getCurrentUser().getTenantId(), "绯荤粺閰嶇疆",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(logInfo).toString(), request);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSystemConfig(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteSystemConfigByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSystemConfig(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteSystemConfigByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSystemConfigByIds(String ids)throws Exception {
        logService.insertLog("绯荤粺閰嶇疆",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_DELETE).append(ids).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        try{
            result = systemConfigMapperEx.batchDeleteSystemConfigByIds(new Date(), userInfo == null ? null : userInfo.getId(), idArray);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name) throws Exception{
        SystemConfigExample example = new SystemConfigExample();
        example.createCriteria().andIdNotEqualTo(id).andCompanyNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<SystemConfig> list =null;
        try{
            list=systemConfigMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    /**
     * 鏈湴鏂囦欢涓婁紶
     * @param mf 鏂囦欢
     * @param bizPath  鑷畾涔夎矾寰?     * @return
     */
    public String uploadLocal(MultipartFile mf, String bizPath, HttpServletRequest request) throws Exception {
        try {
            if(StringUtil.isEmpty(bizPath)){
                bizPath = "";
            }
            // Validate bizPath to prevent directory traversal
            if (bizPath.contains("..") || bizPath.contains("/")) {
                throw new IllegalArgumentException("Invalid bizPath");
            }
            String token = request.getHeader("X-Access-Token");
            Long tenantId = Tools.getTenantIdByToken(token);
            bizPath = bizPath + File.separator + tenantId;
            String ctxPath = filePath;
            String fileName = null;
            File file = new File(ctxPath + File.separator + bizPath + File.separator );
            if (!file.exists()) {
                file.mkdirs();// 鍒涘缓鏂囦欢鏍圭洰褰?            }
            String orgName = mf.getOriginalFilename();// 鑾峰彇鏂囦欢鍚?            orgName = FileUtils.getFileName(orgName);

            // Validate file extension to allow only specific types
            String[] allowedExtensions = {".gif", ".jpg", ".jpeg", ".png", ".pdf", ".txt",".doc",".docx",".xls",".xlsx",
                    ".ppt",".pptx",".zip",".rar",".mp3",".mp4",".avi"};
            boolean isValidExtension = false;
            for (String ext : allowedExtensions) {
                if (orgName.toLowerCase().endsWith(ext)) {
                    isValidExtension = true;
                    break;
                }
            }
            if (!isValidExtension) {
                throw new IllegalArgumentException("Invalid file type");
            }

            if(orgName.contains(".")){
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
            }else{
                fileName = orgName+ "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);

            // 杩斿洖璺緞
            String dbpath = null;
            if(StringUtil.isNotEmpty(bizPath)){
                dbpath = bizPath + File.separator + fileName;
            }else{
                dbpath = fileName;
            }
            if (dbpath.contains("\\")) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * 闃块噷Oss鏂囦欢涓婁紶
     * @param mf 鏂囦欢
     * @param bizPath  鑷畾涔夎矾寰?     * @return
     */
    public String uploadAliOss(MultipartFile mf, String bizPath, HttpServletRequest request) throws Exception {
        if(StringUtil.isEmpty(bizPath)){
            bizPath = "";
        }
        // Validate bizPath to prevent directory traversal
        if (bizPath.contains("..") || bizPath.contains("/")) {
            throw new IllegalArgumentException("Invalid bizPath");
        }
        String token = request.getHeader("X-Access-Token");
        Long tenantId = Tools.getTenantIdByToken(token);
        bizPath = bizPath + "/" + tenantId;
        String endpoint = platformConfigService.getPlatformConfigByKey("aliOss_endpoint").getPlatformValue();
        String accessKeyId = platformConfigService.getPlatformConfigByKey("aliOss_accessKeyId").getPlatformValue();
        String accessKeySecret = platformConfigService.getPlatformConfigByKey("aliOss_accessKeySecret").getPlatformValue();
        String bucketName = platformConfigService.getPlatformConfigByKey("aliOss_bucketName").getPlatformValue();
        // 濉啓Object瀹屾暣璺緞锛屽畬鏁磋矾寰勪腑涓嶈兘鍖呭惈Bucket鍚嶇О锛屼緥濡俥xampledir/exampleobject.txt銆?        String fileName = "";
        String orgName = mf.getOriginalFilename();// 鑾峰彇鏂囦欢鍚?        orgName = FileUtils.getFileName(orgName);

        // Validate file extension to allow only specific types
        String[] allowedExtensions = {".gif", ".jpg", ".jpeg", ".png", ".pdf", ".txt",".doc",".docx",".xls",".xlsx",
                ".ppt",".pptx",".zip",".rar",".mp3",".mp4",".avi"};
        boolean isValidExtension = false;
        for (String ext : allowedExtensions) {
            if (orgName.toLowerCase().endsWith(ext)) {
                isValidExtension = true;
                break;
            }
        }
        if (!isValidExtension) {
            throw new IllegalArgumentException("Invalid file type");
        }

        if(orgName.contains(".")){
            fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
        }else{
            fileName = orgName+ "_" + System.currentTimeMillis();
        }
        String filePathStr = StringUtil.isNotEmpty(filePath)? filePath.substring(1):"";
        String objectName = filePathStr + "/" + bizPath + "/" + fileName;
        String smallObjectName = filePathStr + "-small/" + bizPath + "/" + fileName;
        // 濡傛灉鏈寚瀹氭湰鍦拌矾寰勶紝鍒欓粯璁や粠绀轰緥绋嬪簭鎵€灞為」鐩搴旀湰鍦拌矾寰勪腑涓婁紶鏂囦欢娴併€?        byte [] byteArr = mf.getBytes();

        // 鍒涘缓OSSClient瀹炰緥銆?        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 淇濆瓨鍘熸枃浠?            InputStream inputStream = new ByteArrayInputStream(byteArr);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(putObjectRequest);
            // 濡傛灉鏄浘鐗?淇濆瓨缂╃暐鍥?            int index = fileName.lastIndexOf(".");
            String ext = fileName.substring(index + 1);
            if(ext.contains("gif") || ext.contains("jpg") || ext.contains("jpeg") || ext.contains("png")
                    || ext.contains("GIF") || ext.contains("JPG") || ext.contains("JPEG") || ext.contains("PNG")) {
                String fileUrl = getFileUrlAliOss(bizPath + "/" + fileName);
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                InputStream imgInputStream = conn.getInputStream();// 閫氳繃杈撳叆娴佽幏鍙栧浘鐗囨暟鎹?                BufferedImage smallImage = getImageMini(imgInputStream, 80);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
                ImageIO.write(smallImage, ext, imOut);
                InputStream isImg = new ByteArrayInputStream(bs.toByteArray());
                PutObjectRequest putSmallObjectRequest = new PutObjectRequest(bucketName, smallObjectName, isImg);
                ossClient.putObject(putSmallObjectRequest);
            }
            // 杩斿洖璺緞
            return bizPath + "/" + fileName;
        } catch (OSSException oe) {
            logger.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            logger.error("Error Message:" + oe.getErrorMessage());
            logger.error("Error Code:" + oe.getErrorCode());
            logger.error("Request ID:" + oe.getRequestId());
            logger.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            logger.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return "";
    }

    public String getFileUrlLocal(String imgPath) {
        return filePath + File.separator + imgPath;
    }

    public String getFileUrlAliOss(String imgPath) throws Exception {
        String linkUrl = platformConfigService.getPlatformConfigByKey("aliOss_linkUrl").getPlatformValue();
        return linkUrl + filePath + "/" + imgPath;
    }

    /**
     * 閫昏緫鍒犻櫎鏂囦欢
     * @param pathList
     */
    public void deleteFileByPathList(List<String> pathList) throws Exception {
        if(fileUploadType == 1) {
            //鏈湴
            for(String pathStr: pathList) {
                if(StringUtil.isNotEmpty(pathStr)) {
                    String[] pathArr = pathStr.split(",");
                    for (String path : pathArr) {
                        // 鎻愬彇鏂囦欢鐨勮矾寰?                        String pathDir = getDirByPath(path);
                        if (StringUtil.isNotEmpty(pathDir)) {
                            // 婧愭枃浠惰矾寰?                            Path sourcePath = Paths.get(filePath + File.separator + path);
                            // 鐩爣鏂囦欢璺緞锛堟敞鎰忚繖閲屾槸鏂版枃浠剁殑瀹屾暣璺緞锛屽寘鎷枃浠跺悕锛?                            Path targetPath = Paths.get(filePath + File.separator + DELETED + File.separator + path);
                            try {
                                File file = new File(filePath + File.separator + DELETED + File.separator + pathDir);
                                if (!file.exists()) {
                                    file.mkdirs();// 鍒涘缓鏂囦欢鏍圭洰褰?                                }
                                // 澶嶅埗鏂囦欢锛屽鏋滅洰鏍囨枃浠跺凡瀛樺湪鍒欐浛鎹㈠畠
                                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                // 鍒犻櫎婧愭枃浠?                                Files.delete(sourcePath);
                                logger.info("File copied successfully.");
                            } catch (NoSuchFileException e) {
                                logger.error("Source file not found: " + e.getMessage());
                            } catch (IOException e) {
                                logger.error("An I/O error occurred: " + e.getMessage());
                            } catch (SecurityException e) {
                                logger.error("No permission to copy file: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } else if(fileUploadType == 2) {
            //oss
            String endpoint = platformConfigService.getPlatformConfigByKey("aliOss_endpoint").getPlatformValue();
            String accessKeyId = platformConfigService.getPlatformConfigByKey("aliOss_accessKeyId").getPlatformValue();
            String accessKeySecret = platformConfigService.getPlatformConfigByKey("aliOss_accessKeySecret").getPlatformValue();
            String bucketName = platformConfigService.getPlatformConfigByKey("aliOss_bucketName").getPlatformValue();
            for(String pathStr: pathList) {
                if(StringUtil.isNotEmpty(pathStr)) {
                    String[] pathArr = pathStr.split(",");
                    for (String path : pathArr) {
                        if(StringUtil.isNotEmpty(path)) {
                            // 鍒涘缓OSSClient瀹炰緥銆?                            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                            try {
                                String filePathStr = StringUtil.isNotEmpty(filePath) ? filePath.substring(1) : "";
                                String sourceObjectKey = filePathStr + "/" + path;
                                String sourceSmallObjectKey = filePathStr + "-small/" + path;
                                String destinationObjectKey = DELETED + "/list/" + sourceObjectKey;
                                String destinationSmallObjectKey = DELETED + "/list/" + sourceSmallObjectKey;
                                this.copySourceToDest(ossClient, bucketName, sourceObjectKey, destinationObjectKey);
                                this.copySourceToDest(ossClient, bucketName, sourceSmallObjectKey, destinationSmallObjectKey);
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                            } finally {
                                // 鍏抽棴OSSClient銆?                                if (ossClient != null) {
                                    ossClient.shutdown();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param ossClient
     * @param bucketName
     * @param sourceObjectKey 婧愭枃浠惰矾寰勶紝鍖呮嫭鐩綍鍜屾枃浠跺悕
     * @param destinationObjectKey 鐩爣鏂囦欢璺緞锛屽寘鎷柊鐩綍鍜屾枃浠跺悕
     */
    public void copySourceToDest(OSS ossClient, String bucketName, String sourceObjectKey, String destinationObjectKey) {
        // 澶嶅埗鏂囦欢
        CopyObjectResult copyResult = ossClient.copyObject(bucketName, sourceObjectKey, bucketName, destinationObjectKey);
        // 纭澶嶅埗鎴愬姛
        if (copyResult != null && copyResult.getETag() != null) {
            logger.info("鏂囦欢澶嶅埗鎴愬姛锛孍Tag: " + copyResult.getETag());
            // 鍒犻櫎婧愭枃浠?            ossClient.deleteObject(bucketName, sourceObjectKey);
            logger.info("婧愭枃浠跺凡鍒犻櫎锛? + sourceObjectKey);
        } else {
            logger.info("鏂囦欢澶嶅埗澶辫触");
        }
    }

    public String getDirByPath(String path) {
        if(path.lastIndexOf("/")>-1) {
            return path.substring(0, path.lastIndexOf("/"));
        } else {
            return null;
        }
    }

    public BufferedImage getImageMini(InputStream inputStream, int w) throws Exception {
        BufferedImage img = ImageIO.read(inputStream);
        //鑾峰彇鍥剧墖鐨勯暱鍜屽
        int width = img.getWidth();
        int height = img.getHeight();
        int tempw = 0;
        int temph = 0;
        if(width>height){
            tempw = w;
            temph = height* w/width;
        }else{
            tempw = w*width/height;
            temph = w;
        }
        Image _img = img.getScaledInstance(tempw, temph, Image.SCALE_DEFAULT);
        BufferedImage image = new BufferedImage(tempw, temph, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(_img, 0, 0, null);
        graphics.dispose();
        return image;
    }

    /**
     * 鑾峰彇浠撳簱寮€鍏?     * @return
     * @throws Exception
     */
    public boolean getDepotFlag() throws Exception {
        boolean depotFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getDepotFlag();
            if(("1").equals(flag)) {
                depotFlag = true;
            }
        }
        return depotFlag;
    }

    /**
     * 鑾峰彇瀹㈡埛寮€鍏?     * @return
     * @throws Exception
     */
    public boolean getCustomerFlag() throws Exception {
        boolean customerFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getCustomerFlag();
            if(("1").equals(flag)) {
                customerFlag = true;
            }
        }
        return customerFlag;
    }

    /**
     * 鑾峰彇璐熷簱瀛樺紑鍏?     * @return
     * @throws Exception
     */
    public boolean getMinusStockFlag() throws Exception {
        boolean minusStockFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getMinusStockFlag();
            if(("1").equals(flag)) {
                minusStockFlag = true;
            }
        }
        return minusStockFlag;
    }

    /**
     * 鑾峰彇鏇存柊鍗曚环寮€鍏?     * @return
     * @throws Exception
     */
    public boolean getUpdateUnitPriceFlag() throws Exception {
        boolean updateUnitPriceFlag = true;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getUpdateUnitPriceFlag();
            if(("0").equals(flag)) {
                updateUnitPriceFlag = false;
            }
        }
        return updateUnitPriceFlag;
    }

    /**
     * 鑾峰彇瓒呭嚭鍏宠仈鍗曟嵁寮€鍏?     * @return
     * @throws Exception
     */
    public boolean getOverLinkBillFlag() throws Exception {
        boolean overLinkBillFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getOverLinkBillFlag();
            if(("1").equals(flag)) {
                overLinkBillFlag = true;
            }
        }
        return overLinkBillFlag;
    }

    /**
     * 鑾峰彇寮哄鏍稿紑鍏?     * @return
     * @throws Exception
     */
    public boolean getForceApprovalFlag() throws Exception {
        boolean forceApprovalFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getForceApprovalFlag();
            if(("1").equals(flag)) {
                forceApprovalFlag = true;
            }
        }
        return forceApprovalFlag;
    }

    /**
     * 鑾峰彇澶氱骇瀹℃牳寮€鍏?     * @return
     * @throws Exception
     */
    public boolean getMultiLevelApprovalFlag() throws Exception {
        boolean multiLevelApprovalFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getMultiLevelApprovalFlag();
            if(("1").equals(flag)) {
                multiLevelApprovalFlag = true;
            }
        }
        return multiLevelApprovalFlag;
    }

    /**
     * 鑾峰彇鍑哄叆搴撶鐞嗗紑鍏?     * @return
     * @throws Exception
     */
    public boolean getInOutManageFlag() throws Exception {
        boolean inOutManageFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getInOutManageFlag();
            if(("1").equals(flag)) {
                inOutManageFlag = true;
            }
        }
        return inOutManageFlag;
    }

    /**
     * 鑾峰彇绉诲姩骞冲潎浠峰紑鍏?     * @return
     * @throws Exception
     */
    public boolean getMoveAvgPriceFlag() throws Exception {
        boolean moveAvgPriceFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getMoveAvgPriceFlag();
            if(("1").equals(flag)) {
                moveAvgPriceFlag = true;
            }
        }
        return moveAvgPriceFlag;
    }

    /**
     * 鑾峰彇瀹㈡埛闈欐€佸崟浠峰紑鍏?     * @return
     * @throws Exception
     */
    public boolean getCustomerStaticPriceFlag() throws Exception {
        boolean customerStaticPriceFlag = false;
        List<SystemConfig> list = getSystemConfig();
        if(list.size()>0) {
            String flag = list.get(0).getCustomerStaticPriceFlag();
            if(("1").equals(flag)) {
                customerStaticPriceFlag = true;
            }
        }
        return customerStaticPriceFlag;
    }

    /**
     * Excel瀵煎嚭缁熶竴鏂规硶
     * @param title
     * @param head
     * @param tip
     * @param arr
     * @param response
     * @throws Exception
     */
    public void exportExcelByParam(String title, String head, String tip, JSONArray arr, HttpServletResponse response) throws Exception {
        List<String> nameList = StringUtil.strToStringList(head);
        String[] names = StringUtil.listToStringArray(nameList);
        List<Object[]> objects = new ArrayList<>();
        if (null != arr) {
            for (Object object: arr) {
                List<Object> list = (List<Object>) object;
                Object[] objs = new Object[names.length];
                for (int i = 0; i < list.size(); i++) {
                    if(null != list.get(i)) {
                        objs[i] = list.get(i);
                    }
                }
                objects.add(objs);
            }
        }
        File file = ExcelUtils.exportObjectsOneSheet(title, tip, names, title, objects);
        ExcelUtils.downloadExcel(file, file.getName(), response);
    }
}